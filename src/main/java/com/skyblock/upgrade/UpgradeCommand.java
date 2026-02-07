package com.skyblock.upgrade;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;

public class UpgradeCommand implements CommandExecutor {

    private final IslandUpgradePlugin plugin;

    public UpgradeCommand(IslandUpgradePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cBu komut sadece oyuncular tarafından kullanılabilir!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§cKullanım: /islandupgrade <border|member|piston|reload>");
            return true;
        }

        // Admin komutu - reload
        if (args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("islandupgrade.admin")) {
                player.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }
            plugin.reloadConfig();
            plugin.saveData();
            player.sendMessage(plugin.getMessage("reload-success"));
            return true;
        }

        // Ada kontrolü
        IslandsManager islandsManager = plugin.getBentoBox().getIslandsManager();
        Island island = islandsManager.getIsland(player.getWorld(), player.getUniqueId());

        if (island == null) {
            player.sendMessage(plugin.getMessage("no-island"));
            return true;
        }

        // Sadece ada sahibi yükseltme yapabilir
        if (!island.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("not-owner"));
            return true;
        }

        String islandUUID = island.getUniqueId();
        IslandUpgradeData upgradeData = plugin.getUpgradeData(islandUUID);

        switch (args[0].toLowerCase()) {
            case "border":
                upgradeBorder(player, island, upgradeData);
                break;
            case "member":
                upgradeMember(player, island, upgradeData);
                break;
            case "piston":
                upgradePiston(player, island, upgradeData);
                break;
            case "testmember":
                testMemberPermissions(player);
                break;
            default:
                player.sendMessage("§cGeçersiz yükseltme türü! Kullanım: border, member, piston");
                break;
        }

        return true;
    }

    private void upgradeBorder(Player player, Island island, IslandUpgradeData upgradeData) {
        int currentLevel = upgradeData.getBorderLevel();
        int nextLevel = currentLevel + 1;

        ConfigurationSection borderConfig = plugin.getConfig().getConfigurationSection("border.levels." + nextLevel);
        
        if (borderConfig == null) {
            player.sendMessage(plugin.getMessage("max-level"));
            return;
        }

        double cost = borderConfig.getDouble("cost");
        int newSize = borderConfig.getInt("size");

        // Para kontrolü
        if (plugin.getEconomy().getBalance(player) < cost) {
            player.sendMessage(plugin.getMessage("insufficient-funds").replace("{cost}", String.valueOf(cost)));
            return;
        }

        // Parayı çek
        plugin.getEconomy().withdrawPlayer(player, cost);

        // Ada sınırını güncelle
        island.setProtectionRange(newSize / 2); // BentoBox yarıçap kullanır

        // Seviyeyi kaydet
        upgradeData.setBorderLevel(nextLevel);
        plugin.setUpgradeData(island.getUniqueId(), upgradeData);

        player.sendMessage(plugin.getMessage("upgrade-success")
                .replace("{level}", String.valueOf(nextLevel))
                .replace("&", "§"));
        player.sendMessage("§aYeni ada sınırı: §e" + newSize + "x" + newSize);
        
        // Menüyü yenile
        refreshMenu(player);
    }

    private void upgradeMember(Player player, Island island, IslandUpgradeData upgradeData) {
        int currentLevel = upgradeData.getMemberLevel();
        int nextLevel = currentLevel + 1;

        ConfigurationSection memberConfig = plugin.getConfig().getConfigurationSection("member.levels." + nextLevel);
        
        if (memberConfig == null) {
            player.sendMessage(plugin.getMessage("max-level"));
            return;
        }

        double cost = memberConfig.getDouble("cost");
        int newLimit = memberConfig.getInt("limit");

        // Para kontrolü
        if (plugin.getEconomy().getBalance(player) < cost) {
            player.sendMessage(plugin.getMessage("insufficient-funds").replace("{cost}", String.valueOf(cost)));
            return;
        }

        // Parayı çek
        plugin.getEconomy().withdrawPlayer(player, cost);

        // Eski limiti kaldır
        if (currentLevel > 1) {
            ConfigurationSection oldConfig = plugin.getConfig().getConfigurationSection("member.levels." + currentLevel);
            if (oldConfig != null) {
                int oldLimit = oldConfig.getInt("limit");
                String oldPermission = "bskyblock.team.maxsize." + oldLimit;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                    "lp user " + player.getName() + " permission unset " + oldPermission);
            }
        }

        // Yeni limiti ekle
        String newPermission = "bskyblock.team.maxsize." + newLimit;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
            "lp user " + player.getName() + " permission set " + newPermission + " true");

        // Seviyeyi kaydet
        upgradeData.setMemberLevel(nextLevel);
        plugin.setUpgradeData(island.getUniqueId(), upgradeData);

        player.sendMessage(plugin.getMessage("upgrade-success")
                .replace("{level}", String.valueOf(nextLevel))
                .replace("&", "§"));
        player.sendMessage("§aYeni üye limiti: §e" + newLimit + " oyuncu");
        player.sendMessage("§7İzin eklendi: §e" + newPermission);
        
        // Menüyü yenile
        refreshMenu(player);
    }

    private void upgradePiston(Player player, Island island, IslandUpgradeData upgradeData) {
        int currentLevel = upgradeData.getPistonLevel();
        int nextLevel = currentLevel + 1;

        ConfigurationSection pistonConfig = plugin.getConfig().getConfigurationSection("piston.levels." + nextLevel);
        
        if (pistonConfig == null) {
            player.sendMessage(plugin.getMessage("max-level"));
            return;
        }

        double cost = pistonConfig.getDouble("cost");
        int newLimit = pistonConfig.getInt("limit");

        // Para kontrolü
        if (plugin.getEconomy().getBalance(player) < cost) {
            player.sendMessage(plugin.getMessage("insufficient-funds").replace("{cost}", String.valueOf(cost)));
            return;
        }

        // Parayı çek
        plugin.getEconomy().withdrawPlayer(player, cost);

        // Eski limiti kaldır
        if (currentLevel > 1) {
            ConfigurationSection oldConfig = plugin.getConfig().getConfigurationSection("piston.levels." + currentLevel);
            if (oldConfig != null) {
                int oldLimit = oldConfig.getInt("limit");
                String oldPermission = "bskyblock.island.limit.PISTON." + oldLimit;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                    "lp user " + player.getName() + " permission unset " + oldPermission);
                
                // Sticky piston için de
                String oldPermission2 = "bskyblock.island.limit.STICKY_PISTON." + oldLimit;
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                    "lp user " + player.getName() + " permission unset " + oldPermission2);
            }
        }

        // Yeni limiti ekle
        String newPermission = "bskyblock.island.limit.PISTON." + newLimit;
        String newPermission2 = "bskyblock.island.limit.STICKY_PISTON." + newLimit;
        
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
            "lp user " + player.getName() + " permission set " + newPermission + " true");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
            "lp user " + player.getName() + " permission set " + newPermission2 + " true");

        // Seviyeyi kaydet
        upgradeData.setPistonLevel(nextLevel);
        plugin.setUpgradeData(island.getUniqueId(), upgradeData);

        player.sendMessage(plugin.getMessage("upgrade-success")
                .replace("{level}", String.valueOf(nextLevel))
                .replace("&", "§"));
        player.sendMessage("§aYeni piston limiti: §e" + newLimit + " piston");
        
        // Menüyü yenile
        refreshMenu(player);
    }

    private void refreshMenu(Player player) {
        // Config'den ayarları oku
        boolean autoRefresh = plugin.getConfig().getBoolean("menu.auto-refresh", true);
        int delay = plugin.getConfig().getInt("menu.refresh-delay", 5);
        
        if (autoRefresh) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Bukkit.dispatchCommand(player, "yukseltme");
            }, delay);
        }
    }
    
    private void testMemberPermissions(Player player) {
        player.sendMessage("§6=== Üye Limiti İzin Testi ===");
        player.sendMessage("");
        
        // Mevcut izinleri göster
        player.sendMessage("§eMevcut üye limiti izinleriniz:");
        boolean foundAny = false;
        
        for (int i = 3; i <= 15; i++) {
            String perm = "bskyblock.team.maxsize." + i;
            
            if (player.hasPermission(perm)) {
                player.sendMessage("§a✓ " + perm + " §7(Aktif limit: " + i + " oyuncu)");
                foundAny = true;
            }
        }
        
        if (!foundAny) {
            player.sendMessage("§cHiçbir üye limiti izni bulunamadı!");
            player.sendMessage("§7Varsayılan limit: 4 oyuncu");
        }
        
        player.sendMessage("");
        player.sendMessage("§7Üye eklemek için: §e/is team invite <oyuncu>");
        player.sendMessage("§7Ada bilgisi: §e/is team");
        player.sendMessage("§7Ada üyeleri: §e/is team");
    }
}
