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
            sender.sendMessage("§cOnly players!");
            return true;
        }

        Player player = (Player) sender;

        // Reload komutu
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("islandupgrade.admin")) {
                player.sendMessage(plugin.getMessage("no-permission"));
                return true;
            }
            plugin.reloadConfig();
            plugin.getDatabase().reloadCache();
            player.sendMessage(plugin.getMessage("reload-success"));
            return true;
        }

        // Info komutu
        if (args.length > 0 && args[0].equalsIgnoreCase("info")) {
            player.sendMessage("§6=== Island Upgrade v2.0 ===");
            player.sendMessage("§7Database: §eSQLite");
            player.sendMessage("§7Cached islands: §e" + plugin.getDatabase().getCacheSize());
            return true;
        }

        // Menü aç
        if (args.length == 0) {
            Bukkit.dispatchCommand(player, "dm open IslandUpgrade");
            return true;
        }

        // Ada kontrolü
        IslandsManager manager = plugin.getBentoBox().getIslandsManager();
        Island island = manager.getIsland(player.getWorld(), player.getUniqueId());

        if (island == null) {
            player.sendMessage(plugin.getMessage("no-island"));
            return true;
        }

        if (!island.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMessage("not-owner"));
            return true;
        }

        String islandId = island.getUniqueId();
        UpgradeData data = plugin.getDatabase().getData(islandId);

        switch (args[0].toLowerCase()) {
            case "border":
                upgradeBorder(player, island, data, islandId);
                break;
            case "member":
                upgradeMember(player, island, data, islandId);
                break;
            case "piston":
                upgradePiston(player, data, islandId);
                break;
            default:
                player.sendMessage("§cKullanım: /iu <border|member|piston>");
                break;
        }

        return true;
    }

    private void upgradeBorder(Player player, Island island, UpgradeData data, String islandId) {
        int current = data.getBorderLevel();
        int next = current + 1;

        ConfigurationSection config = plugin.getConfig().getConfigurationSection("border.levels." + next);
        if (config == null) {
            player.sendMessage(plugin.getMessage("max-level"));
            return;
        }

        double cost = config.getDouble("cost");
        int newSize = config.getInt("size");

        if (plugin.getEconomy().getBalance(player) < cost) {
            player.sendMessage(plugin.getMessage("insufficient-funds").replace("{cost}", String.valueOf(cost)));
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, cost);
        island.setProtectionRange(newSize / 2);
        
        data.setBorderLevel(next);
        plugin.getDatabase().saveData(islandId, data);

        player.sendMessage(plugin.getMessage("upgrade-success").replace("{level}", String.valueOf(next)));
        player.sendMessage("§7Ada sınırı: §e" + newSize + "x" + newSize);
        
        refreshMenu(player);
    }

    private void upgradeMember(Player player, Island island, UpgradeData data, String islandId) {
        int current = data.getMemberLevel();
        int next = current + 1;

        ConfigurationSection config = plugin.getConfig().getConfigurationSection("member.levels." + next);
        if (config == null) {
            player.sendMessage(plugin.getMessage("max-level"));
            return;
        }

        double cost = config.getDouble("cost");
        int newLimit = config.getInt("limit");

        if (plugin.getEconomy().getBalance(player) < cost) {
            player.sendMessage(plugin.getMessage("insufficient-funds").replace("{cost}", String.valueOf(cost)));
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, cost);
        
        // Eski izni kaldır
        if (current > 1) {
            ConfigurationSection oldConfig = plugin.getConfig().getConfigurationSection("member.levels." + current);
            if (oldConfig != null) {
                int oldLimit = oldConfig.getInt("limit");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                    "lp user " + player.getName() + " permission unset bskyblock.team.maxsize." + oldLimit);
            }
        }
        
        // Yeni izni ekle
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
            "lp user " + player.getName() + " permission set bskyblock.team.maxsize." + newLimit + " true");
        
        data.setMemberLevel(next);
        plugin.getDatabase().saveData(islandId, data);

        player.sendMessage(plugin.getMessage("upgrade-success").replace("{level}", String.valueOf(next)));
        player.sendMessage("§7Üye limiti: §e" + newLimit + " oyuncu");
        
        refreshMenu(player);
    }

    private void upgradePiston(Player player, UpgradeData data, String islandId) {
        int current = data.getPistonLevel();
        int next = current + 1;

        ConfigurationSection config = plugin.getConfig().getConfigurationSection("piston.levels." + next);
        if (config == null) {
            player.sendMessage(plugin.getMessage("max-level"));
            return;
        }

        double cost = config.getDouble("cost");
        int newLimit = config.getInt("limit");

        if (plugin.getEconomy().getBalance(player) < cost) {
            player.sendMessage(plugin.getMessage("insufficient-funds").replace("{cost}", String.valueOf(cost)));
            return;
        }

        plugin.getEconomy().withdrawPlayer(player, cost);
        
        // Eski izni kaldır
        if (current > 1) {
            ConfigurationSection oldConfig = plugin.getConfig().getConfigurationSection("piston.levels." + current);
            if (oldConfig != null) {
                int oldLimit = oldConfig.getInt("limit");
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                    "lp user " + player.getName() + " permission unset bskyblock.island.limit.PISTON." + oldLimit);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                    "lp user " + player.getName() + " permission unset bskyblock.island.limit.STICKY_PISTON." + oldLimit);
            }
        }
        
        // Yeni izni ekle
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
            "lp user " + player.getName() + " permission set bskyblock.island.limit.PISTON." + newLimit + " true");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
            "lp user " + player.getName() + " permission set bskyblock.island.limit.STICKY_PISTON." + newLimit + " true");
        
        data.setPistonLevel(next);
        plugin.getDatabase().saveData(islandId, data);

        player.sendMessage(plugin.getMessage("upgrade-success").replace("{level}", String.valueOf(next)));
        player.sendMessage("§7Piston limiti: §e" + newLimit);
        
        refreshMenu(player);
    }

    private void refreshMenu(Player player) {
        // Menüyü kapat
        Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
        
        // 10 tick (0.5 saniye) sonra tekrar aç
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.dispatchCommand(player, "dm open IslandUpgrade");
        }, 10L);
    }
}
