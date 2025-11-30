package com.NguyenDevs.simpleMachete.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class UpdateChecker {
    private final JavaPlugin plugin;
    private final int resourceId;
    private String latestVersion;

    public UpdateChecker(int resourceId, JavaPlugin plugin) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void checkForUpdate() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=" + resourceId)
                        .openConnection();
                connection.setRequestMethod("GET");
                latestVersion = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Couldn't check the latest plugin version: " + e.getMessage());
                return;
            }

            String currentVersion = plugin.getDescription().getVersion();
            if (latestVersion == null || latestVersion.isEmpty()) {
                plugin.getLogger().log(Level.WARNING, "Received invalid version from SpigotMC API.");
                return;
            }

            if (isVersionNewer(currentVersion, latestVersion)) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&b[&eSimple&6Machete&b] &eA new version is available: &a" + latestVersion + " &e(you are running &c" + currentVersion + "&e)"));

                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&b[&eSimple&6Machete&b] &a&oDownload it here: " + getResourceUrl()));

                if (plugin.getConfig().getBoolean("notify-update", true)) {
                    Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().registerEvents(new Listener() {
                        @EventHandler(priority = EventPriority.MONITOR)
                        public void onPlayerJoin(PlayerJoinEvent event) {
                            Player player = event.getPlayer();
                            if (player.hasPermission("simplemachete.admin")) {
                                getOutOfDateMessage().forEach(msg -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg)));
                            }
                        }
                    }, plugin));
                }
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&b[&eSimple&6Machete&b] &aYou are running the latest version: &2" + currentVersion));
            }
        });
    }

    private boolean isVersionNewer(String currentVersion, String latestVersion) {
        if (currentVersion.equals(latestVersion)) {
            return false;
        }
        try {
            String[] currentParts = currentVersion.split("\\.");
            String[] latestParts = latestVersion.split("\\.");

            int length = Math.max(currentParts.length, latestParts.length);
            for (int i = 0; i < length; i++) {
                int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
                int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;

                if (latestPart > currentPart) {
                    return true;
                } else if (latestPart < currentPart) {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().log(Level.WARNING, "Invalid version format detected: Current=" + currentVersion + ", Latest=" + latestVersion);
            return false;
        }

        return false;
    }

    private List<String> getOutOfDateMessage() {
        return Arrays.asList(
                "&8--------------------------------------------",
                "&eSimple&6Machete &a" + latestVersion + " &eis available!",
                "&a" + getResourceUrl(),
                "&7&oYou can disable this notification in config.yml (notify-update: false)",
                "&8--------------------------------------------"
        );
    }

    public String getResourceUrl() {
        return "https://www.spigotmc.org/resources/" + resourceId + "/";
    }
}