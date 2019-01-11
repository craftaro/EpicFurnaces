package com.songoda.epicfurnaces.utils;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.epicfurnaces.EpicFurnaces;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by songo on 6/4/2017.
 */
public class SettingsManager implements Listener {

    private static final Pattern SETTINGS_PATTERN = Pattern.compile("(.{1,28}(?:\\s|$))|(.{0,28})", Pattern.DOTALL);

    private static ConfigWrapper defs;
    private final EpicFurnaces instance;
    private String pluginName = "EpicFurnaces";
    private Map<Player, String> cat = new HashMap<>();
    private Map<Player, String> current = new HashMap<>();

    public SettingsManager(EpicFurnaces plugin) {
        this.instance = plugin;

        plugin.saveResource("SettingDefinitions.yml", true);
        defs = new ConfigWrapper(plugin, "", "SettingDefinitions.yml");
        defs.createNewFile("Loading data file", pluginName + " SettingDefinitions file");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();

        if (event.getInventory() != event.getWhoClicked().getOpenInventory().getTopInventory()
                || clickedItem == null || !clickedItem.hasItemMeta()
                || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        if (event.getInventory().getTitle().equals(pluginName + " Settings Manager")) {
            event.setCancelled(true);
            if (clickedItem.getType().name().contains("STAINED_GLASS")) return;

            String type = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            this.cat.put((Player) event.getWhoClicked(), type);
            this.openEditor((Player) event.getWhoClicked());
        } else if (event.getInventory().getTitle().equals(pluginName + " Settings Editor")) {
            event.setCancelled(true);
            if (clickedItem.getType().name().contains("STAINED_GLASS")) return;

            Player player = (Player) event.getWhoClicked();

            String key = cat.get(player) + "." + ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

            if (instance.getConfig().get(key).getClass().getName().equals("java.lang.Boolean")) {
                this.instance.getConfig().set(key, !instance.getConfig().getBoolean(key));
                this.finishEditing(player);
            } else {
                this.editObject(player, key);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!current.containsKey(player)) return;

        String value = current.get(player);
        FileConfiguration config = instance.getConfig();
        if (config.isInt(value)) {
            config.set(value, Integer.parseInt(event.getMessage()));
        } else if (config.isDouble(value)) {
            config.set(value, Double.parseDouble(event.getMessage()));
        } else if (config.isString(value)) {
            config.set(value, event.getMessage());
        }

        this.finishEditing(player);
        event.setCancelled(true);
    }

    public void finishEditing(Player player) {
        this.current.remove(player);
        this.instance.saveConfig();
        this.openEditor(player);
    }


    public void editObject(Player player, String current) {
        this.current.put(player, ChatColor.stripColor(current));

        player.closeInventory();
        player.sendMessage("");
        player.sendMessage(TextComponent.formatText("&7Please enter a value for &6" + current + "&7."));
        if (instance.getConfig().isInt(current) || instance.getConfig().isDouble(current)) {
            player.sendMessage(TextComponent.formatText("&cUse only numbers."));
        }
        player.sendMessage("");
    }

    public void openSettingsManager(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, pluginName + " Settings Manager");
        ItemStack glass = Methods.getGlass();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glass);
        }

        int slot = 10;
        for (String key : instance.getConfig().getDefaultSection().getKeys(false)) {
            ItemStack item = new ItemStack(instance.getBukkitEnums().getWool(), 1, (byte) (slot - 9)); //ToDo: Make this function as it was meant to.
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Collections.singletonList(TextComponent.formatText("&6Click To Edit This Category.")));
            meta.setDisplayName(TextComponent.formatText("&f&l" + key));
            item.setItemMeta(meta);
            inventory.setItem(slot, item);
            slot++;
        }

        player.openInventory(inventory);
    }

    public void openEditor(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, pluginName + " Settings Editor");
        FileConfiguration config = instance.getConfig();

        int slot = 0;
        for (String key : config.getConfigurationSection(cat.get(player)).getKeys(true)) {
            String fKey = cat.get(player) + "." + key;
            ItemStack item = new ItemStack(Material.DIAMOND_HELMET);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(TextComponent.formatText("&6" + key));

            List<String> lore = new ArrayList<>();
            if (config.isBoolean(fKey)) {
                item.setType(Material.LEVER);
                lore.add(TextComponent.formatText(config.getBoolean(fKey) ? "&atrue" : "&cfalse"));
            } else if (config.isString(fKey)) {
                item.setType(Material.PAPER);
                lore.add(TextComponent.formatText("&9" + config.getString(fKey)));
            } else if (config.isInt(fKey)) {
                item.setType(Material.COMPASS);
                lore.add(TextComponent.formatText("&5" + config.getInt(fKey)));
            }

            if (defs.getConfig().contains(fKey)) {
                String text = defs.getConfig().getString(key);

                Matcher m = SETTINGS_PATTERN.matcher(text);
                while (m.find()) {
                    if (m.end() != text.length() || m.group().length() != 0)
                        lore.add(TextComponent.formatText("&7" + m.group()));
                }
            }

            meta.setLore(lore);
            item.setItemMeta(meta);

            inventory.setItem(slot, item);
            slot++;
        }

        player.openInventory(inventory);
    }

    public void updateSettings() {
        FileConfiguration config = instance.getConfig();

        for (Setting setting : Setting.values()) {
            if (config.contains("settings." + setting.oldSetting)) {
                config.addDefault(setting.setting, instance.getConfig().get("settings." + setting.oldSetting));
                config.set("settings." + setting.oldSetting, null);
            } else {
                config.addDefault(setting.setting, setting.option);
            }
        }
    }

    public enum Setting {

        SOUNDS_ENABLED("Sounds", "Main.Sounds Enabled", true),

        o3("Upgrade-with-material", "Main.Upgrade By Smelting Materials", true),
        UPGRADE_WITH_ECO_ENABLED("Upgrade-with-eco", "Main.Upgrade With Economy", true),
        UPGRADE_WITH_XP_ENABLED("Upgrade-with-xp", "Main.Upgrade With XP", true),

        o6("Turbo-level-multiplier", "Main.Level Cost Multiplier", 50),

        o102("Remember-furnace-Levels", "Main.Remember Furnace Item Levels", true),

        owqr("-", "Main.Furnaces Have Holograms", true),

        o324("Redstone-Deactivate", "Main.Redstone Deactivates Furnaces", true),

        o11("furnace-upgrade-cost", "Main.Furnace Upgrade Cost", "IRON_INGOT"),
        o12("Custom-recipes", "Main.Use Custom Recipes", true),
        o13("Ignore-custom-recipes-for-rewards", "Main.No Rewards From Custom Recipes", true),

        UPGRADE_PARTICLE_TYPE("Upgrade-particle-Type", "Main.Upgrade Particle Type", "SPELL_WITCH"),

        o18("Remote-Furnaces", "Main.Access Furnaces Remotely", true),

        o543("-", "Main.Furnace Tick Speed", 10),
        o5423("-", "Main.Auto Save Interval In Seconds", 15),
        o54("-", "Main.Overheat Particles", true),

        o14("Reward-Icon", "Interfaces.Reward Icon", "GOLDEN_APPLE"),
        o15("Performance-Icon", "Interfaces.Performance Icon", "REDSTONE"),
        o16("FuelDuration-Icon", "Interfaces.FuelShare Icon", "COAL_BLOCK"),
        o316("FuelDuration-Icon", "Interfaces.FuelDuration Icon", "COAL"),
        o162("-", "Interfaces.Overheat Icon", "FIRE_CHARGE"),
        ECO_ICON("ECO-Icon", "Interfaces.Economy Icon", "SUNFLOWER"),
        XP_ICON("XP-Icon", "Interfaces.XP Icon", "EXPERIENCE_BOTTLE"),
        GLASS_TYPE_1("Glass-Type-1", "Interfaces.Glass Type 1", 7),
        GLASS_TYPE_2("Glass-Type-2", "Interfaces.Glass Type 2", 11),
        GLASS_TYPE_3("Glass-Type-3", "Interfaces.Glass Type 3", 3),
        RAINBOW_GLASS("Rainbow-Glass", "Interfaces.Replace Glass Type 1 With Rainbow Glass", false),

        DATABASE_SUPPORT("-", "Database.Activate Mysql Support", false),
        DATABASE_IP("-", "Database.IP", "127.0.0.1"),
        DATABASE_PORT("-", "Database.Port", 3306),
        DATABASE_NAME("-", "Database.Database Name", "EpicFurnaces"),
        DATABASE_PREFIX("-", "Database.Prefix", "EF-"),
        DATABASE_USERNAME("-", "Database.Username", "PUT_USERNAME_HERE"),
        DATABASE_PASSWORD("-", "Database.Password", "PUT_PASSWORD_HERE"),

        DOWNLOAD_FILES("-", "System.Download Needed Data Files", true),
        LANGUAGE_MODE("-", "System.Language Mode", "en_US"),
        DEBUG_MODE("Debug-Mode", "System.Debugger Enabled", false);

        private final String setting, oldSetting;
        private final Object option;

        Setting(String oldSetting, String setting, Object option) {
            this.oldSetting = oldSetting;
            this.setting = setting;
            this.option = option;
        }

    }
}
