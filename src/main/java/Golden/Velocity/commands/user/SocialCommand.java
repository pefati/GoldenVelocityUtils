package Golden.Velocity.commands.user;

import Golden.Velocity.Main;
import Golden.Velocity.utils.ConfigManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;

public class SocialCommand {
    private final Main plugin;
    private final ConfigManager configManager;
    private final String commandName;
    private final String messageKey;

    public SocialCommand(Main plugin, String commandName, String messageKey) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.commandName = commandName;
        this.messageKey = messageKey;
    }

    public BrigadierCommand createCommand() {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder
            .<CommandSource>literal(commandName)
            .requires(source -> source.hasPermission("gbu.command." + commandName))
            .executes(context -> {
                context.getSource().sendMessage(configManager.getMessage(messageKey));
                return 1;
            })
            .build();

        return new BrigadierCommand(node);
    }
}
