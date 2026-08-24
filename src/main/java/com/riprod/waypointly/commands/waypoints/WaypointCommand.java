package com.riprod.waypointly.commands.waypoints;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.util.Config;
import com.riprod.waypointly.config.WaypointsConfig;
import com.riprod.waypointly.pages.WaypointPage;
import com.riprod.waypointly.util.PermissionsUtil;
import com.riprod.waypointly.util.Waypoints;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;

import javax.annotation.Nonnull;

public class WaypointCommand extends AbstractPlayerCommand {

    private final Config<WaypointsConfig> config;

    public WaypointCommand(Config<WaypointsConfig> config) {
        super("wpm", "Parent command for marker operations");
        setPermissionGroups(HytalePermissionsProvider.GROUP_ADVENTURER);
        this.config = config;
        addSubCommand(new AddWaypointCommand());
        addSubCommand(new RemoveWaypointCommand());
        addAliases("waypoint","wp", "waypoints");
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());

        if (!PermissionsUtil.canOpenWaypointUI(playerRef)) {
            playerRef.sendMessage(Message.raw("You do not have permission to use this command."));
            return;
        }

        WaypointPage page = new WaypointPage(playerRef, Waypoints.markers(player, world.getName()), config);
        player.getPageManager().openCustomPage(ref, store, page);
    }
}
