package Golden.Velocity.utils;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatManager {
    private final Set<UUID> staffChatEnabled = ConcurrentHashMap.newKeySet();
    private final Set<UUID> adminChatEnabled = ConcurrentHashMap.newKeySet();
    private final ConfigManager configManager;
    private final LuckPermsUtils luckPermsUtils;

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    public ChatManager(ConfigManager configManager, LuckPermsUtils luckPermsUtils) {
        this.configManager = configManager;
        this.luckPermsUtils = luckPermsUtils;
    }

    public boolean isInStaffChat(UUID uuid) {
        return staffChatEnabled.contains(uuid);
    }

    public boolean isInAdminChat(UUID uuid) {
        return adminChatEnabled.contains(uuid);
    }

    public void toggleStaffChat(UUID uuid) {
        if (staffChatEnabled.contains(uuid)) {
            staffChatEnabled.remove(uuid);
        } else {
            staffChatEnabled.add(uuid);
            adminChatEnabled.remove(uuid);
        }
    }

    public void toggleAdminChat(UUID uuid) {
        if (adminChatEnabled.contains(uuid)) {
            adminChatEnabled.remove(uuid);
        } else {
            adminChatEnabled.add(uuid);
            staffChatEnabled.remove(uuid);
        }
    }

    public void enableStaffChat(UUID uuid) {
        staffChatEnabled.add(uuid);
        adminChatEnabled.remove(uuid);
    }

    public void enableAdminChat(UUID uuid) {
        adminChatEnabled.add(uuid);
        staffChatEnabled.remove(uuid);
    }

    public void disableStaffChat(UUID uuid) {
        staffChatEnabled.remove(uuid);
    }

    public void disableAdminChat(UUID uuid) {
        adminChatEnabled.remove(uuid);
    }

    public void removePlayer(UUID uuid) {
        staffChatEnabled.remove(uuid);
        adminChatEnabled.remove(uuid);
    }

    public Component formatStaffChatMessage(Player player, String message) {
        String prefix = luckPermsUtils.getPrefix(player);
        String serverName = player.getCurrentServer()
                .map(server -> server.getServerInfo().getName())
                .orElse("Unknown");

        String format = configManager.getMessages().get("staffchat-format").toString();

        Component playerName = SERIALIZER.deserialize(prefix + player.getUsername())
                .hoverEvent(HoverEvent.showText(luckPermsUtils.getHoverInfo(player, serverName)));

        String beforePlayer = format.substring(0, format.indexOf("%player%"));
        String afterPlayer = format.substring(format.indexOf("%player%") + 8);

        afterPlayer = afterPlayer.replace("%message%", message);

        return Component.text()
                .append(SERIALIZER.deserialize(beforePlayer))
                .append(playerName)
                .append(SERIALIZER.deserialize(afterPlayer))
                .build();
    }

    public Component formatAdminChatMessage(Player player, String message) {
        String prefix = luckPermsUtils.getPrefix(player);
        String serverName = player.getCurrentServer()
                .map(server -> server.getServerInfo().getName())
                .orElse("Unknown");

        String format = configManager.getMessages().get("adminchat-format").toString();

        Component playerName = SERIALIZER.deserialize(prefix + player.getUsername())
                .hoverEvent(HoverEvent.showText(luckPermsUtils.getHoverInfo(player, serverName)));

        String beforePlayer = format.substring(0, format.indexOf("%player%"));
        String afterPlayer = format.substring(format.indexOf("%player%") + 8);

        afterPlayer = afterPlayer.replace("%message%", message);

        return Component.text()
                .append(SERIALIZER.deserialize(beforePlayer))
                .append(playerName)
                .append(SERIALIZER.deserialize(afterPlayer))
                .build();
    }
}
