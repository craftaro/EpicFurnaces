package com.songoda.epicfurnaces;

import com.gb6.songoda.epicfurnaces.hooks.PlotSquaredHook;
import com.songoda.epicfurnaces.command.CommandManager;
import com.songoda.epicfurnaces.handlers.BlacklistHandler;
import com.songoda.epicfurnaces.hook.CraftBukkitHook;
import com.songoda.epicfurnaces.hooks.*;
import com.songoda.epicfurnaces.listeners.*;
import com.songoda.epicfurnaces.managers.*;
import com.songoda.epicfurnaces.storage.Storage;
import com.songoda.epicfurnaces.storage.types.StorageMysql;
import com.songoda.epicfurnaces.storage.types.StorageYaml;
import com.songoda.epicfurnaces.tasks.FurnaceTask;
import com.songoda.epicfurnaces.tasks.HologramTask;
import com.songoda.epicfurnaces.utils.BukkitEnums;
import com.songoda.epicfurnaces.utils.Methods;
import com.songoda.epicfurnaces.utils.StringUtils;
import com.songoda.epicfurnaces.utils.gui.FastInv;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import static com.songoda.epicfurnaces.utils.StringUtils.formatText;
import static java.util.Arrays.asList;

public class EpicFurnaces extends JavaPlugin {
    private static Map<String, FileConfiguration> configurations = new HashMap<>();
    private BlacklistHandler blacklistHandler;
    private BoostManager boostManager;
    private BukkitEnums bukkitEnums;
    private CommandManager commandManager;
    private FurnaceManager furnaceManager;
    private HookManager hookManager;
    private LevelManager levelManager;
    private Locale locale;
    private Storage storage;
    private HologramManager hologramManager;
    private Economy economy;
    private CraftBukkitHook craftBukkitHook;
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

        if (getConfig().getBoolean("System.Download Needed Data Files")) {
            this.update();
        }

        FastInv.init(this);
        Methods.init(this);
        StringUtils.init(this);

        this.furnaceManager = new FurnaceManager(this);
        this.commandManager = new CommandManager(this);
        this.boostManager = new BoostManager(this);
        this.blacklistHandler = new BlacklistHandler(this);
        this.bukkitEnums = new BukkitEnums(this);
        this.levelManager = new LevelManager(this);
        this.hookManager = new HookManager(this);
        this.economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();

        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays") && getConfig().getBoolean("Main.Furnaces Have Holograms")) {
            this.hologramManager = new HologramManager(this);
        }

        checkStorage();
        levelManager.loadLevelManager();
        setupRecipes();

        int timeout = getConfig().getInt("Main.Auto Save Interval In Seconds") * 60 * 20;

        Bukkit.getScheduler().runTaskLater(this, furnaceManager::loadFurnaces, 10);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveToFile, timeout, timeout);

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

        // Register default hooks
        if (pluginManager.isPluginEnabled("ASkyBlock")) hookManager.register(ASkyBlockHook::new);
        if (pluginManager.isPluginEnabled("FactionsFramework")) hookManager.register(FactionsHook::new);
        if (pluginManager.isPluginEnabled("GriefPrevention")) hookManager.register(GriefPreventionHook::new);
        if (pluginManager.isPluginEnabled("Kingdoms")) hookManager.register(KingdomsHook::new);
        if (pluginManager.isPluginEnabled("PlotSquared")) hookManager.register(PlotSquaredHook::new);
        if (pluginManager.isPluginEnabled("RedProtect")) hookManager.register(RedProtectHook::new);
        if (pluginManager.isPluginEnabled("Towny")) hookManager.register(TownyHook::new);
        if (pluginManager.isPluginEnabled("USkyBlock")) hookManager.register(USkyBlockHook::new);

        if (pluginManager.isPluginEnabled("WorldGuard")) {
            if (currentVersion >= 13) {
                hookManager.register(WorldGuard7Hook::new);
            } else {
                hookManager.register(WorldGuard6Hook::new);
            }
        }

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
        saveToFile();
    }

    private void checkStorage() {
        if (getConfig().getBoolean("Database.Activate Mysql Support")) {
            this.storage = new StorageMysql(this);
        } else {
            this.storage = new StorageYaml(this);
        }
    }

    private void saveToFile() {
        this.storage.closeConnection();
        checkStorage();
        furnaceManager.saveToFile();
        boostManager.saveToFile();
        storage.doSave();
    }

    private void update() {
        try {
            URL url = new URL("http://update.songoda.com/index.php?plugin=" + getDescription().getName() + "&version=" + getDescription().getVersion());
            URLConnection urlConnection = url.openConnection();
            InputStream is = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            int numCharsRead;
            char[] charArray = new char[1024];
            StringBuilder sb = new StringBuilder();
            while ((numCharsRead = isr.read(charArray)) > 0) {
                sb.append(charArray, 0, numCharsRead);
            }
            String jsonString = sb.toString();
            JSONObject json = (JSONObject) new JSONParser().parse(jsonString);

            JSONArray files = (JSONArray) json.get("neededFiles");
            for (Object o : files) {
                JSONObject file = (JSONObject) o;

                if ("locale".equals(file.get("type"))) {
                    InputStream in = new URL((String) file.get("link")).openStream();
                    Locale.saveDefaultLocale(in, (String) file.get("name"));
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to update.");
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
        craftBukkitHook = currentVersion >= 13 ? new CraftBukkitHook113() : new CraftBukkitHook18();
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

    public HookManager getHookManager() {
        return hookManager;
    }

    public Storage getStorage() {
        return storage;
    }

    public Economy getEconomy() {
        return economy;
    }

    public CraftBukkitHook getCraftBukkitHook() {
        return craftBukkitHook;
    }
}