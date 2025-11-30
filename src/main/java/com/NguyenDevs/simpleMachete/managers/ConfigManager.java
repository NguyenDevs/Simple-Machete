package com.NguyenDevs.simpleMachete.managers;

import com.NguyenDevs.simpleMachete.SimpleMachete;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ConfigManager {

    private final SimpleMachete plugin;
    private FileConfiguration config;
    private File configFile;

    public ConfigManager(SimpleMachete plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Add missing config options
        addDefaultValues();
        saveConfig();
    }

    private void addDefaultValues() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("notify-update", true);
        defaults.put("settings.radius", 5);
        defaults.put("settings.require-sweeping-edge", true);
        defaults.put("settings.cooldown-ticks", 10);
        defaults.put("settings.durability-damage", 1);
        defaults.put("settings.enable-durability-damage", true);
        defaults.put("settings.wave-speed-ticks", 1);
        defaults.put("settings.enable-world", Arrays.asList("world", "world_nether", "world_the_end"));

        defaults.put("allowed-swords", Arrays.asList(
                "WOODEN_SWORD",
                "STONE_SWORD",
                "IRON_SWORD",
                "GOLDEN_SWORD",
                "DIAMOND_SWORD",
                "NETHERITE_SWORD"
        ));

        defaults.put("breakable-blocks", Arrays.asList(
                "SHORT_GRASS",
                "TALL_GRASS",
                "FERN",
                "LARGE_FERN",
                "DEAD_BUSH",
                "SEAGRASS",
                "TALL_SEAGRASS",
                "DANDELION",
                "POPPY",
                "BLUE_ORCHID",
                "ALLIUM",
                "AZURE_BLUET",
                "RED_TULIP",
                "ORANGE_TULIP",
                "WHITE_TULIP",
                "PINK_TULIP",
                "OXEYE_DAISY",
                "CORNFLOWER",
                "LILY_OF_THE_VALLEY",
                "WITHER_ROSE",
                "SUNFLOWER",
                "LILAC",
                "ROSE_BUSH",
                "PEONY",
                "OAK_LEAVES",
                "SPRUCE_LEAVES",
                "BIRCH_LEAVES",
                "JUNGLE_LEAVES",
                "ACACIA_LEAVES",
                "DARK_OAK_LEAVES",
                "AZALEA_LEAVES",
                "FLOWERING_AZALEA_LEAVES",
                "MOSS_CARPET",
                "MOSS_BLOCK",
                "WHEAT",
                "CARROTS",
                "POTATOES",
                "BEETROOTS",
                "SUGAR_CANE",
                "BAMBOO",
                "VINE",
                "GLOW_BERRIES",
                "CAVE_VINES",
                "CAVE_VINES_PLANT"
        ));

        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            if (!config.contains(entry.getKey())) {
                config.set(entry.getKey(), entry.getValue());
            }
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml!");
            e.printStackTrace();
        }
    }

    public boolean getNotifyUpdate() { return config.getBoolean("notify-update", true);}

    public int getRadius() {
        return config.getInt("settings.radius", 3);
    }

    public boolean requireSweepingEdge() {
        return config.getBoolean("settings.require-sweeping-edge", true);
    }

    public int getCooldownTicks() {
        return config.getInt("settings.cooldown-ticks", 20);
    }

    public int getDurabilityDamage() {
        return config.getInt("settings.durability-damage", 1);
    }

    public boolean isDurabilityDamageEnabled() {
        return config.getBoolean("settings.enable-durability-damage", true);
    }

    public int getWaveSpeedTicks() {
        return config.getInt("settings.wave-speed-ticks", 1);
    }

    public List<String> getEnabledWorlds() {
        return config.getStringList("settings.enable-world");
    }

    public Set<Material> getAllowedSwords() {
        Set<Material> swords = new HashSet<>();
        List<String> swordList = config.getStringList("allowed-swords");

        for (String sword : swordList) {
            try {
                Material mat = Material.valueOf(sword.toUpperCase());
                swords.add(mat);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in allowed-swords: " + sword);
            }
        }

        return swords;
    }

    public Set<Material> getBreakableBlocks() {
        Set<Material> blocks = new HashSet<>();
        List<String> blockList = config.getStringList("breakable-blocks");

        for (String block : blockList) {
            try {
                Material mat = Material.valueOf(block.toUpperCase());
                blocks.add(mat);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in breakable-blocks: " + block);
            }
        }

        return blocks;
    }
}