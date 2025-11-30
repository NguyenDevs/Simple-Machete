package com.NguyenDevs.simpleMachete;

import com.NguyenDevs.simpleMachete.commands.MacheteCommand;
import com.NguyenDevs.simpleMachete.hooks.WorldGuardHook;
import com.NguyenDevs.simpleMachete.listeners.SweepListener;
import com.NguyenDevs.simpleMachete.managers.ConfigManager;
import com.NguyenDevs.simpleMachete.managers.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleMachete extends JavaPlugin {

    private static SimpleMachete instance;
    private ConfigManager configManager;
    private LanguageManager languageManager;

    @Override
    public void onLoad() {
         if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&b[&eSimple&6Machete&b] &eWorldGuard detected! Registering flags..."));
            try {
                WorldGuardHook.register();
                if (WorldGuardHook.isEnabled()) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&b[&eSimple&6Machete&b] &aFlag 'sm-break' registered successfully!"));
                } else {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                            "&b[&eSimple&6Machete&b] &cFlag registration failed!"));
                }
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&b[&eSimple&6Machete&b] &cError registering WorldGuard flag: " + e.getMessage()));
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        // Print logo
        printLogo();

        // Initialize managers
        configManager = new ConfigManager(this);
        languageManager = new LanguageManager(this);

        // Load configs
        configManager.loadConfigs();
        languageManager.loadLanguage();

        // Check WorldGuard hook status
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            if (WorldGuardHook.isEnabled()) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&b[&eSimple&6Machete&b] &aWorldGuard hook successful! Region protection enabled."));
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&b[&eSimple&6Machete&b] &cWorldGuard hook failed! Running without protection."));
            }
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&b[&eSimple&6Machete&b] &eWorldGuard not found. Running without region protection."));
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new SweepListener(this), this);

        // Register commands
        getCommand("simplemachete").setExecutor(new MacheteCommand(this));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&eSimple&6Machete&b] &aSimpleMachete plugin enabled successfully!!"));
    }

    public void printLogo() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&e   ███████╗██╗███╗   ███╗██████╗ ██╗     ███████╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&e   ██╔════╝██║████╗ ████║██╔══██╗██║     ██╔════╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&e   ███████╗██║██╔████╔██║██████╔╝██║     █████╗  "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&e   ╚════██║██║██║╚██╔╝██║██╔═══╝ ██║     ██╔══╝  "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&e   ███████║██║██║ ╚═╝ ██║██║     ███████╗███████╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&e   ╚══════╝╚═╝╚═╝     ╚═╝╚═╝     ╚══════╝╚══════╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6   ███╗   ███╗ █████╗  ██████╗██╗  ██╗███████╗████████╗███████╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6   ████╗ ████║██╔══██╗██╔════╝██║  ██║██╔════╝╚══██╔══╝██╔════╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6   ██╔████╔██║███████║██║     ███████║█████╗     ██║   █████╗  "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6   ██║╚██╔╝██║██╔══██║██║     ██╔══██║██╔══╝     ██║   ██╔══╝  "));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6   ██║ ╚═╝ ██║██║  ██║╚██████╗██║  ██║███████╗   ██║   ███████╗"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6   ╚═╝     ╚═╝╚═╝  ╚═╝ ╚═════╝╚═╝  ╚═╝╚══════╝   ╚═╝   ╚══════╝"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&e         Simple Machete"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&6         Version " + getDescription().getVersion()));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b         Development by NguyenDevs"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', ""));
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&b[&eSimple&6Machete&b] &aSimpleMachete has been disabled!"));
    }

    public static SimpleMachete getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public void reload() {
        configManager.loadConfigs();
        languageManager.loadLanguage();
    }
}