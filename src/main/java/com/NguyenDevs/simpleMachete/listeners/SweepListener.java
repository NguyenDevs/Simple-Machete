package com.NguyenDevs.simpleMachete.listeners;

import com.NguyenDevs.simpleMachete.SimpleMachete;
import com.NguyenDevs.simpleMachete.hooks.WorldGuardHook;
import com.NguyenDevs.simpleMachete.managers.ConfigManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class SweepListener implements Listener {

    private final SimpleMachete plugin;
    private final ConfigManager configManager;
    private final Set<UUID> cooldowns = new HashSet<>();

    public SweepListener(SimpleMachete plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only trigger on left click (attack)
        if (event.getAction() != Action.LEFT_CLICK_AIR &&
                event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        // Check permission
        if (!player.hasPermission("simplemachete.use")) {
            return;
        }

        // Check if world is enabled
        if (!configManager.getEnabledWorlds().contains(player.getWorld().getName())) {
            return;
        }

        // Check if on cooldown
        if (cooldowns.contains(player.getUniqueId())) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if item is null or air
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        // Check if item is allowed sword
        if (!configManager.getAllowedSwords().contains(item.getType())) {
            return;
        }

        // Check for Sweeping Edge enchantment if required
        if (configManager.requireSweepingEdge()) {
            if (!item.containsEnchantment(Enchantment.SWEEPING_EDGE)) {
                return;
            }
        }

        // Check if attack is on cooldown (full charge)
        float attackCooldown = player.getAttackCooldown();
        if (attackCooldown < 0.9f) { // 90% charged
            return;
        }

        // Check if player is targeting an entity - DON'T break blocks if attacking mobs
        if (isTargetingEntity(player)) {
            return; // Skip block breaking when attacking entities
        }

        // Add cooldown
        cooldowns.add(player.getUniqueId());

        // Get cooldown duration from config
        int cooldownTicks = configManager.getCooldownTicks();

        // Set item cooldown (visual feedback on hotbar)
        player.setCooldown(item.getType(), cooldownTicks);

        // Remove cooldown after configured time
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            cooldowns.remove(player.getUniqueId());
        }, cooldownTicks);

        // Perform sweep
        performSweep(player, item);
    }

    /**
     * Check if player is targeting an entity (mob, player, etc.)
     * @param player The player to check
     * @return true if targeting an entity, false otherwise
     */
    private boolean isTargetingEntity(Player player) {
        // Raycast to detect if looking at an entity
        double maxDistance = 5.0; // Sword attack range (can be configured)

        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                maxDistance,
                0.5, // Entity bounding box expansion
                entity -> entity != player && entity instanceof org.bukkit.entity.LivingEntity
        );

        return result != null && result.getHitEntity() != null;
    }

    private void performSweep(Player player, ItemStack sword) {
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize();
        int radius = configManager.getRadius();
        Set<Material> breakableBlocks = configManager.getBreakableBlocks();

        // Play sweep sound
        player.getWorld().playSound(eyeLoc, Sound.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS, 1.0f, 1.0f);

        // Play sweep particle at a fixed position relative to player
        Location sweepLoc = eyeLoc.clone().add(direction.clone().multiply(1.5)).add(0, -0.3, 0);
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, sweepLoc, 1, 0, 0, 0, 0);

        // Collect blocks organized by distance layers (like onion layers)
        Map<Integer, List<Block>> blocksByLayer = new TreeMap<>();

        // Start from player's position (0 distance) to max radius
        for (double dist = 0; dist <= radius; dist += 0.5) {
            // Create a sphere at this distance
            for (double theta = 0; theta < Math.PI * 2; theta += 0.3) {
                for (double phi = 0; phi < Math.PI; phi += 0.3) {
                    // Spherical coordinates
                    double x = dist * Math.sin(phi) * Math.cos(theta);
                    double y = dist * Math.sin(phi) * Math.sin(theta);
                    double z = dist * Math.cos(phi);

                    Vector offset = new Vector(x, y, z);

                    // Rotate to player's view direction
                    offset = rotateVector(offset, direction);

                    Location blockLoc = eyeLoc.clone().add(offset);
                    Block block = blockLoc.getBlock();

                    // Check if block is breakable
                    if (!breakableBlocks.contains(block.getType())) {
                        continue;
                    }

                    // Calculate exact distance
                    double exactDistance = eyeLoc.distance(block.getLocation());

                    // Check if within radius
                    if (exactDistance > radius + 0.5) {
                        continue;
                    }

                    // Check if block is in front of player
                    Vector toBlock = block.getLocation().toVector().subtract(eyeLoc.toVector());
                    if (toBlock.lengthSquared() > 0) {
                        toBlock.normalize();
                        double dot = toBlock.dot(direction);

                        // Wider cone (90 degrees)
                        if (dot < 0.3) {
                            continue;
                        }
                    }

                    // Group by layer (round to nearest 0.5 block for smoother waves)
                    int layer = (int) Math.round(exactDistance * 2); // Multiply by 2 for finer layers
                    blocksByLayer.computeIfAbsent(layer, l -> new ArrayList<>()).add(block);
                }
            }
        }

        // Break blocks in waves (layer by layer - like onion peeling)
        int delay = 0;
        int totalBlocks = 0;
        int waveSpeed = configManager.getWaveSpeedTicks();

        for (Map.Entry<Integer, List<Block>> entry : blocksByLayer.entrySet()) {
            List<Block> layerBlocks = entry.getValue();
            totalBlocks += layerBlocks.size();

            // Schedule breaking for entire layer at once
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Play single sound for the entire layer (not per block)
                    if (!layerBlocks.isEmpty()) {
                        Location avgLoc = getAverageLocation(layerBlocks);
                        Sound layerSound = getBreakSound(layerBlocks.get(0).getType());
                        player.getWorld().playSound(avgLoc, layerSound,
                                SoundCategory.BLOCKS, 0.5f, 1.0f);
                    }

                    // Break all blocks in this layer simultaneously
                    for (Block block : layerBlocks) {
                        breakBlock(player, block);
                    }
                }
            }.runTaskLater(plugin, delay);

            delay += waveSpeed; // Configurable wave speed
        }

        // Damage sword based on total blocks broken
        if (totalBlocks > 0) {
            final int finalTotalBlocks = totalBlocks;
            new BukkitRunnable() {
                @Override
                public void run() {
                    damageSword(player, sword, finalTotalBlocks);
                }
            }.runTaskLater(plugin, delay + 1);
        }
    }

    private Vector rotateVector(Vector vec, Vector direction) {
        // Calculate yaw and pitch from direction
        double yaw = Math.atan2(direction.getZ(), direction.getX());
        double pitch = Math.asin(-direction.getY());

        // Rotate around Y axis (yaw)
        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);
        double x = vec.getX() * cosYaw - vec.getZ() * sinYaw;
        double z = vec.getX() * sinYaw + vec.getZ() * cosYaw;

        // Rotate around X axis (pitch)
        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);
        double y = vec.getY() * cosPitch - z * sinPitch;
        z = vec.getY() * sinPitch + z * cosPitch;

        return new Vector(x, y, z);
    }

    private void breakBlock(Player player, Block block) {
        // Check WorldGuard protection
        if (WorldGuardHook.isEnabled() && !WorldGuardHook.canBreak(player, block.getLocation())) {
            return; // Skip this block if protected
        }

        Location blockLoc = block.getLocation().add(0.5, 0.5, 0.5);

        // Play block break particle
        player.getWorld().spawnParticle(Particle.BLOCK_CRACK,
                blockLoc, 20, 0.3, 0.3, 0.3, 0.1, block.getBlockData());

        // Break block and drop items (no sound here - played per layer instead)
        block.breakNaturally(player.getInventory().getItemInMainHand());
    }

    private Location getAverageLocation(List<Block> blocks) {
        double x = 0, y = 0, z = 0;
        for (Block block : blocks) {
            Location loc = block.getLocation();
            x += loc.getX();
            y += loc.getY();
            z += loc.getZ();
        }
        int size = blocks.size();
        return new Location(blocks.get(0).getWorld(), x / size, y / size, z / size);
    }

    private Sound getBreakSound(Material material) {
        String matName = material.name();

        if (matName.contains("LEAVES")) {
            return Sound.BLOCK_GRASS_BREAK;
        } else if (matName.contains("GRASS") || matName.contains("FERN")) {
            return Sound.BLOCK_GRASS_BREAK;
        } else if (matName.contains("MOSS")) {
            return Sound.BLOCK_MOSS_BREAK;
        } else if (matName.contains("SUGAR_CANE") || matName.contains("BAMBOO")) {
            return Sound.BLOCK_BAMBOO_BREAK;
        } else if (matName.contains("VINE")) {
            return Sound.BLOCK_VINE_BREAK;
        } else if (matName.contains("FLOWER") || matName.contains("ROSE") ||
                matName.contains("TULIP") || matName.contains("ORCHID") ||
                matName.contains("DANDELION") || matName.contains("POPPY")) {
            return Sound.BLOCK_GRASS_BREAK;
        } else {
            return Sound.BLOCK_GRASS_BREAK;
        }
    }

    private void damageSword(Player player, ItemStack sword, int totalBlocks) {
        // Check if durability damage is enabled
        if (!configManager.isDurabilityDamageEnabled()) {
            return;
        }

        if (sword.getType().getMaxDurability() <= 0) {
            return;
        }

        // Get configured damage per sweep
        int configuredDamage = configManager.getDurabilityDamage();

        // Apply unbreaking enchantment logic
        int unbreakingLevel = sword.getEnchantmentLevel(Enchantment.DURABILITY);
        int actualDamage = 0;

        // Roll for each damage point
        for (int i = 0; i < configuredDamage; i++) {
            // Unbreaking gives a chance to not consume durability
            // Unbreaking I: 50% chance, II: 66.67%, III: 75%
            if (Math.random() > (1.0 / (unbreakingLevel + 1.0))) {
                continue;
            }
            actualDamage++;
        }

        if (actualDamage > 0) {
            // Damage the sword
            short newDurability = (short) (sword.getDurability() + actualDamage);

            // Check if sword should break
            if (newDurability >= sword.getType().getMaxDurability()) {
                player.getWorld().playSound(player.getLocation(),
                        Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                player.getInventory().setItemInMainHand(null);
            } else {
                sword.setDurability(newDurability);
            }
        }
    }
}