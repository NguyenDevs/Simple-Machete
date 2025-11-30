package com.NguyenDevs.simpleMachete.commands;

import com.NguyenDevs.simpleMachete.SimpleMachete;
import com.NguyenDevs.simpleMachete.managers.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MacheteCommand implements CommandExecutor, TabCompleter {

    private final SimpleMachete plugin;
    private final LanguageManager languageManager;

    public MacheteCommand(SimpleMachete plugin) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sender.sendMessage(languageManager.getPrefixedMessage("usage"));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("simplemachete.admin")) {
                sender.sendMessage(languageManager.getPrefixedMessage("no-permission"));
                return true;
            }

            plugin.reload();
            sender.sendMessage(languageManager.getPrefixedMessage("reload-success"));
            return true;
        }

        sender.sendMessage(languageManager.getPrefixedMessage("usage"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("simplemachete.admin")) {
                completions.add("reload");
            }

            // Filter based on what user has typed
            String input = args[0].toLowerCase();
            completions.removeIf(s -> !s.toLowerCase().startsWith(input));
        }

        return completions;
    }
}