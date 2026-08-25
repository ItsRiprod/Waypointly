package com.riprod.waypointly.commands.waypoints;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;
import com.riprod.waypointly.util.Waypoints;

import javax.annotation.Nonnull;

public class ListWaypointsCommand extends AbstractPlayerCommand {

    public ListWaypointsCommand() {
        super("listmarkers", "List all your map markers");
        setPermissionGroups(HytalePermissionsProvider.GROUP_ADVENTURER);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());
        var markers = Waypoints.markers(player, world);

        if (markers.isEmpty()) {
            playerRef.sendMessage(Message.raw("You have no map markers."));
            return;
        }

        playerRef.sendMessage(Message.raw("Your map markers:"));
        for (var marker : markers) {
            String info = String.format("- %s%s at [%.0f, %.0f]",
                    Waypoints.displayName(marker),
                    Waypoints.isShared(marker) ? " (shared)" : "",
                    marker.getX(),
                    marker.getZ()
            );
            playerRef.sendMessage(Message.raw(info));
        }
    }
}
