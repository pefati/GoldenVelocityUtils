package Golden.Velocity.commands.staff;

import Golden.Velocity.Main;
import Golden.Velocity.utils.ConfigManager;
import Golden.Velocity.utils.LuckPermsUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.stream.Collectors;

public class NetworkListCommand {
    private final Main plugin;
    private final ConfigManager configManager;
    private final LuckPermsUtils luckPermsUtils;

    public NetworkListCommand(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.luckPermsUtils = plugin.getLuckPermsUtils();
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
            .<CommandSource>literal("blist")
            .requires(source -> source.hasPermission("gbu.user.list"))
            .executes(context -> {
                showNetworkList(context.getSource());
                return 1;
            })
            .build();

        return new BrigadierCommand(node);
    }

    private void showNetworkList(CommandSource source) {
        int totalPlayers = plugin.getProxy().getPlayerCount();
        Collection<RegisteredServer> servers = plugin.getProxy().getAllServers();
        
        Object value = configManager.getMessages().get("network-list");
        if (value instanceof List) {
            List<?> lines = (List<?>) value;
            for (Object lineObj : lines) {
                String line = lineObj.toString();
                
                line = line.replace("%count%", String.valueOf(totalPlayers));
                
                if (line.contains("%servers%")) {
                    for (RegisteredServer server : servers) {
                        int playerCount = server.getPlayersConnected().size();
                        String serverName = server.getServerInfo().getName();
                        
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("server", serverName);
                        placeholders.put("count", String.valueOf(playerCount));
                        
                        source.sendMessage(configManager.getMessage("network-list-format", placeholders));
                    }
                } else {
                    source.sendMessage(Component.text(line.replace("&", "§")));
                }
            }
        }
    }
}
