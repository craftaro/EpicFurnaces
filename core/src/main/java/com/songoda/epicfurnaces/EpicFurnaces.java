package com.songoda.epicfurnaces;

import com.songoda.epicfurnaces.command.CommandManager;
import com.songoda.epicfurnaces.handlers.BlacklistHandler;
import com.songoda.epicfurnaces.listeners.BlockListeners;
import com.songoda.epicfurnaces.listeners.FurnaceListeners;
import com.songoda.epicfurnaces.listeners.InteractListeners;
import com.songoda.epicfurnaces.listeners.InventoryListeners;
import com.songoda.epicfurnaces.managers.BoostManager;
import com.songoda.epicfurnaces.managers.FurnaceManager;
import com.songoda.epicfurnaces.managers.HologramManager;
import com.songoda.epicfurnaces.managers.LevelManager;
import com.songoda.epicfurnaces.storage.Storage;
import com.songoda.epicfurnaces.storage.types.StorageMysql;
import com.songoda.epicfurnaces.storage.types.StorageYaml;
import com.songoda.epicfurnaces.tasks.FurnaceTask;
import com.songoda.epicfurnaces.tasks.HologramTask;
import com.songoda.epicfurnaces.utils.BukkitEnums;
import com.songoda.epicfurnaces.utils.Methods;
import com.songoda.epicfurnaces.utils.StringUtils;
import com.songoda.epicfurnaces.utils.gui.FastInv;
import com.songoda.epicfurnaces.utils.updateModules.LocaleModule;
import com.songoda.update.Plugin;
import com.songoda.update.SongodaUpdate;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static com.songoda.epicfurnaces.utils.StringUtils.formatText;
import static java.util.Arrays.asList;

public class EpicFurnaces extends JavaPlugin {
    private static Map<String, FileConfiguration> configurations = new HashMap<>();
    private BlacklistHandler blacklistHandler;
    private BoostManager boostManager;
    private BukkitEnums bukkitEnums;
    private CommandManager commandManager;
    private FurnaceManager furnaceManager;
    private LevelManager levelManager;
    private Locale locale;
    private Storage storage;
    private HologramManager hologramManager;
    private Economy economy;
    private int currentVersion;

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage(formatText("&a============================="));
        Bukkit.getConsoleSender().sendMessage(formatText("&7EpicFurnaces " + this.getDescription().getVersion() + " by &5Songoda <3&7!"));
        Bukkit.getConsoleSender().sendMessage(formatText("&7Action: &aEnabling&7..."));

        if (!checkVersion()) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        for (String name : asList("config", "data", "hooks", "blacklist", "Furnace Recipes")) {
            File file = new File(getDataFolder(), name + ".yml");
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                saveResource(file.getName(), false);
            }
            FileConfiguration configuration = new YamlConfiguration();
            try {
                configuration.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
            configurations.put(name, configuration);
        }

        String langMode = getConfig().getString("System.Language Mode");
        Locale.init(this);
        Locale.saveDefaultLocale("en_US");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));


        //Running Songoda Updater
        Plugin plugin = new Plugin(this, 22);
        plugin.addModule(new LocaleModule());
        SongodaUpdate.load(plugin);

        FastInv.init(this);
        Methods.init(this);
        StringUtils.init(this);

        this.furnaceManager = new FurnaceManager(this);
        this.commandManager = new CommandManager(this);
        this.boostManager = new BoostManager(this);
        this.blacklistHandler = new BlacklistHandler(this);
        this.bukkitEnums = new BukkitEnums(this);
        this.levelManager = new LevelManager(this);

        if (!setupEconomy()) {
            getLogger().severe("Economy provider not found/not supported, disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            Bukkit.getConsoleSender().sendMessage(formatText("&a============================="));
            return;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays") && getConfig().getBoolean("Main.Furnaces Have Holograms")) {
            this.hologramManager = new HologramManager(this);
        }

        checkStorage();
        levelManager.loadLevelManager();
        setupRecipes();

        int timeout = getConfig().getInt("Main.Auto Save Interval In Seconds") * 60 * 20;

        Bukkit.getScheduler().runTaskLater(this, furnaceManager::loadFurnaces, 10);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> storage.doSave(), timeout, timeout);

        // Start Tasks
        HologramTask.startTask(this);
        FurnaceTask.startTask(this);

        // Register Listeners
        PluginManager pluginManager = Bukkit.getPluginManager();
        new HashSet<>(asList(
                new BlockListeners(this),
                new FurnaceListeners(this),
                new InteractListeners(this),
                new InventoryListeners(this))).forEach(listener -> pluginManager.registerEvents(listener, this));

        new Metrics(this);

        Bukkit.getConsoleSender().sendMessage(formatText("&a============================="));
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(formatText("&a============================="));
        Bukkit.getConsoleSender().sendMessage(formatText("&7EpicFurnaces " + this.getDescription().getVersion() + " by &5Songoda <3&7!"));
        Bukkit.getConsoleSender().sendMessage(formatText("&7Action: &cDisabling&7..."));
        Bukkit.getConsoleSender().sendMessage(formatText("&a============================="));
        getHologramManager().ifPresent(HologramManager::clearAll);
        this.hologramManager = null;
        storage.doSave();
        storage.closeConnection();

        Map<Inventory, Location> loadedFurnaceInventories = this.furnaceManager.getLoadedFurnaceInventories();
        for (Inventory inventory : loadedFurnaceInventories.keySet())
            loadedFurnaceInventories.get(inventory).getChunk().load();
        loadedFurnaceInventories.clear();
    }

    private void checkStorage() {
        if (getConfig().getBoolean("Database.Activate Mysql Support")) {
            this.storage = new StorageMysql(this);
        } else {
            this.storage = new StorageYaml(this);
        }
    }

    public void reload() {
        Bukkit.getScheduler().cancelTasks(this);
        reloadConfig();
        saveConfig();
        onDisable();
        onEnable();
    }

    private void setupRecipes() {
        if (!getConfig().getBoolean("Main.Use Custom Recipes")) {
            return;
        }

        ConfigurationSection cs = getConfiguration("Furnace Recipes").getConfigurationSection("Recipes");

        for (String key : cs.getKeys(false)) {
            Material item = Material.matchMaterial(key.toUpperCase());

            if (item == null) {
                getLogger().info("Invalid material from recipes files: " + key.toUpperCase());
                continue;
            }

            Material result = Material.matchMaterial(cs.getString(key.toUpperCase() + ".result"));

            if (result == null) {
                getLogger().info("Invalid material from recipes files: " + cs.getString(key.toUpperCase() + ".result"));
                continue;
            }

            int amount = cs.getInt(key.toUpperCase() + ".amount");
            getServer().addRecipe(new FurnaceRecipe(new ItemStack(result, amount), item));
        }

    }

    public void save(String configuration) {
        try {
            File configurationFile = new File(getDataFolder(), configuration + ".yml");
            getConfiguration(configuration).save(configurationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkVersion() {
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];
        currentVersion = Integer.parseInt(version.split("_")[1]);
        int workingVersion = 8;

        if (currentVersion < workingVersion) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                Bukkit.getConsoleSender().sendMessage("");
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You installed the 1." + workingVersion + "+ version of " +
                        this.getDescription().getName() + " on a 1." + currentVersion + " server. " +
                        "We currently do not support " + currentVersion + " and below.");
                Bukkit.getConsoleSender().sendMessage("");
            });
            return false;
        }

        return true;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }


    public FileConfiguration getConfiguration(String name) {
        return configurations.get(name);
    }

    public BlacklistHandler getBlacklistHandler() {
        return blacklistHandler;
    }

    public BoostManager getBoostManager() {
        return boostManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public FurnaceManager getFurnaceManager() {
        return furnaceManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public Locale getLocale() {
        return locale;
    }

    public BukkitEnums getBukkitEnums() {
        return bukkitEnums;
    }

    public Optional<HologramManager> getHologramManager() {
        return Optional.ofNullable(hologramManager);
    }

    public Storage getStorage() {
        return storage;
    }

    public Economy getEconomy() {
        return economy;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }
}