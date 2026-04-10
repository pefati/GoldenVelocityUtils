package Golden.Velocity.commands.admin;

import Golden.Velocity.Main;
import Golden.Velocity.utils.ConfigManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.HashMap;
import java.util.Map;

public class GbuCommand {
    private final Main plugin;
    private final ConfigManager configManager;

    public GbuCommand(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
            .<CommandSource>literal("gbu")
            .requires(source -> source.hasPermission("gbu.admin"))
            .executes(context -> {
                showHelp(context.getSource());
                return 1;
            })
            .then(LiteralArgumentBuilder.<CommandSource>literal("reload")
                .requires(source -> source.hasPermission("gbu.admin.reload")).executes(context -> {
                    configManager.reload();
                    context.getSource().sendMessage(configManager.getMessage("reload"));
                    return 1;
                }))
            .then(LiteralArgumentBuilder.<CommandSource>literal("help")
                .requires(source -> source.hasPermission("gbu.admin.help")).executes(context -> {
                    showHelp(context.getSource());
                    return 1;
                })).build();

        return new BrigadierCommand(node);
    }

    private void showHelp(CommandSource source) {
        source.sendMessage(configManager.getMessage("help-header"));
        source.sendMessage(configManager.getMessage("help-title"));
        source.sendMessage(Component.empty());

        if (source.hasPermission("gbu.admin.reload")) {
            addHelpEntry(source, "/gbu reload", "help-desc-reload");
        }
        if (source.hasPermission("gbu.admin.stafftime")) {
            addHelpEntry(source, "/stafftime <player>", "help-desc-stafftime");
        }

        if (source.hasPermission("gbu.staff.alert")) {
            addHelpEntry(source, "/alert <message>", "help-desc-alert");
        }
        if (source.hasPermission("gbu.staff.maintenance")) {
            addHelpEntry(source, "/maintenance <on/off/add/remove/list>", "help-desc-maintenance");
        }
        if (source.hasPermission("gbu.staff.find")) {
            addHelpEntry(source, "/find <player>", "help-desc-find");
        }
        if (source.hasPermission("gbu.staff.goto")) {
            addHelpEntry(source, "/goto <player>", "help-desc-goto");
        }
        if (source.hasPermission("gbu.staff.srv")) {
            addHelpEntry(source, "/srv <server>", "help-desc-srv");
        }
        if (source.hasPermission("gbu.staff.list")) {
            addHelpEntry(source, "/stafflist", "help-desc-stafflist");
        }
        if (source.hasPermission("gbu.staff.chat")) {
            addHelpEntry(source, "/staffchat", "help-desc-staffchat");
        }
        if (source.hasPermission("gbu.admin.chat")) {
            addHelpEntry(source, "/adminchat", "help-desc-adminchat");
        }
        if (source.hasPermission("gbu.user.list")) {
            addHelpEntry(source, "/blist", "help-desc-blist");
        }

        if (source.hasPermission("gbu.user.report")) {
            addHelpEntry(source, "/report <player> <reason>", "help-desc-report");
        }
        if (source.hasPermission("gbu.command.stream")) {
            addHelpEntry(source, "/stream <url>", "help-desc-stream");
        }
        if (source.hasPermission("gbu.user.helpop")) {
            addHelpEntry(source, "/helpop <message>", "help-desc-helpop");
        }

        source.sendMessage(Component.empty());
        source.sendMessage(configManager.getMessage("help-footer"));
    }

    private void addHelpEntry(CommandSource source, String command, String descriptionKey) {
        String description = configManager.getString("messages." + descriptionKey, "No description");
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("command", command);
        placeholders.put("description", description);
        
        Component message = configManager.getMessage("help-command-format", placeholders)
            .hoverEvent(HoverEvent.showText(configManager.getMessage("help-hover")))
            .clickEvent(ClickEvent.suggestCommand(command));
        
        source.sendMessage(message);
    }
}
