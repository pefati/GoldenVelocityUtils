package Golden.Velocity.listeners;

import Golden.Velocity.Main;
import Golden.Velocity.utils.ConfigManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.Random;

public class MotdListener {
    private final Main plugin;
    private final ConfigManager configManager;
    private final Random random = new Random();
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    public MotdListener(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        boolean motdEnabled = configManager.getBoolean("motd.enabled", true);

        if (!motdEnabled) {
            return;
        }

        List<String> motdLines = configManager.getStringList("motd.lines");

        if (motdLines.isEmpty()) {
            return;
        }

        String randomMotd = motdLines.get(random.nextInt(motdLines.size()));

        Component motdComponent = SERIALIZER.deserialize(randomMotd);

        ServerPing originalPing = event.getPing();
        ServerPing.Builder builder = originalPing.asBuilder();
        builder.description(motdComponent);

        event.setPing(builder.build());
    }
}
