package Golden.Velocity.commands.user;

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

public class HelpopCommand {
    private final Main plugin;
    private final ConfigManager configManager;

    public HelpopCommand(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
                .<CommandSource>literal("helpop")
                .requires(source -> source.hasPermission("gbu.user.helpop"))
                .then(RequiredArgumentBuilder
                        .<CommandSource, String>argument("message", StringArgumentType.greedyString())
                        .executes(context -> {
                            if (!(context.getSource() instanceof Player)) {
                                context.getSource().sendMessage(configManager.getMessage("only-players"));
                                return 0;
                            }

                            Player player = (Player) context.getSource();
                            String message = context.getArgument("message", String.class);

                            String serverName = player.getCurrentServer()
                                    .map(server -> server.getServerInfo().getName())
                                    .orElse("Unknown");

                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("player", player.getUsername());
                            placeholders.put("message", message);
                            placeholders.put("server", serverName);

                            plugin.getProxy().getAllPlayers().stream()
                                    .filter(p -> p.hasPermission("gbu.staff.notify"))
                                    .forEach(staff -> staff.sendMessage(
                                            configManager.getMessage("helpop-format", placeholders)));

                            player.sendMessage(configManager.getMessage("helpop-sent"));

                            return 1;
                        }))
                .executes(context -> {
                    context.getSource().sendMessage(configManager.getMessage("usage-helpop"));
                    return 0;
                })
                .build();

        return new BrigadierCommand(node);
    }
}
