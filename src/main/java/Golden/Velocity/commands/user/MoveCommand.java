package Golden.Velocity.commands.user;

import Golden.Velocity.Main;
import Golden.Velocity.utils.ConfigManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MoveCommand {
    private final Main plugin;
    private final ConfigManager configManager;
    private final String commandName;
    private final String targetServerKey;

    public MoveCommand(Main plugin, String commandName, String targetServerKey) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.commandName = commandName;
        this.targetServerKey = targetServerKey;
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
                .<CommandSource>literal(commandName)
                .requires(source -> source.hasPermission("gbu.command." + commandName))
                .executes(context -> {
                    if (!(context.getSource() instanceof Player)) {
                        context.getSource().sendMessage(configManager.getMessage("only-players"));
                        return 0;
                    }

                    Player player = (Player) context.getSource();

                    String targetServerName = configManager.getString("servers." + targetServerKey, targetServerKey);
                    Optional<RegisteredServer> server = plugin.getProxy().getServer(targetServerName);

                    if (server.isEmpty()) {
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("server", targetServerName);
                        player.sendMessage(configManager.getMessage("server-not-found", placeholders));
                        return 0;
                    }

                    Optional<String> currentServer = player.getCurrentServer()
                            .map(conn -> conn.getServerInfo().getName());

                    if (currentServer.isPresent() && currentServer.get().equals(targetServerName)) {
                        player.sendMessage(configManager.getMessage("already-connected"));
                        return 0;
                    }

                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("server", targetServerName);
                    player.sendMessage(configManager.getMessage("connecting", placeholders));

                    player.createConnectionRequest(server.get()).fireAndForget();

                    return 1;
                })
                .build();

        return new BrigadierCommand(node);
    }
}
