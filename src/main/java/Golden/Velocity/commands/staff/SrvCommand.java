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

public class SrvCommand {
    private final Main plugin;
    private final ConfigManager configManager;

    public SrvCommand(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
                .<CommandSource>literal("srv")
                .requires(source -> source.hasPermission("gbu.staff.srv"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("server", StringArgumentType.word())
                        .executes(context -> {
                            if (!(context.getSource() instanceof Player)) {
                                context.getSource().sendMessage(configManager.getMessage("only-players"));
                                return 0;
                            }

                            Player player = (Player) context.getSource();
                            String serverName = context.getArgument("server", String.class);

                            Optional<RegisteredServer> server = plugin.getProxy().getServer(serverName);

                            if (server.isEmpty()) {
                                Map<String, String> placeholders = new HashMap<>();
                                placeholders.put("server", serverName);
                                player.sendMessage(configManager.getMessage("server-not-found", placeholders));
                                return 0;
                            }

                            Optional<String> currentServer = player.getCurrentServer()
                                    .map(conn -> conn.getServerInfo().getName());

                            if (currentServer.isPresent() && currentServer.get().equals(serverName)) {
                                player.sendMessage(configManager.getMessage("already-connected"));
                                return 0;
                            }

                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("server", serverName);
                            player.sendMessage(configManager.getMessage("connecting", placeholders));

                            player.createConnectionRequest(server.get()).fireAndForget();

                            return 1;
                        }))
                .executes(context -> {
                    context.getSource().sendMessage(configManager.getMessage("usage-srv"));
                    return 0;
                })
                .build();

        return new BrigadierCommand(node);
    }
}
