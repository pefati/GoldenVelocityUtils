package Golden.Velocity.utils;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;

public class LuckPermsUtils {
    private LuckPerms luckPerms;

    public LuckPermsUtils() {
        try {
            this.luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            this.luckPerms = null;
        }
    }

    public boolean isAvailable() {
        return luckPerms != null;
    }

    public String getPrefix(Player player) {
        if (!isAvailable()) {
            return "";
        }

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return "";
        }

        CachedMetaData metaData = user.getCachedData().getMetaData();
        String prefix = metaData.getPrefix();
        
        return prefix != null ? prefix : "";
    }

    public String getSuffix(Player player) {
        if (!isAvailable()) {
            return "";
        }

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return "";
        }

        CachedMetaData metaData = user.getCachedData().getMetaData();
        String suffix = metaData.getSuffix();
        
        return suffix != null ? suffix : "";
    }

    public String getPrimaryGroup(Player player) {
        if (!isAvailable()) {
            return "default";
        }

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return "default";
        }

        return user.getPrimaryGroup();
    }

    public Component getDisplayName(Player player) {
        String prefix = getPrefix(player);
        String suffix = getSuffix(player);
        
        return Component.text()
            .append(Component.text(prefix))
            .append(Component.text(player.getUsername()))
            .append(Component.text(suffix))
            .build();
    }

    public Component getHoverInfo(Player player, String currentServer) {
        String group = getPrimaryGroup(player);
        
        return Component.text()
            .append(Component.text("Rank: ", NamedTextColor.GRAY))
            .append(Component.text(group, NamedTextColor.YELLOW))
            .append(Component.newline())
            .append(Component.text("Server: ", NamedTextColor.GRAY))
            .append(Component.text(currentServer, NamedTextColor.GREEN))
            .build();
    }
}
