package com.NguyenDevs.simpleMachete.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardHook {

    private static StateFlag SM_BREAK_FLAG;
    private static boolean enabled = false;

    public static void register() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("sm-break", true);
            registry.register(flag);
            SM_BREAK_FLAG = flag;
            enabled = true;
        } catch (FlagConflictException e) {
            // Flag already registered
            Flag<?> existing = registry.get("sm-break");
            if (existing instanceof StateFlag) {
                SM_BREAK_FLAG = (StateFlag) existing;
                enabled = true;
            }
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static boolean canBreak(Player player, Location location) {
        if (!enabled || SM_BREAK_FLAG == null) {
            return true;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(location);

        // Check if sm-break flag allows breaking
        // If flag is not set or is ALLOW, return true
        // If flag is DENY, return false
        return query.testState(loc, WorldGuardPlugin.inst().wrapPlayer(player), SM_BREAK_FLAG);
    }
}