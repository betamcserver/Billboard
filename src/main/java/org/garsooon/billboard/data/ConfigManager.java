package org.garsooon.billboard.data;

import org.garsooon.billboard.billboard;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final billboard plugin;
    private File configFile;
    private Map<String, Object> config;

    // Default values
    private int broadcastInterval = 300;
    private String broadcastHeader = "&6[AD]";
    private int maxDurationDays = 30;
    private int maxMessageLength = 100;
    private double costPerDay = 25.0;

    public ConfigManager(billboard plugin) {
        this.plugin = plugin;
        this.config = new HashMap<>();

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.configFile = new File(plugin.getDataFolder(), "config.yml");
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            createDefaultConfig();
            return;
        }

        config = loadYamlFile(configFile);

        if (config == null || config.isEmpty()) {
            config = new HashMap<>();
            createDefaultConfig();
            return;
        }

        broadcastInterval = getInt("broadcast-interval", broadcastInterval);
        broadcastHeader = getString(broadcastHeader);
        maxDurationDays = getInt("max-duration-days", maxDurationDays);
        maxMessageLength = getInt("max-message-length", maxMessageLength);
        costPerDay = getDouble(costPerDay);

        plugin.getLogger().info("Configuration loaded successfully");
    }

    private void createDefaultConfig() {
        config.put("broadcast-interval", broadcastInterval);
        config.put("broadcast-header", broadcastHeader);
        config.put("max-duration-days", maxDurationDays);
        config.put("max-message-length", maxMessageLength);
        config.put("cost-per-day", costPerDay);

        saveYamlFile(configFile, config);
        plugin.getLogger().info("Created default config.yml");
    }

    private String getString(String defaultValue) {
        if (config.containsKey("broadcast-header")) {
            Object val = config.get("broadcast-header");
            if (val instanceof String) {
                return (String) val;
            }
        }
        return defaultValue;
    }

    private int getInt(String key, int defaultValue) {
        if (config.containsKey(key)) {
            Object val = config.get(key);
            if (val instanceof Number) {
                return ((Number) val).intValue();
            }
        }
        return defaultValue;
    }

    public int getBroadcastInterval() {
        return broadcastInterval;
    }

    public String getBroadcastHeader() {
        return broadcastHeader;
    }

    public int getMaxDurationDays() {
        return maxDurationDays;
    }

    public int getMaxMessageLength() {
        return maxMessageLength;
    }

    ///util
    public Map<String, Object> loadYamlFile(File file) {
        if (!file.exists() || file.length() == 0) {
            return new HashMap<>();
        }

        try {
            Yaml yaml = new Yaml();
            FileInputStream fis = new FileInputStream(file);
            Object loadedObj = yaml.load(fis);
            fis.close();

            if (loadedObj instanceof Map) {
                return (Map<String, Object>) loadedObj;
            }

            return new HashMap<>();

        } catch (IOException e) {
            plugin.getLogger().severe("Could not load " + file.getName() + "!");
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public void saveYamlFile(File file, Map<String, Object> data) {
        try {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            FileWriter writer = new FileWriter(file);
            yaml.dump(data, writer);
            writer.close();
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + file.getName() + "!");
            e.printStackTrace();
        }
    }

    private double getDouble(double defaultValue) {
        if (config.containsKey("cost-per-day")) {
            Object val = config.get("cost-per-day");
            if (val instanceof Number) {
                return ((Number) val).doubleValue();
            }
        }
        return defaultValue;
    }

    public double getCostPerDay() {
        return costPerDay;
    }
}