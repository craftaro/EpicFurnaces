package com.songoda.epicfurnaces.objects;

import com.songoda.epicfurnaces.EpicFurnaces;
import com.songoda.epicfurnaces.menus.OverviewMenu;
import com.songoda.epicfurnaces.utils.NMSUtil;
import com.songoda.epicfurnaces.utils.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.InventoryHolder;

import java.lang.reflect.Method;
import java.util.*;

import static com.songoda.epicfurnaces.objects.FurnaceObject.BoostType.*;

/**
 * Created by songoda on 3/7/2017.
 */
public class FurnaceObject {

    private final EpicFurnaces instance;
    private Location location;
    private Level level;
    private String nickname;
    private UUID placedBy;
    private int uses, toLevel, radiusOverheatLast, radiusFuelShareLast;
    private List<Location> radiusOverheat = new ArrayList<>();
    private List<Location> radiusFuelShare = new ArrayList<>();
    private List<String> accessList;

    public FurnaceObject(EpicFurnaces instance, Location location, Level level, String nickname, int uses, int toLevel, List<String> accessList, UUID placedBy) {
        this.instance = instance;
        this.location = location;
        this.level = level;
        this.uses = uses;
        this.toLevel = toLevel;
        this.nickname = nickname;
        this.placedBy = placedBy;
        this.accessList = accessList;
        this.syncName();
    }

    public void openOverview(Player player) {
        if (!player.hasPermission("epicfurnaces.overview")) {
            return;
        }

        new OverviewMenu(instance, this, player).open(player);
    }

    public void plus(FurnaceSmeltEvent e) {
        Block block = location.getBlock();
        if (block.getType() != Material.FURNACE && block.getType() != instance.getBukkitEnums().getMaterial("BURNING_FURNACE").getType()) {
            return;
        }

        this.uses++;
        this.toLevel++;

        int multi = instance.getConfig().getInt("Main.Level Cost Multiplier");

        if (level.getReward() == null) return;

        String reward = level.getReward();
        String[] amt = {"1", "1"};
        if (reward.contains(":")) {
            String[] rewardSplit = reward.split(":");
            reward = rewardSplit[0].substring(0, rewardSplit[0].length() - 1);
            if (rewardSplit[1].contains("-"))
                amt = rewardSplit[1].split("-");
            else {
                amt[0] = rewardSplit[1];
                amt[1] = rewardSplit[0];
            }
        }


        int needed = ((multi * level.getLevel()) - toLevel) - 1;

        if (instance.getConfig().getBoolean("Main.Upgrade By Smelting Materials")
                && needed <= 0
                && instance.getConfig().contains("settings.levels.Level-" + (level.getLevel() + 1))) {
            toLevel = 0;
            level = instance.getLevelManager().getLevel(this.level.getLevel() + 1);
        }

        updateCook();

        FurnaceInventory inventory = (FurnaceInventory) ((InventoryHolder) block.getState()).getInventory();

        int num = Integer.parseInt(reward);
        double rand = Math.random() * 100;
        if (rand >= num
                || e.getResult().getType().equals(Material.SPONGE)
                || instance.getConfig().getBoolean("Main.No Rewards From Custom Recipes")
                && instance.getConfiguration("Furnace Recipes").contains("Recipes." + inventory.getSmelting().getType().toString())) {
            return;
        }

        int r = Integer.parseInt(amt[0]);
        if (Integer.parseInt(amt[0]) !=
                Integer.parseInt(amt[1].replace("%", "")))
            r = (int) (Math.random() * ((Integer.parseInt(amt[1].replace("%", "")) - Integer.parseInt(amt[0])))) + Integer.parseInt(amt[0]);

        BoostData boostData = instance.getBoostManager().getBoost(placedBy);
        r = r * (boostData == null ? 1 : boostData.getMultiplier());

        if (e.getResult() == null) {
            return;
        }

        e.getResult().setAmount(e.getResult().getAmount() + r);
    }

    public void upgrade(String type, Player player) {
        if (!instance.getLevelManager().getLevels().containsKey(this.level.getLevel() + 1))
            return;

        int cost = type.equals("XP") ? level.getCostExperience() : level.getCostEconomy();
        Level level = instance.getLevelManager().getLevel(this.level.getLevel() + 1);

        if (type.equals("ECO")) {
            if (instance.getEconomy().has(player, cost)) {
                instance.getEconomy().withdrawPlayer(player, cost);
                upgradeFinal(level, player);
                return;
            }
            player.sendMessage(instance.getLocale().getMessage("event.upgrade.cannotafford"));
            return;
        }

        if (player.getLevel() >= cost || player.getGameMode() == GameMode.CREATIVE) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.setLevel(player.getLevel() - cost);
            }
            upgradeFinal(level, player);
            return;
        }

        player.sendMessage(instance.getLocale().getMessage("event.upgrade.cannotafford"));
    }

    private void upgradeFinal(Level level, Player player) {
        this.level = level;
        syncName();
        if (instance.getLevelManager().getHighestLevel() != level) {
            player.sendMessage(instance.getLocale().getMessage("event.upgrade.success", level.getLevel()));
        } else {
            player.sendMessage(instance.getLocale().getMessage("event.upgrade.maxed", level.getLevel()));
        }

        Location loc = location.clone().add(.5, .5, .5);
        instance.getCraftBukkitHook().broadcastParticle(loc, instance.getConfig().getString("Main.Upgrade Particle Type"), 200);

        if (instance.getConfig().getBoolean("Main.Sounds Enabled")) {
            if (instance.getLevelManager().getHighestLevel() == level) {
                player.playSound(player.getLocation(), instance.getBukkitEnums().getSound("ENTITY_PLAYER_LEVELUP"), 0.6F, 15.0F);
            } else {
                player.playSound(player.getLocation(), instance.getBukkitEnums().getSound("ENTITY_PLAYER_LEVELUP"), 2F, 25.0F);
                player.playSound(player.getLocation(), instance.getBukkitEnums().getSound("BLOCK_NOTE_BLOCK_CHIME"), 2F, 25.0F);
                Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> player.playSound(player.getLocation(), instance.getBukkitEnums().getSound("BLOCK_NOTE_BLOCK_CHIME"), 1.2F, 35.0F), 5L);
                Bukkit.getScheduler().scheduleSyncDelayedTask(instance, () -> player.playSound(player.getLocation(), instance.getBukkitEnums().getSound("BLOCK_NOTE_BLOCK_CHIME"), 1.8F, 35.0F), 10L);
            }
        }
    }

    private void syncName() {
        if (location.getBlock().getType() != Material.FURNACE && location.getBlock().getType() != instance.getBukkitEnums().getMaterial("BURNING_FURNACE").getType()) {
            return;
        }

        String name = StringUtils.formatName(level.getLevel(), uses, false);

        try {
            Furnace craftFurnace = (Furnace) location.getBlock().getState();
            craftFurnace.setCustomName(name);
            craftFurnace.update(true);
        } catch (Exception | Error e) {
            try {
                Object craftFurnace = NMSUtil.getCraftClass("block.CraftFurnace").cast(location.getBlock().getState());
                Method getTileEntity = craftFurnace.getClass().getDeclaredMethod("getTileEntity");
                Object tileEntity = getTileEntity.invoke(craftFurnace);
                Method a = tileEntity.getClass().getDeclaredMethod("a", String.class);
                a.invoke(tileEntity, name);
            } catch (Exception | Error ignore) {
            }
        } finally {
            location.getBlock().getState().update(true);
        }
    }

    public void updateCook() {
        Block block = location.getBlock();
        if (block == null || (block.getType() != Material.FURNACE && block.getType() != instance.getBukkitEnums().getMaterial("BURNING_FURNACE").getType())) {
            return;
        }
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(instance, () -> {
            int num = getPerformanceTotal();

            if (num > 200) {
                num = 200;
            }

            if (num != 0) {
                BlockState bs = (block.getState()); // max is 200
                ((Furnace) bs).setCookTime(Short.parseShort(Integer.toString(num)));
                bs.update();
            }
        }, 1L);

    }


    public boolean addToAccessList(String string) {
        return accessList.add(string);
    }


    public boolean removeFromAccessList(String string) {
        return accessList.remove(string);
    }


    public void clearAccessList() {
        accessList.clear();
    }


    public List<Location> getRadius(BoostType boostType) {
        if (boostType == OVERHEAT) {
            return radiusOverheat.isEmpty() ? null : Collections.unmodifiableList(radiusOverheat);
        } else if (boostType == FUEL_SHARE) {
            return radiusFuelShare.isEmpty() ? null : Collections.unmodifiableList(radiusFuelShare);
        }

        return null;
    }


    public void addToRadius(Location location, BoostType boostType) {
        if (boostType == OVERHEAT) {
            radiusOverheat.add(location);
        } else if (boostType == FUEL_SHARE) {
            radiusFuelShare.add(location);
        }
    }


    public void clearRadius(BoostType boostType) {
        if (boostType == OVERHEAT) {
            radiusOverheat.clear();
        } else if (boostType == FUEL_SHARE) {
            radiusFuelShare.clear();
        }
    }


    public int getRadiusLast(BoostType boostType) {
        if (boostType == OVERHEAT) {
            return radiusOverheatLast;
        } else if (boostType == FUEL_SHARE) {
            return radiusFuelShareLast;
        }

        return 0;
    }


    public void setRadiusLast(int radiusLast, BoostType boostType) {
        if (boostType == OVERHEAT) {
            this.radiusOverheatLast = radiusLast;
        } else if (boostType == FUEL_SHARE) {
            this.radiusFuelShareLast = radiusLast;
        }
    }


    public List<UUID> getAccessList() {
        List<UUID> list = new ArrayList<>();
        for (String line : accessList) {
            String[] halfs = line.split(":");
            list.add(UUID.fromString(halfs[0]));
        }

        return list;
    }


    public Level getLevel() {
        return level;
    }


    public Location getLocation() {
        return location.clone();
    }


    public String getNickname() {
        return nickname;
    }


    public void setNickname(String nickname) {
        this.nickname = nickname;
    }


    public List<String> getOriginalAccessList() {
        return Collections.unmodifiableList(accessList);
    }


    public int getPerformanceTotal() {
        return (int) Math.round((level.getPerformance() / 100.0) * 200);
    }


    public UUID getPlacedBy() {
        return placedBy;
    }


    public int getToLevel() {
        return toLevel;
    }


    public int getUses() {
        return uses;
    }

    public enum BoostType {
        OVERHEAT, FUEL_SHARE
    }
}
