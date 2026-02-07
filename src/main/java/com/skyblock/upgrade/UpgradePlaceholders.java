package com.skyblock.upgrade;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;

import java.text.DecimalFormat;

public class UpgradePlaceholders extends PlaceholderExpansion {

    private final IslandUpgradePlugin plugin;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public UpgradePlaceholders(IslandUpgradePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "SkyBlock";
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "islandupgrade";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        IslandsManager manager = plugin.getBentoBox().getIslandsManager();
        Island island = manager.getIsland(player.getWorld(), player.getUniqueId());

        if (island == null) {
            return "N/A";
        }

        // Cache'den direkt al - her zaman güncel
        UpgradeData data = plugin.getDatabase().getData(island.getUniqueId());

        // Border
        if (identifier.equals("border_level")) {
            return String.valueOf(data.getBorderLevel());
        }
        if (identifier.equals("border_size")) {
            ConfigurationSection config = plugin.getConfig().getConfigurationSection("border.levels." + data.getBorderLevel());
            return config != null ? String.valueOf(config.getInt("size")) : "50";
        }
        if (identifier.equals("border_next_size")) {
            ConfigurationSection config = plugin.getConfig().getConfigurationSection("border.levels." + (data.getBorderLevel() + 1));
            return config != null ? String.valueOf(config.getInt("size")) : "MAX";
        }
        if (identifier.equals("border_cost")) {
            ConfigurationSection config = plugin.getConfig().getConfigurationSection("border.levels." + (data.getBorderLevel() + 1));
            return config != null ? formatter.format(config.getDouble("cost")) : "0";
        }
        if (identifier.equals("border_status")) {
            return getStatus(player, "border", data.getBorderLevel());
        }

        // Member
        if (identifier.equals("member_level")) {
            return String.valueOf(data.getMemberLevel());
        }
        if (identifier.equals("member_limit")) {
            ConfigurationSection config = plugin.getConfig().getConfigurationSection("member.levels." + data.getMemberLevel());
            return config != null ? String.valueOf(config.getInt("limit")) : "4";
        }
        if (identifier.equals("member_next_limit")) {
            ConfigurationSection config = plugin.getConfig().getConfigurationSection("member.levels." + (data.getMemberLevel() + 1));
            return config != null ? String.valueOf(config.getInt("limit")) : "MAX";
        }
        if (identifier.equals("member_cost")) {
            ConfigurationSection config = plugin.getConfig().getConfigurationSection("member.levels." + (data.getMemberLevel() + 1));
            return config != null ? formatter.format(config.getDouble("cost")) : "0";
        }
        if (identifier.equals("member_status")) {
            return getStatus(player, "member", data.getMemberLevel());
        }

        // Piston
        if (identifier.equals("piston_level")) {
            return String.valueOf(data.getPistonLevel());
        }
        if (identifier.equals("piston_limit")) {
            ConfigurationSection config = plugin.getConfig().getConfigurationSection("piston.levels." + data.getPistonLevel());
            return config != null ? String.valueOf(config.getInt("limit")) : "250";
        }
        if (identifier.equals("piston_next_limit")) {
            ConfigurationSection config = plugin.getConfig().getConfigurationSection("piston.levels." + (data.getPistonLevel() + 1));
            return config != null ? String.valueOf(config.getInt("limit")) : "MAX";
        }
        if (identifier.equals("piston_cost")) {
            ConfigurationSection config = plugin.getConfig().getConfigurationSection("piston.levels." + (data.getPistonLevel() + 1));
            return config != null ? formatter.format(config.getDouble("cost")) : "0";
        }
        if (identifier.equals("piston_status")) {
            return getStatus(player, "piston", data.getPistonLevel());
        }

        return null;
    }

    private String getStatus(Player player, String type, int level) {
        ConfigurationSection next = plugin.getConfig().getConfigurationSection(type + ".levels." + (level + 1));
        
        if (next == null) {
            return "§a§l✓ MAX";
        }
        
        double cost = next.getDouble("cost");
        double balance = plugin.getEconomy().getBalance(player);
        
        return balance >= cost ? "§e§l▸ Tıkla!" : "§c§l✗ Yetersiz";
    }
}
