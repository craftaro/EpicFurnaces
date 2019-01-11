package com.songoda.epicfurnaces;

import com.gb6.songoda.epicfurnaces.hooks.PlotSquaredHook;
import com.google.common.base.Preconditions;
import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.arconix.api.methods.serialize.Serialize;
import com.songoda.arconix.api.utils.ConfigWrapper;
import com.songoda.arconix.plugin.Arconix;
import com.songoda.epicfurnaces.boost.BoostData;
import com.songoda.epicfurnaces.boost.BoostManager;
import com.songoda.epicfurnaces.command.CommandManager;
import com.songoda.epicfurnaces.furnace.FurnaceManager;
import com.songoda.epicfurnaces.furnace.FurnaceObject;
import com.songoda.epicfurnaces.furnace.LevelManager;
import com.songoda.epicfurnaces.handlers.BlacklistHandler;
import com.songoda.epicfurnaces.hook.CraftBukkitHook;
import com.songoda.epicfurnaces.hook.ProtectionPluginHook;
import com.songoda.epicfurnaces.hooks.*;
import com.songoda.epicfurnaces.listeners.BlockListeners;
import com.songoda.epicfurnaces.listeners.FurnaceListeners;
import com.songoda.epicfurnaces.listeners.InteractListeners;
import com.songoda.epicfurnaces.listeners.InventoryListeners;
import com.songoda.epicfurnaces.storage.Storage;
import com.songoda.epicfurnaces.storage.StorageItem;
import com.songoda.epicfurnaces.storage.StorageRow;
import com.songoda.epicfurnaces.storage.types.StorageMysql;
import com.songoda.epicfurnaces.storage.types.StorageYaml;
import com.songoda.epicfurnaces.tasks.FurnaceTask;
import com.songoda.epicfurnaces.tasks.HologramTask;
import com.songoda.epicfurnaces.utils.BukkitEnums;
import com.songoda.epicfurnaces.utils.Debugger;
import com.songoda.epicfurnaces.utils.Methods;
import com.songoda.epicfurnaces.utils.SettingsManager;
import com.songoda.epicfurnaces.utils.gui.FastInv;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class EpicFurnaces extends JavaPlugin {
    private static CommandSender console = Bukkit.getConsoleSender();
    private static EpicFurnaces instance;
    private BlacklistHandler blacklistHandler;
    private BoostManager boostManager;
    private BukkitEnums bukkitEnums;
    private CommandManager commandManager;
    private CraftBukkitHook craftBukkitHook;
    private int currentVersion;
    private ConfigWrapper dataFile = new ConfigWrapper(this, "", "data.yml");
    private FurnaceManager furnaceManager;
    private ConfigWrapper furnaceRecipeFile;
    private HologramTask hologramTask;
    private ConfigWrapper hooksFile = new ConfigWrapper(this, "", "hooks.yml");
    private ConfigWrapper langFile = new ConfigWrapper(this, "", "lang.yml");
    private LevelManager levelManager;
    private Locale locale;
    private List<ProtectionPluginHook> protectionHooks = new ArrayList<>();
    private References references = null;
    private SettingsManager settingsManager;
    private Storage storage;

    public static EpicFurnaces getInstance() {
        return instance;
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

        switch (version) {
            case "v1_8_R1":
                craftBukkitHook = new CraftBukkit18R1();
                break;
            case "v1_8_R2":
                craftBukkitHook = new CraftBukkit18R2();
                break;
            case "v1_8_R3":
                craftBukkitHook = new CraftBukkit18R3();
                break;
            default:
                craftBukkitHook = new CraftBukkit19();
                break;
        }

        File config = new File(getDataFolder(), "Furnace Recipes.yml");
        if (!config.exists()) {
            saveResource("Furnace Recipes.yml", false);
        }

        if (currentVersion < 13) {
            getLogger().info("Converting recipes to fit server version...");

            Charset charset = StandardCharsets.UTF_8;
            Path path = Paths.get(config.getAbsolutePath());

            try {
                String content = new String(Files.readAllBytes(path), charset);
                content = content.replaceAll("GOLDEN", "GOLD")
                        .replaceAll("SHOVEL", "SPADE")
                        .replaceAll("WOODEN", "WOOD")
                        .replaceAll("CLOCK", "WATCH");
                Files.write(path, content.getBytes(charset));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void onEnable() {
        if (!checkVersion()) {
            getPluginLoader().disablePlugin(this);
            return;
        }

        instance = this;
        console.sendMessage(TextComponent.formatText("&a============================="));
        console.sendMessage(TextComponent.formatText("&7EpicFurnaces " + this.getDescription().getVersion() + " by &5Brianna <3&7!"));
        console.sendMessage(TextComponent.formatText("&7Action: &aEnabling&7..."));
        settingsManager = new SettingsManager(this);
        setupConfig();
        dataFile.createNewFile("Loading data file", "EpicFurnaces data file");
        langFile.createNewFile("Loading language file", "EpicFurnaces language file");
        loadDataFile();

        String langMode = getConfig().getString("System.Language Mode");
        Locale.init(this);
        Locale.saveDefaultLocale("en_US");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));

        if (getConfig().getBoolean("System.Download Needed Data Files")) {
            this.update();
        }

        loadLevelManager();
        FastInv.init(this);

        this.furnaceManager = new FurnaceManager();
        this.commandManager = new CommandManager(this);
        this.boostManager = new BoostManager();
        this.blacklistHandler = new BlacklistHandler();
        this.bukkitEnums = new BukkitEnums(this);

        this.checkStorage();

        /*
         * Register furnaces into FurnaceManger from configuration
         */
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (storage.containsGroup("charged")) {
                for (StorageRow row : storage.getRowsByGroup("charged")) {
                    Location location = Serialize.getInstance().unserializeLocation(row.getKey());
                    if (location == null || location.getBlock() == null) return;

                    int level = row.get("level").asInt();
                    int uses = row.get("uses").asInt();
                    int toLevel = row.get("level").asInt();
                    String nickname = row.get("nickname").asString();

                    List<String> accessList = row.get("accesslist").asStringList();
                    String placedByStr = row.get("placedBy").asString();
                    UUID placedBy = placedByStr == null ? null : UUID.fromString(placedByStr);

                    FurnaceObject furnace = new FurnaceObject(location, levelManager.getLevel(level), nickname, uses, toLevel, accessList, placedBy);
                    furnaceManager.addFurnace(location, furnace);
                }

            }

            // Adding in Boosts
            if (storage.containsGroup("boosts")) {
                for (StorageRow row : storage.getRowsByGroup("boosts")) {
                    if (row.getItems().get("uuid").asObject() != null)
                        continue;

                    BoostData boostData = new BoostData(
                            row.get("amount").asInt(),
                            Long.parseLong(row.getKey()),
                            UUID.fromString(row.get("uuid").asString()));

                    this.boostManager.addBoostToPlayer(boostData);
                }
            }

        }, 10);

        setupRecipes();
        references = new References();

        int timeout = getConfig().getInt("Main.Auto Save Interval In Seconds") * 60 * 20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::saveToFile, timeout, timeout);

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Start Tasks
        this.hologramTask = HologramTask.startTask(this);
        FurnaceTask.startTask(this);

        // Register Listeners
        pluginManager.registerEvents(new BlockListeners(this), this);
        pluginManager.registerEvents(new FurnaceListeners(this), this);
        pluginManager.registerEvents(new InteractListeners(this), this);
        pluginManager.registerEvents(new InventoryListeners(this), this);

        // Register default hooks
        if (pluginManager.isPluginEnabled("ASkyBlock")) this.register(ASkyBlockHook::new);
        if (pluginManager.isPluginEnabled("FactionsFramework")) this.register(FactionsHook::new);
        if (pluginManager.isPluginEnabled("GriefPrevention")) this.register(GriefPreventionHook::new);
        if (pluginManager.isPluginEnabled("Kingdoms")) this.register(KingdomsHook::new);
        if (pluginManager.isPluginEnabled("PlotSquared")) this.register(PlotSquaredHook::new);
        if (pluginManager.isPluginEnabled("RedProtect")) this.register(RedProtectHook::new);
        if (pluginManager.isPluginEnabled("Towny")) this.register(TownyHook::new);
        if (pluginManager.isPluginEnabled("USkyBlock")) this.register(USkyBlockHook::new);
        //        if (pluginManager.isPluginEnabled("WorldGuard")) this.register(WorldGuardHook::new);

        console.sendMessage(TextComponent.formatText("&a============================="));
    }

    private void loadLevelManager() {
        // Load an instance of LevelManager
        levelManager = new LevelManager();
        /*
         * Register Levels into LevelManager from configuration.
         */
        levelManager.clear();
        for (String levelName : getConfig().getConfigurationSection("settings.levels").getKeys(false)) {
            int level = Integer.valueOf(levelName.split("-")[1]);
            int costExperiance = getConfig().getInt("settings.levels." + levelName + ".Cost-xp");
            int costEconomy = getConfig().getInt("settings.levels." + levelName + ".Cost-eco");

            String performanceStr = getConfig().getString("settings.levels." + levelName + ".Performance");
            int performance = performanceStr == null ? 0 : Integer.parseInt(performanceStr.substring(0, performanceStr.length() - 1));

            String reward = getConfig().getString("settings.levels." + levelName + ".Reward");

            String fuelDurationStr = getConfig().getString("settings.levels." + levelName + ".Fuel-duration");
            int fuelDuration = fuelDurationStr == null ? 0 : Integer.parseInt(fuelDurationStr.substring(0, fuelDurationStr.length() - 1));

            int overheat = getConfig().getInt("settings.levels." + levelName + ".Overheat");
            int fuelShare = getConfig().getInt("settings.levels." + levelName + ".Fuel-share");

            levelManager.addLevel(level, costExperiance, costEconomy, performance, reward, fuelDuration, overheat, fuelShare);
        }
    }

    private void checkStorage() {
        if (getConfig().getBoolean("Database.Activate Mysql Support")) {
            this.storage = new StorageMysql(this);
        } else {
            this.storage = new StorageYaml(this);
        }
    }

    /*
     * Saves registered furnaces to file.
     */
    private void saveToFile() {

        this.storage.closeConnection();
        checkStorage();

        /*
         * Dump FurnaceManager to file.
         */
        for (FurnaceObject furnace : furnaceManager.getFurnaces().values()) {
            if (furnace == null || furnace.getLocation() == null || furnace.getLocation().getWorld() == null) continue;
            String locationStr = Arconix.pl().getApi().serialize().serializeLocation(furnace.getLocation());

            storage.prepareSaveItem("charged",
                    new StorageItem("location", locationStr),
                    new StorageItem("level", furnace.getLevel().getLevel()),
                    new StorageItem("uses", furnace.getUses()),
                    new StorageItem("tolevel", furnace.getToLevel()),
                    new StorageItem("nickname", furnace.getNickname()),
                    new StorageItem("accesslist", furnace.getOriginalAccessList()),
                    new StorageItem("placedby", furnace.getPlacedBy() == null ? null : furnace.getPlacedBy().toString()));
        }

        /*
         * Dump BoostManager to file.
         */
        for (BoostData boostData : boostManager.getBoosts()) {
            storage.prepareSaveItem("boosts", new StorageItem("endtime", String.valueOf(boostData.getEndTime())),
                    new StorageItem("amount", boostData.getMultiplier()),
                    new StorageItem("uuid", boostData.getPlayer().toString()));
        }

        storage.doSave();

    }

    private void setupConfig() {
        settingsManager.updateSettings();

        getConfig().addDefault("settings.levels.Level-1.Performance", "10%");
        getConfig().addDefault("settings.levels.Level-1.Reward", "10%:1");
        getConfig().addDefault("settings.levels.Level-1.Cost-xp", 20);
        getConfig().addDefault("settings.levels.Level-1.Cost-eco", 5000);

        getConfig().addDefault("settings.levels.Level-2.Performance", "25%");
        getConfig().addDefault("settings.levels.Level-2.Reward", "20%:1-2");
        getConfig().addDefault("settings.levels.Level-2.Cost-xp", 25);
        getConfig().addDefault("settings.levels.Level-2.Cost-eco", 7500);

        getConfig().addDefault("settings.levels.Level-3.Performance", "40%");
        getConfig().addDefault("settings.levels.Level-3.Reward", "35%:2-3");
        getConfig().addDefault("settings.levels.Level-3.Fuel-duration", "10%");
        getConfig().addDefault("settings.levels.Level-3.Cost-xp", 30);
        getConfig().addDefault("settings.levels.Level-3.Cost-eco", 10000);

        getConfig().addDefault("settings.levels.Level-4.Performance", "55%");
        getConfig().addDefault("settings.levels.Level-4.Reward", "50%:2-4");
        getConfig().addDefault("settings.levels.Level-4.Fuel-duration", "25%");
        getConfig().addDefault("settings.levels.Level-4.Cost-xp", 35);
        getConfig().addDefault("settings.levels.Level-4.Cost-eco", 12000);

        getConfig().addDefault("settings.levels.Level-5.Performance", "75%");
        getConfig().addDefault("settings.levels.Level-5.Reward", "70%:3-4");
        getConfig().addDefault("settings.levels.Level-5.Fuel-duration", "45%");
        getConfig().addDefault("settings.levels.Level-5.Overheat", 1);
        getConfig().addDefault("settings.levels.Level-5.Cost-xp", 40);
        getConfig().addDefault("settings.levels.Level-5.Cost-eco", 15000);

        getConfig().addDefault("settings.levels.Level-6.Performance", "75%");
        getConfig().addDefault("settings.levels.Level-6.Reward", "70%:3-4");
        getConfig().addDefault("settings.levels.Level-6.Fuel-duration", "45%");
        getConfig().addDefault("settings.levels.Level-6.Overheat", 2);
        getConfig().addDefault("settings.levels.Level-6.Fuel-share", 1);
        getConfig().addDefault("settings.levels.Level-6.Cost-xp", 40);
        getConfig().addDefault("settings.levels.Level-6.Cost-eco", 15000);

        getConfig().options().copyDefaults(true);
        saveConfig();
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

                if ("locale".equals((String) file.get("type"))) {
                    InputStream in = new URL((String) file.get("link")).openStream();
                    Locale.saveDefaultLocale(in, (String) file.get("name"));
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to update.");
            //e.printStackTrace();
        }
    }

    public void reload() {
        String langMode = getConfig().getString("System.Language Mode");
        this.locale = Locale.getLocale(getConfig().getString("System.Language Mode", langMode));
        this.locale.reloadMessages();
        this.settingsManager.updateSettings();
        this.blacklistHandler.reload();
        references = new References();
        this.setupConfig();
    }

    private void loadDataFile() {
        dataFile.getConfig().options().copyDefaults(true);
        dataFile.saveConfig();
    }

    private void setupRecipes() {
        furnaceRecipeFile = new ConfigWrapper(this, "", "Furnace Recipes.yml");
        if (!getConfig().getBoolean("Main.Use Custom Recipes")) {
            return;
        }

        ConfigurationSection cs = furnaceRecipeFile.getConfig().getConfigurationSection("Recipes");
        for (String key : cs.getKeys(false)) {
            Material item;
            try {
                item = Material.valueOf(key.toUpperCase());
            } catch (IllegalArgumentException e) {
                getLogger().info("Invalid material from recipes files: " + key.toUpperCase());
                continue;
            }

            Material result;
            try {
                result = Material.valueOf(furnaceRecipeFile.getConfig().getString("Recipes." + key.toUpperCase() + ".result"));
            } catch (IllegalArgumentException e) {
                getLogger().info("Invalid material from recipes files: " + furnaceRecipeFile.getConfig().getString("Recipes." + key.toUpperCase() + ".result"));
                continue;
            }

            int amount = furnaceRecipeFile.getConfig().getInt("Recipes." + key.toUpperCase() + ".amount");
//            System.out.println("Adding- " + item.toString() + ":" + result.toString());

            getServer().addRecipe(new FurnaceRecipe(new ItemStack(result, amount), item));
        }

    }

    public ItemStack createLeveledFurnace(int level, int uses) {
        ItemStack item = new ItemStack(Material.FURNACE, 1);
        ItemMeta itemmeta = item.getItemMeta();

        if (getConfig().getBoolean("Main.Remember Furnace Item Levels")) {
            itemmeta.setDisplayName(TextComponent.formatText(Methods.formatName(level, uses, true)));
        }

        item.setItemMeta(itemmeta);
        return item;
    }

    private void register(Supplier<ProtectionPluginHook> hookSupplier) {
        this.registerProtectionHook(hookSupplier.get());
    }

    public void registerProtectionHook(ProtectionPluginHook hook) {
        Preconditions.checkNotNull(hook, "Cannot register null hooks");
        Preconditions.checkNotNull(hook.getPlugin(), "Protection plugin hooks returns null plugin instance (#getPlugin())");

        JavaPlugin hookPlugin = hook.getPlugin();
        for (ProtectionPluginHook existingHook : protectionHooks) {
            if (existingHook.getPlugin().equals(hookPlugin)) {
                throw new IllegalArgumentException("Hook already registered");
            }
        }

        this.hooksFile.getConfig().addDefault("hooks." + hookPlugin.getName(), true);
        if (!hooksFile.getConfig().getBoolean("hooks." + hookPlugin.getName(), true)) return;
        this.hooksFile.getConfig().options().copyDefaults(true);
        this.hooksFile.saveConfig();

        this.protectionHooks.add(hook);
        this.getLogger().info("Registered protection hooks for plugin: " + hook.getPlugin().getName());
    }

    public boolean canBuild(Player player, Location location) {
        if (player.hasPermission(getDescription().getName() + ".bypass")) {
            return true;
        }

        for (ProtectionPluginHook hook : protectionHooks)
            if (!hook.canBuild(player, location)) return false;
        return true;
    }

    public int getFurnceLevel(ItemStack item) {
        try {
            if (item.getItemMeta().getDisplayName().contains(":")) {
                String[] arr = (item.getItemMeta().getDisplayName().replace("§", "")).split(":");
                return Integer.parseInt(arr[0]);
            } else {
                return 1;
            }

        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return 9999;
    }

    public int getFurnaceUses(ItemStack item) {
        try {
            if (item.getItemMeta().getDisplayName().contains(":")) {
                String[] arr = (item.getItemMeta().getDisplayName().replace("§", "")).split(":");
                return Integer.parseInt(arr[1]);
            } else {
                return 0;
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return 9999;
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

    public ConfigWrapper getDataFile() {
        return dataFile;
    }

    public FurnaceManager getFurnaceManager() {
        return furnaceManager;
    }

    public ConfigWrapper getFurnaceRecipeFile() {
        return furnaceRecipeFile;
    }

    public HologramTask getHologramTask() {
        return hologramTask;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    public Locale getLocale() {
        return locale;
    }

    public References getReferences() {
        return references;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public CraftBukkitHook getCraftBukkitHook() {
        return craftBukkitHook;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public BukkitEnums getBukkitEnums() {
        return bukkitEnums;
    }
}