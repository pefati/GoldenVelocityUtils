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
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GotoCommand {
    private final Main plugin;
    private final ConfigManager configManager;

    public GotoCommand(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
                .<CommandSource>literal("goto")
                .requires(source -> source.hasPermission("gbu.staff.goto"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                        .executes(context -> {
                            if (!(context.getSource() instanceof Player)) {
                                context.getSource().sendMessage(configManager.getMessage("only-players"));
                                return 0;
                            }

                            Player sender = (Player) context.getSource();
                            String playerName = context.getArgument("player", String.class);

                            Optional<Player> target = plugin.getProxy().getPlayer(playerName);

                            if (target.isEmpty()) {
                                Map<String, String> placeholders = new HashMap<>();
                                placeholders.put("player", playerName);
                                sender.sendMessage(configManager.getMessage("player-not-found", placeholders));
                                return 0;
                            }

                            Player targetPlayer = target.get();
                            Optional<RegisteredServer> targetServer = targetPlayer.getCurrentServer()
                                    .map(serverConnection -> serverConnection.getServer());

                            if (targetServer.isEmpty()) {
                                sender.sendMessage(configManager.getMessage("player-not-found"));
                                return 0;
                            }

                            Optional<String> currentServer = sender.getCurrentServer()
                                    .map(server -> server.getServerInfo().getName());

                            if (currentServer.isPresent() &&
                                    currentServer.get().equals(targetServer.get().getServerInfo().getName())) {
                                sender.sendMessage(configManager.getMessage("already-connected"));
                                return 0;
                            }

                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("player", targetPlayer.getUsername());

                            sender.sendMessage(configManager.getMessage("goto-message", placeholders));
                            sender.createConnectionRequest(targetServer.get()).fireAndForget();

                            return 1;
                        }))
                .executes(context -> {
                    context.getSource().sendMessage(configManager.getMessage("usage-goto"));
                    return 0;
                })
                .build();

        return new BrigadierCommand(node);
    }
}
