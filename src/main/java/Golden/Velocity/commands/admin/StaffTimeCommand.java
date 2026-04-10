package Golden.Velocity.commands.admin;

import Golden.Velocity.Main;
import Golden.Velocity.utils.ConfigManager;
import Golden.Velocity.utils.SessionManager;
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
import java.util.UUID;

public class StaffTimeCommand {
    private final Main plugin;
    private final ConfigManager configManager;
    private final SessionManager sessionManager;

    public StaffTimeCommand(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.sessionManager = plugin.getSessionManager();
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
            .<CommandSource>literal("stafftime").requires(source -> source.hasPermission("gbu.admin.stafftime"))
            .then(RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                .executes(context -> {
                    String playerName = context.getArgument("player", String.class);
                    showStaffTime(context.getSource(), playerName);
                    return 1;
                }))
            .executes(context -> {
                context.getSource().sendMessage(configManager.getMessage("usage-stafftime"));
                return 0;
            }).build();

        return new BrigadierCommand(node);
    }

    private void showStaffTime(CommandSource source, String playerName) {
        Optional<Player> optPlayer = plugin.getProxy().getPlayer(playerName);
        
        UUID playerId;
        if (optPlayer.isPresent()) {
            playerId = optPlayer.get().getUniqueId();
        } else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", playerName);
            source.sendMessage(configManager.getMessage("player-not-found", placeholders));
            return;
        }

        long totalTime = sessionManager.getSessionTime(playerId);
        String formattedTime = sessionManager.formatTime(totalTime);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", playerName);
        placeholders.put("time", formattedTime);

        source.sendMessage(configManager.getMessage("stafftime-message", placeholders));
    }
}
