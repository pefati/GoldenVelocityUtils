package Golden.Velocity.commands.staff;

import Golden.Velocity.Main;
import Golden.Velocity.utils.ConfigManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class MaintenanceCommand {
    private final Main plugin;
    private final ConfigManager configManager;

    public MaintenanceCommand(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
            .<CommandSource>literal("maintenance")
            .requires(source -> source.hasPermission("gbu.staff.maintenance"))
            .then(LiteralArgumentBuilder.<CommandSource>literal("on")
                .executes(context -> {
                    configManager.set("maintenance.enabled", true);
                    configManager.saveConfig();
                    context.getSource().sendMessage(configManager.getMessage("maintenance-enabled"));
                    return 1;
                }))
            .then(LiteralArgumentBuilder.<CommandSource>literal("off")
                .executes(context -> {
                    configManager.set("maintenance.enabled", false);
                    configManager.saveConfig();
                    context.getSource().sendMessage(configManager.getMessage("maintenance-disabled"));
                    return 1;
                }))
            .then(LiteralArgumentBuilder.<CommandSource>literal("add")
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                    .executes(context -> {
                        String playerName = context.getArgument("player", String.class);
                        
                        List<String> whitelist = configManager.getStringList("maintenance.whitelist");
                        if (!whitelist.contains(playerName)) {
                            whitelist.add(playerName);
                            configManager.set("maintenance.whitelist", whitelist);
                            configManager.saveConfig();
                            
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("player", playerName);
                            context.getSource().sendMessage(
                                configManager.getMessage("maintenance-added", placeholders));
                        }
                        
                        return 1;
                    })))
            .then(LiteralArgumentBuilder.<CommandSource>literal("remove")
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                    .executes(context -> {
                        String playerName = context.getArgument("player", String.class);
                        
                        List<String> whitelist = configManager.getStringList("maintenance.whitelist");
                        if (whitelist.remove(playerName)) {
                            configManager.set("maintenance.whitelist", whitelist);
                            configManager.saveConfig();
                            
                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("player", playerName);
                            context.getSource().sendMessage(
                                configManager.getMessage("maintenance-removed", placeholders));
                        }
                        
                        return 1;
                    })))
            .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                .executes(context -> {
                    List<String> whitelist = configManager.getStringList("maintenance.whitelist");
                    
                    if (whitelist.isEmpty()) {
                        context.getSource().sendMessage(configManager.getMessage("maintenance-list-empty"));
                        return 1;
                    }
                    
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("count", String.valueOf(whitelist.size()));
                    context.getSource().sendMessage(
                        configManager.getMessage("maintenance-list-header", placeholders));
                    
                    for (String name : whitelist) {
                        context.getSource().sendMessage(
                            Component.text("  - " + name, NamedTextColor.YELLOW));
                    }
                    
                    return 1;
                }))
            .executes(context -> {
                context.getSource().sendMessage(configManager.getMessage("usage-maintenance"));
                return 0;
            })
            .build();

        return new BrigadierCommand(node);
    }
}
