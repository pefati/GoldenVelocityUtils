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

public class StreamCommand {
    private final Main plugin;
    private final ConfigManager configManager;
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    public StreamCommand(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
                .<CommandSource>literal("stream")
                .requires(source -> source.hasPermission("gbu.command.stream"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("url", StringArgumentType.word())
                        .executes(context -> {
                            if (!(context.getSource() instanceof Player)) {
                                context.getSource().sendMessage(configManager.getMessage("only-players"));
                                return 0;
                            }

                            Player player = (Player) context.getSource();
                            String url = context.getArgument("url", String.class);

                            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                url = "https://" + url;
                            }

                            Map<String, String> placeholders = new HashMap<>();
                            placeholders.put("player", player.getUsername());

                            Object value = configManager.getMessages().get("stream-broadcast");
                            if (value instanceof java.util.List) {
                                java.util.List<?> lines = (java.util.List<?>) value;
                                var messageBuilder = Component.text();

                                for (int i = 0; i < lines.size(); i++) {
                                    String line = lines.get(i).toString();

                                    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                                        line = line.replace("%" + entry.getKey() + "%", entry.getValue());
                                    }

                                    if (line.contains("<click>") && line.contains("</click>")) {
                                        String beforeClick = line.substring(0, line.indexOf("<click>"));
                                        String clickText = line.substring(line.indexOf("<click>") + 7,
                                                line.indexOf("</click>"));
                                        String afterClick = line.substring(line.indexOf("</click>") + 8);

                                        Component lineComponent = Component.text()
                                                .append(SERIALIZER.deserialize(beforeClick))
                                                .append(SERIALIZER.deserialize(clickText)
                                                        .clickEvent(ClickEvent.openUrl(url))
                                                        .hoverEvent(HoverEvent.showText(configManager
                                                                .getMessage("stream-hover-text", placeholders))))
                                                .append(SERIALIZER.deserialize(afterClick))
                                                .build();

                                        messageBuilder.append(lineComponent);
                                    } else {
                                        messageBuilder.append(SERIALIZER.deserialize(line));
                                    }

                                    if (i < lines.size() - 1) {
                                        messageBuilder.append(Component.newline());
                                    }
                                }

                                Component finalMessage = messageBuilder.build();

                                plugin.getProxy().getAllPlayers().forEach(p -> p.sendMessage(finalMessage));
                            }

                            return 1;
                        }))
                .executes(context -> {
                    context.getSource().sendMessage(configManager.getMessage("usage-stream"));
                    return 0;
                })
                .build();

        return new BrigadierCommand(node);
    }
}
