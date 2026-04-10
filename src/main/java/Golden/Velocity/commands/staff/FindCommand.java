package Golden.Velocity.commands.staff;

import Golden.Velocity.Main;
import Golden.Velocity.utils.ConfigManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FindCommand {
    private final Main plugin;
    private final ConfigManager configManager;

    public FindCommand(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
            .<CommandSource>literal("find")
            .requires(source -> source.hasPermission("gbu.staff.find"))
            .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                .executes(context -> {
                    String playerName = context.getArgument("player", String.class);
                    
                    Optional<Player> target = plugin.getProxy().getPlayer(playerName);
                    
                    if (target.isEmpty()) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("player", playerName);
                        context.getSource().sendMessage(configManager.getMessage("player-not-found", placeholders));
                        return 0;
                    }
                    
                    Player targetPlayer = target.get();
                    String serverName = targetPlayer.getCurrentServer()
                        .map(server -> server.getServerInfo().getName())
                        .orElse("Unknown");
                    
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("player", targetPlayer.getUsername());
                    placeholders.put("server", serverName);
                    
                    context.getSource().sendMessage(configManager.getMessage("find-message", placeholders));
                    return 1;
                }))
            .executes(context -> {
                context.getSource().sendMessage(configManager.getMessage("usage-find"));
                return 0;
            })
            .build();

        return new BrigadierCommand(node);
    }
}
