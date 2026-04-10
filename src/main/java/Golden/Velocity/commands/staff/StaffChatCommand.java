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
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;

public class StaffChatCommand {
    private final Main plugin;
    private final ConfigManager configManager;

    public StaffChatCommand(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
            .<CommandSource>literal("staffchat")
            .requires(source -> source.hasPermission("gbu.staff.chat"))
            .then(RequiredArgumentBuilder.<CommandSource, String>argument("message", StringArgumentType.greedyString())
                .executes(context -> {
                    if (!(context.getSource() instanceof Player)) {
                        context.getSource().sendMessage(configManager.getMessage("only-players"));
                        return 0;
                    }
                    
                    Player player = (Player) context.getSource();
                    String message = context.getArgument("message", String.class);
                    
                    Component staffMessage = plugin.getChatManager().formatStaffChatMessage(player, message);
                    
                    plugin.getProxy().getAllPlayers().stream()
                        .filter(p -> p.hasPermission("gbu.staff.chat"))
                        .forEach(staff -> staff.sendMessage(staffMessage));
                    
                    return 1;
                }))
            .executes(context -> {
                if (!(context.getSource() instanceof Player)) {
                    context.getSource().sendMessage(configManager.getMessage("only-players"));
                    return 0;
                }
                Player player = (Player) context.getSource();
                plugin.getChatManager().toggleStaffChat(player.getUniqueId());
                boolean enabled = plugin.getChatManager().isInStaffChat(player.getUniqueId());
                player.sendMessage(Component.text(enabled ? "§aStaffChat mode enabled!" : "§cStaffChat mode disabled!"));
                
                sendToggleUpdate(player, "StaffChat", enabled);
                return 1;
            })
            .build();

        return new BrigadierCommand(node);
    }

    public BrigadierCommand createAliasCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
            .<CommandSource>literal("sc")
            .requires(source -> source.hasPermission("gbu.staff.chat"))
            .then(RequiredArgumentBuilder.<CommandSource, String>argument("message", StringArgumentType.greedyString())
                .executes(context -> {
                    if (!(context.getSource() instanceof Player)) {
                        context.getSource().sendMessage(configManager.getMessage("only-players"));
                        return 0;
                    }
                    
                    Player player = (Player) context.getSource();
                    String message = context.getArgument("message", String.class);
                    
                    Component staffMessage = plugin.getChatManager().formatStaffChatMessage(player, message);
                    
                    plugin.getProxy().getAllPlayers().stream()
                        .filter(p -> p.hasPermission("gbu.staff.chat"))
                        .forEach(staff -> staff.sendMessage(staffMessage));
                    
                    return 1;
                }))
            .executes(context -> {
                if (!(context.getSource() instanceof Player)) {
                    context.getSource().sendMessage(configManager.getMessage("only-players"));
                    return 0;
                }
                Player player = (Player) context.getSource();
                plugin.getChatManager().toggleStaffChat(player.getUniqueId());
                boolean enabled = plugin.getChatManager().isInStaffChat(player.getUniqueId());
                player.sendMessage(Component.text(enabled ? "§aStaffChat mode enabled!" : "§cStaffChat mode disabled!"));
                
                sendToggleUpdate(player, "StaffChat", enabled);
                return 1;
            })
            .build();

        return new BrigadierCommand(node);
    }

    private void sendToggleUpdate(Player player, String channel, boolean enabled) {
        player.getCurrentServer().ifPresent(serverConnection -> {
            RegisteredServer server = serverConnection.getServer();
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(channel);
            out.writeUTF(player.getUniqueId().toString());
            out.writeBoolean(enabled);
            server.sendPluginMessage(MinecraftChannelIdentifier.from("gbu:toggle"), out.toByteArray());
        });
    }
}
