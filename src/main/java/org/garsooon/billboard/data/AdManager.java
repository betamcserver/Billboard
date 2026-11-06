package org.garsooon.billboard.data;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.garsooon.billboard.billboard;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Logger;

public class AdManager {

    private final ConfigManager configManager;
    private Queue<AdRequest> pendingAds;
    private List<ActiveAd> activeAds;
    private Random random;
    private File dataFile;
    private final billboard plugin;

    public AdManager(billboard plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.pendingAds = new LinkedList<>();
        this.activeAds = new ArrayList<>();
        this.random = new Random();

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        dataFile = new File(plugin.getDataFolder(), "ads.yml");
    }

    public void loadData() {
        Map<String, Object> data = configManager.loadYamlFile(dataFile);

        if (data.isEmpty()) {
            plugin.getLogger().info("No existing ad data found");
            return;
        }

        // Load pending ads
        Object pendingObj = data.get("pending-ads");
        if (pendingObj instanceof List) {
            List<?> pendingList = (List<?>) pendingObj;
            for (Object obj : pendingList) {
                if (obj instanceof Map) {
                    AdRequest request = new AdRequest((Map<String, Object>) obj);
                    pendingAds.add(request);
                }
            }
        }

        // Load active ads
        Object activeObj = data.get("active-ads");
        if (activeObj instanceof List) {
            List<?> activeList = (List<?>) activeObj;
            for (Object obj : activeList) {
                if (obj instanceof Map) {
                    ActiveAd ad = new ActiveAd((Map<String, Object>) obj);
                    // Only load if not expired
                    if (!ad.isExpired(System.currentTimeMillis())) {
                        activeAds.add(ad);
                    }
                }
            }
        }

        plugin.getLogger().info("Loaded " + pendingAds.size() + " pending ads and " + activeAds.size() + " active ads");
    }

    public void saveData() {
        Map<String, Object> data = new HashMap<>();

        // Save pending ads
        List<Map<String, Object>> pendingList = new ArrayList<>();
        for (AdRequest request : pendingAds) {
            pendingList.add(request.serialize());
        }
        data.put("pending-ads", pendingList);

        // Save active ads
        List<Map<String, Object>> activeList = new ArrayList<>();
        for (ActiveAd ad : activeAds) {
            activeList.add(ad.serialize());
        }
        data.put("active-ads", activeList);

        configManager.saveYamlFile(dataFile, data);
        plugin.getLogger().info("Saved " + pendingAds.size() + " pending ads and " + activeAds.size() + " active ads");
    }

    public void addPendingAd(AdRequest request) {
        // Check if player already has a pending ad
        for (AdRequest existingRequest : pendingAds) {
            if (existingRequest.getPlayerName().equals(request.getPlayerName())) {
                return; // Don't add, player already has a pending ad
            }
        }

        pendingAds.add(request);
        saveData();
    }

    public Queue<AdRequest> getPendingAds() {
        return pendingAds;
    }

    public List<ActiveAd> getActiveAds() {
        return activeAds;
    }

    public boolean submitAd(Player player, String message, int durationDays) {
        double totalCost = configManager.getCostPerDay() * durationDays;

        if (!plugin.getEconomyBridge().has(player.getUniqueId(), totalCost)) {
            return false;
        }

        plugin.getEconomyBridge().withdraw(player.getUniqueId(), totalCost);
        pendingAds.add(new AdRequest(player.getUniqueId(), player.getName(), message, durationDays));
        saveData();
        return true;
    }

    public void approveAd(AdRequest request) {
        pendingAds.remove(request);
        ActiveAd activeAd = new ActiveAd(request.getPlayerName(), request.getMessage(), request.getDurationDays());
        activeAds.add(activeAd);
        saveData();
    }

    public void denyAd(AdRequest request) {
        double refund = configManager.getCostPerDay() * request.getDurationDays();

        plugin.getEconomyBridge().deposit(request.getPlayerId(), refund);
        pendingAds.remove(request);
        saveData();
    }

    public void broadcastRandomAd(Server server, ConfigManager config) {
        if (activeAds.isEmpty()) return;

        ActiveAd ad = activeAds.get(random.nextInt(activeAds.size()));
        String header = translateColorCodes(config.getBroadcastHeader());
        String message = translateColorCodes(ad.getMessage());

        server.broadcastMessage(header + " " + message);
    }

    private String translateColorCodes(String text) {
        return text.replace("&0", ChatColor.BLACK.toString())
                .replace("&1", ChatColor.DARK_BLUE.toString())
                .replace("&2", ChatColor.DARK_GREEN.toString())
                .replace("&3", ChatColor.DARK_AQUA.toString())
                .replace("&4", ChatColor.DARK_RED.toString())
                .replace("&5", ChatColor.DARK_PURPLE.toString())
                .replace("&6", ChatColor.GOLD.toString())
                .replace("&7", ChatColor.GRAY.toString())
                .replace("&8", ChatColor.DARK_GRAY.toString())
                .replace("&9", ChatColor.BLUE.toString())
                .replace("&a", ChatColor.GREEN.toString())
                .replace("&b", ChatColor.AQUA.toString())
                .replace("&c", ChatColor.RED.toString())
                .replace("&d", ChatColor.LIGHT_PURPLE.toString())
                .replace("&e", ChatColor.YELLOW.toString())
                .replace("&f", ChatColor.WHITE.toString());

    }

    public void removeExpiredAds(Logger logger) {
        long currentTime = System.currentTimeMillis();
        Iterator<ActiveAd> iterator = activeAds.iterator();
        boolean changed = false;

        while (iterator.hasNext()) {
            ActiveAd ad = iterator.next();
            if (ad.isExpired(currentTime)) {
                iterator.remove();
                logger.info("Ad by " + ad.getPlayerName() + " has expired and been removed.");
                changed = true;
            }
        }

        if (changed) {
            saveData();
        }
    }

    public void notifyModerators(Server server) {
        for (Player player : server.getOnlinePlayers()) {
            if (player.hasPermission("adbroadcast.moderate")) {
                player.sendMessage(ChatColor.GOLD + "[Billboard] " + ChatColor.YELLOW +
                        "New ad pending review! Use /reviewads");
            }
        }
    }
}