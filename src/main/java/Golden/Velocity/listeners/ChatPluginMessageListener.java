package Golden.Velocity.listeners;

import Golden.Velocity.Main;
import Golden.Velocity.utils.ChatManager;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.Component;

public class ChatPluginMessageListener {
    private final Main plugin;
    private final ChatManager chatManager;

    public ChatPluginMessageListener(Main plugin) {
        this.plugin = plugin;
        this.chatManager = plugin.getChatManager();
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().getId().equals("gbu:chat")) {
            return;
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if (event.getSource() instanceof ServerConnection) {
            ServerConnection backend = (ServerConnection) event.getSource();
            Player player = backend.getPlayer();

            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            String subChannel = in.readUTF();

            if (subChannel.equals("StaffChat")) {
                String message = in.readUTF();
                Component staffMessage = chatManager.formatStaffChatMessage(player, message);

                plugin.getProxy().getAllPlayers().stream()
                        .filter(p -> p.hasPermission("gbu.staff.chat"))
                        .forEach(staff -> staff.sendMessage(staffMessage));
            } else if (subChannel.equals("AdminChat")) {
                String message = in.readUTF();
                Component adminMessage = chatManager.formatAdminChatMessage(player, message);

                plugin.getProxy().getAllPlayers().stream()
                        .filter(p -> p.hasPermission("gbu.admin.chat"))
                        .forEach(admin -> admin.sendMessage(adminMessage));
            }
        }
    }
}
