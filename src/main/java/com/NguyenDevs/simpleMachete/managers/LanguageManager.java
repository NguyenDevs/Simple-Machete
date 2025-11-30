package com.NguyenDevs.simpleMachete.managers;

import com.NguyenDevs.simpleMachete.SimpleMachete;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {

    private final SimpleMachete plugin;
    private FileConfiguration langConfig;
    private File langFile;

    public LanguageManager(SimpleMachete plugin) {
        this.plugin = plugin;
    }

    public void loadLanguage() {
        langFile = new File(plugin.getDataFolder(), "language.yml");

        if (!langFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                langFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create language.yml!");
                e.printStackTrace();
            }
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        addDefaultMessages();
        saveLanguage();
    }

    private void addDefaultMessages() {
        Map<String, String> defaults = new HashMap<>();

        defaults.put("prefix", "&8[&6SimpleMachete&8] &r");
        defaults.put("reload-success", "&aPlugin reloaded successfully!");
        defaults.put("no-permission", "&cYou don't have permission to use this command!");
        defaults.put("player-only", "&cThis command can only be used by players!");
        defaults.put("usage", "&cUsage: /simplemachete reload");

        for (Map.Entry<String, String> entry : defaults.entrySet()) {
            if (!langConfig.contains(entry.getKey())) {
                langConfig.set(entry.getKey(), entry.getValue());
            }
        }
    }

    public void saveLanguage() {
        try {
            langConfig.save(langFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save language.yml!");
            e.printStackTrace();
        }
    }

    public String getMessage(String path) {
        String message = langConfig.getString(path, "&cMessage not found: " + path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getPrefix() {
        return getMessage("prefix");
    }

    public String getPrefixedMessage(String path) {
        return getPrefix() + getMessage(path);
    }
}