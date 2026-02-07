package com.skyblock.upgrade;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import world.bentobox.bentobox.BentoBox;

public class IslandUpgradePlugin extends JavaPlugin {

    private static IslandUpgradePlugin instance;
    private Economy economy;
    private BentoBox bentoBox;
    private DatabaseManager database;

    @Override
    public void onEnable() {
        instance = this;
        
        // Config kaydet
        saveDefaultConfig();
        
        // Vault kurulumu
        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // BentoBox kurulumu
        bentoBox = BentoBox.getInstance();
        if (bentoBox == null) {
            getLogger().severe("BentoBox not found! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Database başlat
        database = new DatabaseManager(this);
        database.connect();
        
        // Komutları kaydet
        getCommand("islandupgrade").setExecutor(new UpgradeCommand(this));
        
        // PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new UpgradePlaceholders(this).register();
            getLogger().info("PlaceholderAPI hooked!");
        }
        
        getLogger().info("=================================");
        getLogger().info("Island Upgrade v2.0 ENABLED");
        getLogger().info("Database: SQLite");
        getLogger().info("Islands loaded: " + database.getCacheSize());
        getLogger().info("=================================");
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.close();
        }
        getLogger().info("Island Upgrade v2.0 disabled!");
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

    public static IslandUpgradePlugin getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return economy;
    }

    public BentoBox getBentoBox() {
        return bentoBox;
    }
    
    public DatabaseManager getDatabase() {
        return database;
    }

    public String getMessage(String path) {
        String prefix = getConfig().getString("messages.prefix", "");
        String message = getConfig().getString("messages." + path, "&cMessage not found!");
        return (prefix + message).replace("&", "§");
    }
}
