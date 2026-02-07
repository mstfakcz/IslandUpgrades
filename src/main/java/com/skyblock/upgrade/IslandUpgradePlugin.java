package com.skyblock.upgrade;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import world.bentobox.bentobox.BentoBox;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IslandUpgradePlugin extends JavaPlugin {

    private static IslandUpgradePlugin instance;
    private Economy economy;
    private BentoBox bentoBox;
    private File dataFile;
    private FileConfiguration data;
    
    // Veri önbelleği
    private Map<String, IslandUpgradeData> upgradeCache = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        
        // Yapılandırma dosyasını kaydet
        saveDefaultConfig();
        
        // Vault kurulumu
        if (!setupEconomy()) {
            getLogger().severe("Vault bulunamadı! Eklenti devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // BentoBox kurulumu
        bentoBox = BentoBox.getInstance();
        if (bentoBox == null) {
            getLogger().severe("BentoBox bulunamadı! Eklenti devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Veri dosyasını yükle
        loadData();
        
        // Komutları kaydet
        getCommand("islandupgrade").setExecutor(new UpgradeCommand(this));
        
        // PlaceholderAPI entegrasyonu
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new UpgradePlaceholders(this).register();
            getLogger().info("PlaceholderAPI entegrasyonu aktif!");
        }
        
        getLogger().info("Island Upgrade Plugin başarıyla yüklendi!");
    }

    @Override
    public void onDisable() {
        saveData();
        getLogger().info("Island Upgrade Plugin kapatıldı!");
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

    private void loadData() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        
        // Veriyi önbelleğe yükle
        if (data.contains("islands")) {
            for (String key : data.getConfigurationSection("islands").getKeys(false)) {
                String islandUUID = key;
                int borderLevel = data.getInt("islands." + key + ".border", 1);
                int memberLevel = data.getInt("islands." + key + ".member", 1);
                int pistonLevel = data.getInt("islands." + key + ".piston", 1);
                
                IslandUpgradeData upgradeData = new IslandUpgradeData(borderLevel, memberLevel, pistonLevel);
                upgradeCache.put(islandUUID, upgradeData);
            }
        }
    }

    public void saveData() {
        // Önbellekteki veriyi dosyaya kaydet
        for (Map.Entry<String, IslandUpgradeData> entry : upgradeCache.entrySet()) {
            String path = "islands." + entry.getKey();
            data.set(path + ".border", entry.getValue().getBorderLevel());
            data.set(path + ".member", entry.getValue().getMemberLevel());
            data.set(path + ".piston", entry.getValue().getPistonLevel());
        }
        
        try {
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public IslandUpgradeData getUpgradeData(String islandUUID) {
        return upgradeCache.computeIfAbsent(islandUUID, k -> new IslandUpgradeData(1, 1, 1));
    }

    public void setUpgradeData(String islandUUID, IslandUpgradeData upgradeData) {
        upgradeCache.put(islandUUID, upgradeData);
        saveData();
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

    public String getMessage(String path) {
        String prefix = getConfig().getString("messages.prefix", "");
        String message = getConfig().getString("messages." + path, "&cMesaj bulunamadı!");
        return prefix + message.replace("&", "§");
    }
}
