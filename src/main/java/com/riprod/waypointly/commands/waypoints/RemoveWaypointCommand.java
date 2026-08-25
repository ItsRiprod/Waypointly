package com.riprod.waypointly.commands.waypoints;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;
import com.riprod.waypointly.util.PermissionsUtil;
import com.riprod.waypointly.util.Waypoints;

import javax.annotation.Nonnull;

public class RemoveWaypointCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> nameArg;

    public RemoveWaypointCommand() {
        super("remove", "Remove a waypoint by name");
        this.nameArg = withRequiredArg("name", "The waypoint name to remove", ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String markerName = commandContext.get(this.nameArg);
        if (markerName == null || markerName.trim().isEmpty()) {
            playerRef.sendMessage(Message.raw("You must specify the name of the waypoint to remove. Usage: /waypoint remove <name>"));
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        var marker = Waypoints.findByName(Waypoints.markers(player, world), markerName);
        if (marker == null) {
            playerRef.sendMessage(Message.raw("No waypoint was found with that name."));
            return;
        }

        boolean shared = Waypoints.isShared(marker);
        if (shared && !playerRef.getUuid().equals(marker.getCreatedByUuid()) && !PermissionsUtil.canAdminister(playerRef)) {
            playerRef.sendMessage(Message.raw("That shared waypoint belongs to someone else."));
            return;
        }

        Waypoints.store(player, world, shared).removeUserMapMarker(marker.getId());
        playerRef.sendMessage(Message.raw("Waypoint removed successfully."));
    }
}
