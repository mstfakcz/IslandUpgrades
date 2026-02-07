package com.skyblock.upgrade;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;

import java.text.DecimalFormat;
import java.util.Optional;

public class UpgradePlaceholders extends PlaceholderExpansion {

    private final IslandUpgradePlugin plugin;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public UpgradePlaceholders(IslandUpgradePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "YourName";
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "islandupgrade";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0.0";
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

        // Ada kontrolü
        IslandsManager islandsManager = plugin.getBentoBox().getIslandsManager();
        Island island = islandsManager.getIsland(player.getWorld(), player.getUniqueId());

        if (island == null) {
            return "Ada Yok";
        }

        String islandUUID = island.getUniqueId();
        IslandUpgradeData upgradeData = plugin.getUpgradeData(islandUUID);

        // Border placeholders
        if (identifier.startsWith("border_")) {
            return handleBorderPlaceholder(player, upgradeData, identifier);
        }

        // Member placeholders
        if (identifier.startsWith("member_")) {
            return handleMemberPlaceholder(player, upgradeData, identifier);
        }

        // Piston placeholders
        if (identifier.startsWith("piston_")) {
            return handlePistonPlaceholder(player, upgradeData, identifier);
        }

        return null;
    }

    private String handleBorderPlaceholder(Player player, IslandUpgradeData upgradeData, String identifier) {
        int currentLevel = upgradeData.getBorderLevel();
        ConfigurationSection currentConfig = plugin.getConfig().getConfigurationSection("border.levels." + currentLevel);

        switch (identifier) {
            case "border_level":
                return String.valueOf(currentLevel);

            case "border_size":
                return currentConfig != null ? String.valueOf(currentConfig.getInt("size")) : "50";

            case "border_next_size":
                ConfigurationSection nextConfig = plugin.getConfig().getConfigurationSection("border.levels." + (currentLevel + 1));
                if (nextConfig != null) {
                    return String.valueOf(nextConfig.getInt("size"));
                }
                return "MAX";

            case "border_cost":
                ConfigurationSection nextBorderConfig = plugin.getConfig().getConfigurationSection("border.levels." + (currentLevel + 1));
                if (nextBorderConfig != null) {
                    return formatter.format(nextBorderConfig.getDouble("cost"));
                }
                return "0";

            case "border_status":
                return getUpgradeStatus(player, "border", currentLevel);

            default:
                return null;
        }
    }

    private String handleMemberPlaceholder(Player player, IslandUpgradeData upgradeData, String identifier) {
        int currentLevel = upgradeData.getMemberLevel();
        ConfigurationSection currentConfig = plugin.getConfig().getConfigurationSection("member.levels." + currentLevel);

        switch (identifier) {
            case "member_level":
                return String.valueOf(currentLevel);

            case "member_limit":
                return currentConfig != null ? String.valueOf(currentConfig.getInt("limit")) : "3";

            case "member_next_limit":
                ConfigurationSection nextConfig = plugin.getConfig().getConfigurationSection("member.levels." + (currentLevel + 1));
                if (nextConfig != null) {
                    return String.valueOf(nextConfig.getInt("limit"));
                }
                return "MAX";

            case "member_cost":
                ConfigurationSection nextMemberConfig = plugin.getConfig().getConfigurationSection("member.levels." + (currentLevel + 1));
                if (nextMemberConfig != null) {
                    return formatter.format(nextMemberConfig.getDouble("cost"));
                }
                return "0";

            case "member_status":
                return getUpgradeStatus(player, "member", currentLevel);

            default:
                return null;
        }
    }

    private String handlePistonPlaceholder(Player player, IslandUpgradeData upgradeData, String identifier) {
        int currentLevel = upgradeData.getPistonLevel();
        ConfigurationSection currentConfig = plugin.getConfig().getConfigurationSection("piston.levels." + currentLevel);

        switch (identifier) {
            case "piston_level":
                return String.valueOf(currentLevel);

            case "piston_limit":
                return currentConfig != null ? String.valueOf(currentConfig.getInt("limit")) : "250";

            case "piston_next_limit":
                ConfigurationSection nextConfig = plugin.getConfig().getConfigurationSection("piston.levels." + (currentLevel + 1));
                if (nextConfig != null) {
                    return String.valueOf(nextConfig.getInt("limit"));
                }
                return "MAX";

            case "piston_cost":
                ConfigurationSection nextPistonConfig = plugin.getConfig().getConfigurationSection("piston.levels." + (currentLevel + 1));
                if (nextPistonConfig != null) {
                    return formatter.format(nextPistonConfig.getDouble("cost"));
                }
                return "0";

            case "piston_status":
                return getUpgradeStatus(player, "piston", currentLevel);

            default:
                return null;
        }
    }

    private String getUpgradeStatus(Player player, String upgradeType, int currentLevel) {
        ConfigurationSection nextConfig = plugin.getConfig().getConfigurationSection(upgradeType + ".levels." + (currentLevel + 1));

        if (nextConfig == null) {
            return plugin.getConfig().getString("placeholders.max-level-text", "§a§l✓ Maksimum Seviye").replace("&", "§");
        }

        double cost = nextConfig.getDouble("cost");
        double balance = plugin.getEconomy().getBalance(player);

        if (balance >= cost) {
            return plugin.getConfig().getString("placeholders.can-upgrade-text", "§e§l▸ Yükseltmek için tıkla!").replace("&", "§");
        } else {
            return plugin.getConfig().getString("placeholders.cant-upgrade-text", "§c§l✗ Yetersiz bakiye!").replace("&", "§");
        }
    }
}
