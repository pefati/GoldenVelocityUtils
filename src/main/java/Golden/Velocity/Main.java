package Golden.Velocity;

import Golden.Velocity.commands.admin.GbuCommand;
import Golden.Velocity.commands.admin.StaffTimeCommand;
import Golden.Velocity.commands.staff.*;
import Golden.Velocity.commands.user.*;
import Golden.Velocity.listeners.*;
import Golden.Velocity.tasks.AnnouncementTask;
import Golden.Velocity.utils.*;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import java.nio.file.Path;
import java.util.Map;

@Plugin(id = "goldenvelocityutils", name = "GoldenVelocityUtils", version = "1.0.0", authors = {
        "pefati" }, dependencies = {
                @Dependency(id = "luckperms")
        })
public class Main {

    private static Main instance;
    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private ConfigManager configManager;
    private ChatManager chatManager;
    private LuckPermsUtils luckPermsUtils;
    private PlaceholderUtils placeholderUtils;
    private SessionManager sessionManager;

    private AnnouncementTask announcementTask;

    @Inject
    public Main(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        instance = this;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initializing GoldenVelocityUtils...");

        configManager = new ConfigManager(dataDirectory, proxy, proxy.getPluginManager().getPlugin("goldenvelocityutils").orElse(null));
        luckPermsUtils = new LuckPermsUtils();
        chatManager = new ChatManager(configManager, luckPermsUtils);
        placeholderUtils = new PlaceholderUtils(proxy);
        sessionManager = new SessionManager();

        registerCommands();
        registerListeners();
        scheduleTasks();

        logger.info("GoldenVelocityUtils has been enabled!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (sessionManager != null) {
            sessionManager.clearAll();
        }
        logger.info("GoldenVelocityUtils has been disabled!");
    }

    private void registerCommands() {
        proxy.getCommandManager().register(new GbuCommand(this).createCommand());
        proxy.getCommandManager().register(new StaffTimeCommand(this).createCommand());

        AdminChatCommand adminChatCommand = new AdminChatCommand(this);
        proxy.getCommandManager().register(adminChatCommand.createCommand());
        proxy.getCommandManager().register(adminChatCommand.createAliasCommand());

        proxy.getCommandManager().register(new AlertCommand(this).createCommand());
        proxy.getCommandManager().register(new FindCommand(this).createCommand());
        proxy.getCommandManager().register(new GotoCommand(this).createCommand());
        proxy.getCommandManager().register(new MaintenanceCommand(this).createCommand());
        proxy.getCommandManager().register(new NetworkListCommand(this).createCommand());
        proxy.getCommandManager().register(new SrvCommand(this).createCommand());

        StaffChatCommand staffChatCommand = new StaffChatCommand(this);
        proxy.getCommandManager().register(staffChatCommand.createCommand());
        proxy.getCommandManager().register(staffChatCommand.createAliasCommand());

        proxy.getCommandManager().register(new StaffListCommand(this).createCommand());

        proxy.getCommandManager().register(new HelpopCommand(this).createCommand());
        proxy.getCommandManager().register(new ReportCommand(this).createCommand());
        proxy.getCommandManager().register(new StreamCommand(this).createCommand());

        proxy.getCommandManager().register(new SocialCommand(this, "discord", "discord").createCommand());
        proxy.getCommandManager().register(new SocialCommand(this, "twitter", "twitter").createCommand());

        registerMoveCommands();

        logger.info("Registered all commands successfully!");
    }

    private void registerMoveCommands() {
        Map<String, Object> config = configManager.getConfig();
        Object serversObj = config.get("servers");
        if (serversObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> servers = (Map<String, Object>) serversObj;
            for (Map.Entry<String, Object> entry : servers.entrySet()) {
                String key = entry.getKey();
                String serverName = entry.getValue().toString();
                proxy.getCommandManager().register(new MoveCommand(this, key, serverName).createCommand());
            }
        }
    }

    private void registerListeners() {
        proxy.getChannelRegistrar().register(MinecraftChannelIdentifier.from("gbu:chat"));
        proxy.getChannelRegistrar().register(MinecraftChannelIdentifier.from("gbu:toggle"));

        proxy.getEventManager().register(this, new ChatPluginMessageListener(this));
        proxy.getEventManager().register(this, new MaintenanceListener(this));
        proxy.getEventManager().register(this, new MotdListener(this));
        proxy.getEventManager().register(this, new StaffNotificationListener(this));

        logger.info("Registered all listeners successfully!");
    }

    private void scheduleTasks() {
        announcementTask = new AnnouncementTask(this);
        announcementTask.schedule();

        logger.info("Scheduled all tasks successfully!");
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public LuckPermsUtils getLuckPermsUtils() {
        return luckPermsUtils;
    }

    public PlaceholderUtils getPlaceholderUtils() {
        return placeholderUtils;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
