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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ReportCommand {
    private final Main plugin;
    private final ConfigManager configManager;
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    public ReportCommand(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
                .<CommandSource>literal("report")
                .requires(source -> source.hasPermission("gbu.user.report"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                        .then(RequiredArgumentBuilder
                                .<CommandSource, String>argument("reason", StringArgumentType.greedyString())
                                .executes(context -> {
                                    if (!(context.getSource() instanceof Player)) {
                                        context.getSource().sendMessage(configManager.getMessage("only-players"));
                                        return 0;
                                    }

                                    Player reporter = (Player) context.getSource();
                                    String targetName = context.getArgument("player", String.class);
                                    String reason = context.getArgument("reason", String.class);

                                    Optional<Player> target = plugin.getProxy().getPlayer(targetName);
                                    if (target.isEmpty()) {
                                        Map<String, String> placeholders = new HashMap<>();
                                        placeholders.put("player", targetName);
                                        reporter.sendMessage(
                                                configManager.getMessage("player-not-found", placeholders));
                                        return 0;
                                    }

                                    Player targetPlayer = target.get();
                                    String targetServer = targetPlayer.getCurrentServer()
                                            .map(server -> server.getServerInfo().getName())
                                            .orElse("Unknown");

                                    Map<String, String> placeholders = new HashMap<>();
                                    placeholders.put("player", reporter.getUsername());
                                    placeholders.put("target", targetPlayer.getUsername());
                                    placeholders.put("reason", reason);

                                    Component reportMessage = Component.text()
                                            .append(configManager.getMessage("report-broadcast-header"))
                                            .append(Component.newline())
                                            .append(configManager.getMessage("report-broadcast-title", placeholders))
                                            .append(Component.newline())
                                            .append(configManager.getMessage("report-broadcast-target", placeholders))
                                            .append(Component.newline())
                                            .append(configManager.getMessage("report-broadcast-reason", placeholders))
                                            .append(Component.newline())
                                            .append(configManager.getMessage("report-broadcast-action")
                                                    .clickEvent(ClickEvent
                                                            .runCommand("/goto " + targetPlayer.getUsername()))
                                                    .hoverEvent(HoverEvent.showText(SERIALIZER.deserialize(
                                                            "&7Click to teleport to &e" + targetPlayer.getUsername()))))
                                            .append(Component.newline())
                                            .append(configManager.getMessage("report-broadcast-footer"))
                                            .build();

                                    plugin.getProxy().getAllPlayers().stream()
                                            .filter(p -> p.hasPermission("gbu.staff.notify"))
                                            .forEach(staff -> staff.sendMessage(reportMessage));

                                    reporter.sendMessage(configManager.getMessage("report-sent"));

                                    return 1;
                                })))
                .executes(context -> {
                    context.getSource().sendMessage(configManager.getMessage("usage-report"));
                    return 0;
                })
                .build();

        return new BrigadierCommand(node);
    }
}
