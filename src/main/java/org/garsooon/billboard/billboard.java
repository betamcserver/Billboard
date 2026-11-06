package org.garsooon.billboard;

import org.bukkit.plugin.java.JavaPlugin;
import org.garsooon.billboard.commands.AdCommands;
import org.garsooon.billboard.commands.BroadcastCommands;
import org.garsooon.billboard.data.AdManager;
import org.garsooon.billboard.data.AutoBroadcaster;
import org.garsooon.billboard.data.ConfigManager;
import org.garsooon.billboard.economy.RetroBridgeAccess;
import org.garsooon.billboard.listeners.PlayerJoinListener;
import org.retromc.retrobridge.bridge.economy.EconomyBridge;

public class billboard extends JavaPlugin {

    private AdManager adManager;
    private ConfigManager configManager;
    private AutoBroadcaster autobroadcaster;
    private int broadcastTaskId;
    private int tickTaskId;
    private RetroBridgeAccess retroBridgeAccess;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        retroBridgeAccess = new RetroBridgeAccess();
        if (!retroBridgeAccess.isAvailable()) {
            getLogger().severe("Billboard requires RetroBridge to function.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getEconomyBridge() == null) {
            getLogger().severe("No compatible economy plugin found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        adManager = new AdManager(this, configManager);
        adManager.loadData();

        AdCommands adCommands = new AdCommands(this, adManager, configManager);

        getCommand("buyad").setExecutor(adCommands);
        getCommand("reviewads").setExecutor(adCommands);
        getCommand("approvead").setExecutor(adCommands);
        getCommand("denyad").setExecutor(adCommands);
        getCommand("listads").setExecutor(adCommands);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Start broadcast task for ads
        long interval = configManager.getBroadcastInterval() * 20L;
        broadcastTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> adManager.broadcastRandomAd(getServer(), configManager), interval, interval);

        // Start tick task to remove expired ads (checks every 5 minutes)
        tickTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> adManager.removeExpiredAds(getLogger()), 6000L, 6000L);

        // Start broadcast task for server broadcasts
        autobroadcaster = new AutoBroadcaster(this);
        autobroadcaster.start();

        getCommand("broadcast").setExecutor(new BroadcastCommands(autobroadcaster));

        getLogger().info("Billboard has been enabled!");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTask(broadcastTaskId);
        getServer().getScheduler().cancelTask(tickTaskId);

        if (autobroadcaster != null) {
            autobroadcaster.stop();
        }

        adManager.saveData();
        getLogger().info("Billboard has been disabled!");
    }

    public AdManager getAdManager() {
        return adManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public AutoBroadcaster getAutobroadcaster() {
        return autobroadcaster;
    }

    public EconomyBridge getEconomyBridge() {
        return retroBridgeAccess.getEconomyBridge();
    }
}