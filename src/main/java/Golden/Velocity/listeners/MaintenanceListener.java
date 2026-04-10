package Golden.Velocity.listeners;

import Golden.Velocity.Main;
import Golden.Velocity.utils.ConfigManager;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

import java.util.List;

public class MaintenanceListener {
    private final Main plugin;
    private final ConfigManager configManager;

    public MaintenanceListener(Main plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();

        boolean maintenanceEnabled = configManager.getBoolean("maintenance.enabled", false);

        if (!maintenanceEnabled) {
            return;
        }

        if (player.hasPermission("gbu.maintenance.bypass")) {
            return;
        }

        List<String> whitelist = configManager.getStringList("maintenance.whitelist");
        if (whitelist.contains(player.getUsername())) {
            return;
        }

        Component kickMessage = configManager.getMessage("maintenance-kick");
        event.setResult(ResultedEvent.ComponentResult.denied(kickMessage));
    }
}
