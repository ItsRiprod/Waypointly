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

public class ResetWaypointsCommand extends AbstractPlayerCommand {

    public ResetWaypointsCommand() {
        super("resetmarkers", "Reset all map markers");
        setPermissionGroups(HytalePermissionsProvider.GROUP_ADVENTURER);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());
        Waypoints.store(player, world, false).setUserMapMarkers(null);

        playerRef.sendMessage(Message.raw("All of your personal waypoints in this world were removed."));
    }
}
