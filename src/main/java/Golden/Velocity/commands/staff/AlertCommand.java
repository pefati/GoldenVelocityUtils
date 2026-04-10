package Golden.Velocity.commands.staff;

import Golden.Velocity.Main;
import Golden.Velocity.utils.ConfigManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;

import java.util.HashMap;
import java.util.Map;

public class AlertCommand {
    private final Main plugin;
    private final ConfigManager configManager;

    public AlertCommand(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
            .<CommandSource>literal("alert")
            .requires(source -> source.hasPermission("gbu.staff.alert"))
            .then(RequiredArgumentBuilder.<CommandSource, String>argument("message", StringArgumentType.greedyString())
                .executes(context -> {
                    String message = context.getArgument("message", String.class);
                    
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("message", message);
                    
                    plugin.getProxy().getAllPlayers().forEach(player -> 
                        player.sendMessage(configManager.getMessage("alert-format", placeholders))
                    );
                    
                    return 1;
                }))
            .executes(context -> {
                context.getSource().sendMessage(configManager.getMessage("usage-alert"));
                return 0;
            })
            .build();

        return new BrigadierCommand(node);
    }
}
