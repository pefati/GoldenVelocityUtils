package Golden.Velocity.tasks;

import Golden.Velocity.Main;
import Golden.Velocity.utils.ConfigManager;
import Golden.Velocity.utils.PlaceholderUtils;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class AnnouncementTask implements Runnable {
    private final Main plugin;
    private final ConfigManager configManager;
    private final PlaceholderUtils placeholderUtils;
    private final Map<String, Long> lastAnnouncement = new HashMap<>();
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    public AnnouncementTask(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.placeholderUtils = plugin.getPlaceholderUtils();
    }

    @Override
    public void run() {
        Map<String, Object> anuncios = configManager.getAnuncios();
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<String, Object> entry : anuncios.entrySet()) {
            String announcementKey = entry.getKey();

            if (!(entry.getValue() instanceof Map)) {
                continue;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> announcementData = (Map<String, Object>) entry.getValue();

            int intervalMinutes = announcementData.get("interval") instanceof Number
                    ? ((Number) announcementData.get("interval")).intValue()
                    : 5;

            long intervalMillis = TimeUnit.MINUTES.toMillis(intervalMinutes);

            long lastTime = lastAnnouncement.getOrDefault(announcementKey, 0L);
            if (currentTime - lastTime < intervalMillis) {
                continue;
            }

            lastAnnouncement.put(announcementKey, currentTime);

            @SuppressWarnings("unchecked")
            List<String> lines = announcementData.get("lines") instanceof List
                    ? (List<String>) announcementData.get("lines")
                    : new ArrayList<>();

            if (lines.isEmpty()) {
                continue;
            }

            String linkUrl = null;
            String linkText = null;

            if (announcementData.get("link") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> linkData = (Map<String, Object>) announcementData.get("link");
                linkUrl = linkData.get("url") != null ? linkData.get("url").toString() : null;
                linkText = linkData.get("text") != null ? linkData.get("text").toString() : null;
            }

            var messageBuilder = Component.text();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                line = placeholderUtils.replacePlaceholders(line, new HashMap<>());

                if (line.contains("<Link>") && linkUrl != null && linkText != null) {
                    String beforeLink = line.substring(0, line.indexOf("<Link>"));
                    String afterLink = line.substring(line.indexOf("<Link>") + 6);

                    Component linkComponent = SERIALIZER.deserialize(linkText)
                            .clickEvent(ClickEvent.openUrl(linkUrl))
                            .hoverEvent(HoverEvent.showText(
                                    configManager.getMessage("announcement-open")));

                    messageBuilder
                            .append(SERIALIZER.deserialize(beforeLink))
                            .append(linkComponent)
                            .append(SERIALIZER.deserialize(afterLink));
                } else {
                    messageBuilder.append(SERIALIZER.deserialize(line));
                }

                if (i < lines.size() - 1) {
                    messageBuilder.append(Component.newline());
                }
            }

            Component announcement = messageBuilder.build();

            for (Player player : plugin.getProxy().getAllPlayers()) {
                player.sendMessage(announcement);
            }
        }
    }

    public void schedule() {
        plugin.getProxy().getScheduler()
                .buildTask(plugin, this)
                .delay(30, TimeUnit.SECONDS)
                .repeat(30, TimeUnit.SECONDS)
                .schedule();
    }
}
