package com.songoda.epicfurnaces;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Locale {

    private static JavaPlugin plugin;
    private static File localeFolder;
    private static final Pattern NODE_PATTERN = Pattern.compile("(\\w+(?:\\.{1}\\w+)*)\\s*=\\s*\"(.*)\"");
    private static final String FILE_EXTENSION = ".lang";

    private static final List<Locale> LOCALES = new ArrayList<>();

    private final Map<String, String> nodes = new HashMap<>();

    private static String defaultLocale;

    private File file;
    private String name;

    public Locale(String name) {
        if (plugin == null)
            return;

        this.name = name;

        String fileName = name + FILE_EXTENSION;
        this.file = new File(localeFolder, fileName);

        if (!this.reloadMessages()) return;

        plugin.getLogger().info("Loaded locale \"" + fileName + "\"");
    }
    public Locale(JavaPlugin plugin, String defaultLocale) {

        Locale.plugin = plugin;
        Locale.localeFolder = new File(plugin.getDataFolder(), "locales/");

        if (!localeFolder.exists()) localeFolder.mkdirs();

        //Save the default locale file.
        saveLocale(defaultLocale);

        for (File file : localeFolder.listFiles()) {
            String name = file.getName();
            if (!name.endsWith(FILE_EXTENSION)) continue;

            String fileName = name.substring(0, name.lastIndexOf('.'));

            if (fileName.split("_").length != 2) continue;

            LOCALES.add(new Locale(fileName));
        }
    }

    public static boolean saveLocale(String fileName) {
        return saveLocale(null, fileName);
    }

    public static boolean saveLocale(InputStream in, String fileName) {
        if (!localeFolder.exists()) localeFolder.mkdirs();

        if (!fileName.endsWith(FILE_EXTENSION))
            fileName = (fileName.lastIndexOf(".") == -1 ? fileName : fileName.substring(0, fileName.lastIndexOf('.'))) + FILE_EXTENSION;

        File destinationFile = new File(localeFolder, fileName);
        if (destinationFile.exists()) {
            return compareFiles(plugin.getResource(fileName), destinationFile);
        }

        try (OutputStream outputStream = new FileOutputStream(destinationFile)) {
            copy(in == null ? plugin.getResource(fileName) : in, outputStream);

            fileName = fileName.substring(0, fileName.lastIndexOf('.'));

            if (fileName.split("_").length != 2) return false;

            LOCALES.add(new Locale(fileName));
            if (defaultLocale == null) defaultLocale = fileName;
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    private static boolean compareFiles(InputStream defaultFile, File existingFile) {
        // Look for default
        if (defaultFile == null) {
            defaultFile = plugin.getResource(defaultLocale != null ? defaultLocale : "en_US");
            if (defaultFile == null) return false; // No default at all
        }

        boolean changed = false;

        List<String> defaultLines, existingLines;
        try (BufferedReader defaultReader = new BufferedReader(new InputStreamReader(defaultFile));
             BufferedReader existingReader = new BufferedReader(new FileReader(existingFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(existingFile, true))) {
            defaultLines = defaultReader.lines().collect(Collectors.toList());
            existingLines = existingReader.lines().map(s -> s.split("\\s*=")[0]).collect(Collectors.toList());

            for (String defaultValue : defaultLines) {
                if (defaultValue.isEmpty() || defaultValue.startsWith("#")) continue;

                String key = defaultValue.split("\\s*=")[0];

                if (!existingLines.contains(key)) {
                    if (!changed) {
                        writer.newLine();
                        writer.newLine();
                        writer.write("# New messages for " + plugin.getName() + " v" + plugin.getDescription().getVersion());
                    }

                    writer.newLine();
                    writer.write(defaultValue);

                    changed = true;
                }
            }
        } catch (IOException e) {
            return false;
        }

        return changed;
    }

    public static boolean localeExists(String name) {
        for (Locale locale : LOCALES)
            if (locale.getName().equals(name)) return true;
        return false;
    }


    public static Locale getLocale(String name) {
        for (Locale locale : LOCALES)
            if (locale.getName().equalsIgnoreCase(name)) return locale;
        return null;
    }

    private static void copy(InputStream input, OutputStream output) {
        int n;
        byte[] buffer = new byte[1024 * 4];

        try {
            while ((n = input.read(buffer)) != -1) {
                output.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean reloadMessages() {
        if (!this.file.exists()) {
            plugin.getLogger().warning("Could not find file for locale \"" + this.name + "\"");
            return false;
        }

        this.nodes.clear(); // Clear previous data (if any)

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            for (int lineNumber = 0; (line = reader.readLine()) != null; lineNumber++) {
                if (line.trim().isEmpty() || line.startsWith("#") /* Comment */) continue;

                Matcher matcher = NODE_PATTERN.matcher(line);
                if (!matcher.find()) {
                    System.err.println("Invalid locale syntax at (line=" + lineNumber + ")");
                    continue;
                }

                nodes.put(matcher.group(1), matcher.group(2));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * Get a message set for a specific node
     *
     * @param node the node to get
     * @return the message for the specified node
     */
    public String getMessage(String node) {
        return ChatColor.translateAlternateColorCodes('&', this.getMessageOrDefault(node, node));
    }

    /**
     * Get a message set for a specific node and replace its params with a supplied arguments.
     *
     * @param node the node to get
     * @param args the replacement arguments
     * @return the message for the specified node
     */
    public String getMessage(String node, Object... args) {
        String message = getMessage(node);
        for (Object arg : args) {
            message = message.replaceFirst("\\%.*?\\%", arg.toString());
        }
        return message;
    }

    /**
     * Get a message set for a specific node
     *
     * @param node         the node to get
     * @param defaultValue the default value given that a value for the node was not found
     * @return the message for the specified node. Default if none found
     */
    public String getMessageOrDefault(String node, String defaultValue) {
        return this.nodes.getOrDefault(node, defaultValue);
    }

    public String getName() {
        return name;
    }
}
