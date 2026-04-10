package Golden.Velocity.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.HashMap;
import java.util.Map;

public class PlaceholderUtils {
    private final ProxyServer proxy;

    public PlaceholderUtils(ProxyServer proxy) {
        this.proxy = proxy;
    }

    public String replacePlaceholders(String text, Player player) {
        Map<String, String> placeholders = new HashMap<>();

        if (player != null) {
            placeholders.put("player", player.getUsername());
            placeholders.put("uuid", player.getUniqueId().toString());

            player.getCurrentServer().ifPresent(server -> placeholders.put("server", server.getServerInfo().getName()));
        }

        placeholders.put("online", String.valueOf(proxy.getPlayerCount()));

        return replacePlaceholders(text, placeholders);
    }

    public String replacePlaceholders(String text, Map<String, String> placeholders) {
        String result = text;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        result = result.replace("%online%", String.valueOf(proxy.getPlayerCount()));

        return result;
    }
}
