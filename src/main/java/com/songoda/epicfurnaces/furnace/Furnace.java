package com.songoda.epicfurnaces.furnace;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.ServerVersion;
import com.songoda.core.gui.GuiManager;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.core.hooks.ProtectionManager;
import com.songoda.core.math.MathUtils;
import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.boost.BoostData;
import com.songoda.epicfurnaces.furnace.levels.Level;
import com.songoda.epicfurnaces.gui.GUIOverview;
import com.songoda.epicfurnaces.settings.Settings;
import com.songoda.epicfurnaces.utils.CostType;
import com.songoda.epicfurnaces.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by songoda on 3/7/2017.
 */
public class Furnace {
    private final EpicFurnaces plugin = EpicFurnaces.getInstance();

    private final String hologramId = UUID.randomUUID().toString();

    // Identifier for database use.
    private int id;

    private final Location location;
    private Level level = plugin.getLevelManager().getLowestLevel();
    private String nickname = null;
    private UUID placedBy = null;
    private int uses, radiusOverheatLast, radiusFuelshareLast = 0;

    private final Map<CompatibleMaterial, Integer> toLevel = new HashMap<>();

    private final List<Location> radiusOverheat = new ArrayList<>();
    private final List<Location> radiusFuelshare = new ArrayList<>();
    private final List<UUID> accessList = new ArrayList<>();

    public Furnace(Location location) {
        this.location = location;
    }

    public void overview(GuiManager guiManager, Player player) {
        if (placedBy == null) placedBy = player.getUniqueId();

        if (!player.hasPermission("epicfurnaces.overview")) return;

        if (Settings.USE_PROTECTION_PLUGINS.getBoolean() && !ProtectionManager.canInteract(player, location)) {
            player.sendMessage(plugin.getLocale().getMessage("event.general.protected").getPrefixedMessage());
            return;
        }

        guiManager.showGUI(player, new GUIOverview(plugin, this, player));
    }

    public void plus(FurnaceSmeltEvent event) {
        Block block = location.getBlock();
        if (!block.getType().name().contains("FURNACE") && !block.getType().name().contains("SMOKER")) return;

        this.uses++;
        plugin.getDataManager().queueFurnaceForUpdate(this);

        CompatibleMaterial material = CompatibleMaterial.getMaterial(event.getResult());
        int needed = -1;

        if (level.getMaterials().containsKey(material)) {
            int amount = addToLevel(material, 1);
            plugin.getDataManager().updateLevelupItems(this, material, amount);
            needed = level.getMaterials().get(material) - getToLevel(material);
        }


        if (level.getReward() == null) return;

        String reward = level.getReward();
        int min = 1;
        int max = 1;
        if (reward.contains(":")) {
            String[] rewardSplit = reward.split(":");
            reward = rewardSplit[0].substring(0, rewardSplit[0].length() - 1);
            if (rewardSplit[1].contains("-")) {
                String[] split = rewardSplit[1].split("-");
                min = Integer.parseInt(split[0]);
                max = Integer.parseInt(split[1]);
            } else {
                min = Integer.parseInt(rewardSplit[1]);
                max = min;
            }
        }

        if (Settings.UPGRADE_BY_SMELTING.getBoolean()
                && needed == 0
                && plugin.getLevelManager().getLevel(level.getLevel() + 1) != null) {
            this.toLevel.remove(material);
            levelUp();
        }

        this.updateCook();

        FurnaceInventory inventory = (FurnaceInventory) ((InventoryHolder) block.getState()).getInventory();

        if (event.getSource().getType().name().contains("SPONGE") || event.getSource().getType().name().contains("COBBLESTONE") || event.getSource().getType().name().contains("DEEPSLATE"))
            return;

        int num = Integer.parseInt(reward);
        double rand = Math.random() * 100;
        if (rand >= num
                || event.getResult().equals(Material.SPONGE)
                || Settings.NO_REWARDS_FROM_RECIPES.getBoolean()
                && plugin.getFurnaceRecipeFile().contains("Recipes." + inventory.getSmelting().getType().toString())) {
            return;
        }

        int randomAmount = min == max ? min : (int) (Math.random() * ((max - min) + 1)) + min;

        BoostData boostData = plugin.getBoostManager().getBoost(placedBy);
        randomAmount = randomAmount * (boostData == null ? 1 : boostData.getMultiplier());

        event.getResult().setAmount(Math.min(event.getResult().getAmount() + randomAmount, event.getResult().getMaxStackSize()));
    }

    public void upgrade(Player player, CostType type) {
        if (!plugin.getLevelManager().getLevels().containsKey(this.level.getLevel() + 1)) return;

        Level level = plugin.getLevelManager().getLevel(this.level.getLevel() + 1);
        int cost = type == CostType.ECONOMY ? level.getCostEconomy() : level.getCostExperience();

        if (type == CostType.ECONOMY) {
            if (!EconomyManager.isEnabled()) {
                player.sendMessage("Economy not enabled.");
                return;
            }
            if (!EconomyManager.hasBalance(player, cost)) {
                plugin.getInstance().getLocale().getMessage("event.upgrade.cannotafford").sendPrefixedMessage(player);
                return;
            }
            EconomyManager.withdrawBalance(player, cost);
            upgradeFinal(player);
        } else if (type == CostType.EXPERIENCE) {
            if (player.getLevel() >= cost || player.getGameMode() == GameMode.CREATIVE) {
                if (player.getGameMode() != GameMode.CREATIVE) {
                    player.setLevel(player.getLevel() - cost);
                }
                upgradeFinal(player);
            } else {
                plugin.getLocale().getMessage("event.upgrade.cannotafford").sendPrefixedMessage(player);
            }
        }
    }

    private void upgradeFinal(Player player) {
        levelUp();
        syncName();
        plugin.getDataManager().queueFurnaceForUpdate(this);
        if (plugin.getLevelManager().getHighestLevel() != level) {
            plugin.getLocale().getMessage("event.upgrade.success")
                    .processPlaceholder("level", level.getLevel()).sendPrefixedMessage(player);

        } else {
            plugin.getLocale().getMessage("event.upgrade.maxed")
                    .processPlaceholder("level", level.getLevel()).sendPrefixedMessage(player);
        }
        Location loc = location.clone().add(.5, .5, .5);

        if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_12)) return;

        player.getWorld().spawnParticle(org.bukkit.Particle.valueOf(plugin.getConfig().getString("Main.Upgrade Particle Type")), loc, 200, .5, .5, .5);
        
        final Location playerLocation = player.getLocation();
        if (plugin.getLevelManager().getHighestLevel() != level) {
            player.playSound(playerLocation, Sound.ENTITY_PLAYER_LEVELUP, 0.6F, 15.0F);
        } else {
            player.playSound(playerLocation, Sound.ENTITY_PLAYER_LEVELUP, 2F, 25.0F);

            if (!ServerVersion.isServerVersionAtLeast(ServerVersion.V1_13)) return;

            player.playSound(playerLocation, Sound.BLOCK_NOTE_BLOCK_CHIME, 2F, 25.0F);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.playSound(playerLocation, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.2F, 35.0F), 5L);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.playSound(playerLocation, Sound.BLOCK_NOTE_BLOCK_CHIME, 1.8F, 35.0F), 10L);
        }
    }

    public void levelUp() {
        level = plugin.getLevelManager().getLevel(this.level.getLevel() + 1);
    }

    private void syncName() {
        org.bukkit.block.Furnace furnace = (org.bukkit.block.Furnace) location.getBlock().getState();
        if (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_10))
            furnace.setCustomName(Methods.formatName(level.getLevel()));
        furnace.update(true);
    }

    public void updateCook() {
        Block block = location.getBlock();
        if (!block.getType().name().contains("FURNACE") && !block.getType().name().contains("SMOKER")) return;

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            int num = getPerformanceTotal(block.getType());

            int max = (block.getType().name().contains("BLAST") || block.getType().name().contains("SMOKER") ? 100 : 200);
            if (num >= max)
                num = max - 1;

            if (num != 0) {
                BlockState bs = (block.getState());
                ((org.bukkit.block.Furnace) bs).setCookTime(Short.parseShort(Integer.toString(num)));
                bs.update();
            }
        }, 1L);
    }


    public Level getLevel() {
        return level;
    }


    public List<UUID> getAccessList() {
        return Collections.unmodifiableList(accessList);
    }

    public int getPerformanceTotal(Material material) {
        String cap = (material.name().contains("BLAST") || material.name().contains("SMOKER") ? "100" : "200");
        String equation = "(" + level.getPerformance() + " / 100) * " + cap;
        return (int) MathUtils.eval(equation);
    }

    public boolean addToAccessList(OfflinePlayer player) {
        return addToAccessList(player.getUniqueId());
    }

    public boolean addToAccessList(UUID uuid) {
        return accessList.add(uuid);
    }

    public boolean removeFromAccessList(UUID uuid) {
        return accessList.remove(uuid);
    }

    public boolean isOnAccessList(OfflinePlayer player) {
        return accessList.contains(player.getUniqueId());
    }

    public void clearAccessList() {
        accessList.clear();
    }

    public List<Location> getRadius(boolean overHeat) {
        if (overHeat)
            return radiusOverheat.isEmpty() ? null : Collections.unmodifiableList(radiusOverheat);
        else
            return radiusFuelshare.isEmpty() ? null : Collections.unmodifiableList(radiusFuelshare);
    }


    public void addToRadius(Location location, boolean overHeat) {
        if (overHeat)
            radiusOverheat.add(location);
        else
            radiusFuelshare.add(location);
    }


    public void clearRadius(boolean overHeat) {
        if (overHeat)
            radiusOverheat.clear();
        else
            radiusFuelshare.clear();
    }


    public int getRadiusLast(boolean overHeat) {
        if (overHeat)
            return radiusOverheatLast;
        else
            return radiusFuelshareLast;
    }


    public void setRadiusLast(int radiusLast, boolean overHeat) {
        if (overHeat)
            this.radiusOverheatLast = radiusLast;
        else
            this.radiusFuelshareLast = radiusLast;
    }

    public Location getLocation() {
        return location.clone();
    }

    public boolean isInLoadedChunk() {
        return location != null && location.getWorld() != null && location.getWorld().isChunkLoaded(((int) location.getX()) >> 4, ((int) location.getZ()) >> 4);
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public UUID getPlacedBy() {
        return placedBy;
    }

    public void setPlacedBy(UUID placedBy) {
        this.placedBy = placedBy;
    }

    public int getUses() {
        return uses;
    }

    public void setUses(int uses) {
        this.uses = uses;
    }

    public int getToLevel(CompatibleMaterial material) {
        if (!this.toLevel.containsKey(material))
            return 0;
        return this.toLevel.get(material);
    }

    public Map<CompatibleMaterial, Integer> getToLevel() {
        return Collections.unmodifiableMap(toLevel);
    }

    public int addToLevel(CompatibleMaterial material, int amount) {
        if (this.toLevel.containsKey(material)) {
            int newAmount = this.toLevel.get(material) + amount;
            this.toLevel.put(material, newAmount);
            return newAmount;
        }

        this.toLevel.put(material, amount);
        return amount;
    }

    public int getRadiusOverheatLast() {
        return radiusOverheatLast;
    }

    public void setRadiusOverheatLast(int radiusOverheatLast) {
        this.radiusOverheatLast = radiusOverheatLast;
    }

    public int getRadiusFuelshareLast() {
        return radiusFuelshareLast;
    }

    public void setRadiusFuelshareLast(int radiusFuelshareLast) {
        this.radiusFuelshareLast = radiusFuelshareLast;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void dropItems() {
        FurnaceInventory inventory = (FurnaceInventory) ((InventoryHolder) location.getBlock().getState()).getInventory();
        ItemStack fuel = inventory.getFuel();
        ItemStack smelting = inventory.getSmelting();
        ItemStack result = inventory.getResult();

        World world = location.getWorld();
        if (world == null) return;

        if (fuel != null)
            world.dropItemNaturally(location, fuel);
        if (smelting != null)
            world.dropItemNaturally(location, smelting);
        if (result != null)
            world.dropItemNaturally(location, result);
    }

    public String getHologramId() {
        return this.hologramId;
    }
}
