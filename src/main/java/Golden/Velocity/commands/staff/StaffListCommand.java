package Golden.Velocity.commands.staff;

import Golden.Velocity.Main;
import Golden.Velocity.utils.ConfigManager;
import Golden.Velocity.utils.LuckPermsUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.*;

public class StaffListCommand {
    private final Main plugin;
    private final ConfigManager configManager;
    private final LuckPermsUtils luckPermsUtils;

    public StaffListCommand(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.luckPermsUtils = plugin.getLuckPermsUtils();
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
            .<CommandSource>literal("stafflist")
            .requires(source -> source.hasPermission("gbu.staff.list"))
            .executes(context -> {
                showStaffList(context.getSource());
                return 1;
            })
            .build();

        return new BrigadierCommand(node);
    }

    private void showStaffList(CommandSource source) {
        List<Player> staffPlayers = new ArrayList<>();
        
        for (Player player : plugin.getProxy().getAllPlayers()) {
            if (player.hasPermission("gbu.staff.notify")) {
                staffPlayers.add(player);
            }
        }
        
        Object value = configManager.getMessages().get("staff-list");
        if (!(value instanceof List)) {
            source.sendMessage(Component.text("§cError: staff-list format not found in messages.yml"));
            return;
        }
        
        List<?> lines = (List<?>) value;
        for (Object lineObj : lines) {
            String line = lineObj.toString();
            
            line = line.replace("%count%", String.valueOf(staffPlayers.size()));
            
            if (line.contains("%list%")) {
                var listBuilder = Component.text();
                
                for (int i = 0; i < staffPlayers.size(); i++) {
                    Player staff = staffPlayers.get(i);
                    String serverName = staff.getCurrentServer()
                        .map(server -> server.getServerInfo().getName())
                        .orElse("Unknown");
                    
                    Component displayName = luckPermsUtils.getDisplayName(staff);
                    Component hoverInfo = luckPermsUtils.getHoverInfo(staff, serverName);
                    
                    listBuilder.append(displayName.hoverEvent(HoverEvent.showText(hoverInfo)));
                    
                    if (i < staffPlayers.size() - 1) {
                        listBuilder.append(Component.text(", "));
                    }
                }
                
                source.sendMessage(listBuilder.build());
            } else {
                source.sendMessage(Component.text(line.replace("&", "§")));
            }
        }
    }
}
