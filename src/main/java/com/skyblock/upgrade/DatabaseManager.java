package com.skyblock.upgrade;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    
    private final IslandUpgradePlugin plugin;
    private Connection connection;
    private final File dbFile;
    
    // Cache - anlık erişim için
    private final Map<String, UpgradeData> cache = new HashMap<>();
    
    public DatabaseManager(IslandUpgradePlugin plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), "upgrades.db");
    }
    
    public void connect() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            
            createTables();
            loadCache();
            
            plugin.getLogger().info("SQLite database connected!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createTables() {
        String sql = "CREATE TABLE IF NOT EXISTS island_upgrades (" +
                    "island_id TEXT PRIMARY KEY," +
                    "border_level INTEGER DEFAULT 1," +
                    "member_level INTEGER DEFAULT 1," +
                    "piston_level INTEGER DEFAULT 1," +
                    "last_updated INTEGER" +
                    ")";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadCache() {
        String sql = "SELECT * FROM island_upgrades";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String islandId = rs.getString("island_id");
                int borderLevel = rs.getInt("border_level");
                int memberLevel = rs.getInt("member_level");
                int pistonLevel = rs.getInt("piston_level");
                
                cache.put(islandId, new UpgradeData(borderLevel, memberLevel, pistonLevel));
            }
            
            plugin.getLogger().info("Loaded " + cache.size() + " islands from database");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public UpgradeData getData(String islandId) {
        // Cache'den al veya varsayılan değerler
        return cache.computeIfAbsent(islandId, k -> new UpgradeData(1, 1, 1));
    }
    
    public void saveData(String islandId, UpgradeData data) {
        // Önce cache'i güncelle - anında yansır
        cache.put(islandId, data);
        
        // Sonra async olarak veritabanına kaydet
        saveToDatabase(islandId, data);
    }
    
    private void saveToDatabase(String islandId, UpgradeData data) {
        String sql = "INSERT OR REPLACE INTO island_upgrades " +
                    "(island_id, border_level, member_level, piston_level, last_updated) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, islandId);
            pstmt.setInt(2, data.getBorderLevel());
            pstmt.setInt(3, data.getMemberLevel());
            pstmt.setInt(4, data.getPistonLevel());
            pstmt.setLong(5, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public int getCacheSize() {
        return cache.size();
    }
    
    public void clearCache() {
        cache.clear();
    }
    
    public void reloadCache() {
        clearCache();
        loadCache();
    }
}
