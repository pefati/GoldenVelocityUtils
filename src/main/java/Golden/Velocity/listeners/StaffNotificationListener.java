package Golden.Velocity.listeners;

import Golden.Velocity.Main;
import Golden.Velocity.utils.ChatManager;
import Golden.Velocity.utils.ConfigManager;
import Golden.Velocity.utils.SessionManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.concurrent.TimeUnit;

import java.util.HashMap;
import java.util.Map;

public class StaffNotificationListener {
    private final Main plugin;
    private final ConfigManager configManager;
    private final ChatManager chatManager;
    private final SessionManager sessionManager;

    public StaffNotificationListener(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.chatManager = plugin.getChatManager();
        this.sessionManager = plugin.getSessionManager();
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("gbu.staff.notify")) {
            sessionManager.startSession(player.getUniqueId());

            if (player.hasPermission("gbu.staff.chat")) {
                chatManager.enableStaffChat(player.getUniqueId());
            }
        }

        if (player.hasPermission("gbu.staff.notify")) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", player.getUsername());

            plugin.getProxy().getAllPlayers().stream()
                    .filter(p -> p.hasPermission("gbu.staff.notify"))
                    .forEach(staff -> staff.sendMessage(
                            configManager.getMessage("staff-join", placeholders)));
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("gbu.staff.notify")) {
            sessionManager.endSession(player.getUniqueId());
        }

        chatManager.removePlayer(player.getUniqueId());

        if (player.hasPermission("gbu.staff.notify")) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", player.getUsername());

            plugin.getProxy().getAllPlayers().stream()
                    .filter(p -> p.hasPermission("gbu.staff.notify"))
                    .forEach(staff -> staff.sendMessage(
                            configManager.getMessage("staff-quit", placeholders)));
        }
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();

        boolean inStaffChat = chatManager.isInStaffChat(player.getUniqueId());
        boolean inAdminChat = chatManager.isInAdminChat(player.getUniqueId());

        if (inStaffChat) {
            sendToggleUpdate(event.getServer(), player, "StaffChat", true);
        }
        if (inAdminChat) {
            sendToggleUpdate(event.getServer(), player, "AdminChat", true);
        }

        if (event.getPreviousServer().isEmpty()) {
            return;
        }

        if (player.hasPermission("gbu.staff.notify")) {
            String serverName = event.getServer().getServerInfo().getName();

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", player.getUsername());
            placeholders.put("server", serverName);

            plugin.getProxy().getAllPlayers().stream()
                    .filter(p -> p.hasPermission("gbu.staff.notify"))
                    .forEach(staff -> staff.sendMessage(
                            configManager.getMessage("staff-switch", placeholders)));
        }
    }

    private void sendToggleUpdate(RegisteredServer server, Player player, String channel, boolean enabled) {
        plugin.getProxy().getScheduler().buildTask(plugin, () -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(channel);
            out.writeUTF(player.getUniqueId().toString());
            out.writeBoolean(enabled);
            server.sendPluginMessage(MinecraftChannelIdentifier.from("gbu:toggle"), out.toByteArray());
        }).delay(1, TimeUnit.SECONDS).schedule();
    }
}
