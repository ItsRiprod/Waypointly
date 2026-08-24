package com.riprod.waypointly.commands.waypoints;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.waypointly.util.PermissionsUtil;
import com.riprod.waypointly.util.Waypoints;

import javax.annotation.Nonnull;

public class WaypointTeleportCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> nameArg;

    public WaypointTeleportCommand() {
        super("teleport", "Teleport to a waypoint");
        this.nameArg = withRequiredArg("name", "The waypoint name", ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String name = commandContext.get(this.nameArg);
        if (name == null || name.trim().isEmpty()) {
            playerRef.sendMessage(Message.raw("You must specify a waypoint name!"));
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());

        if (!PermissionsUtil.canTeleport(playerRef)) {
            playerRef.sendMessage(Message.raw("You do not have permission to teleport to waypoints."));
            return;
        }

        var markers = Waypoints.markers(player, world.getName());
        if (markers.isEmpty()) {
            playerRef.sendMessage(Message.raw("You don't have any waypoints in this world."));
            return;
        }

        var waypoint = Waypoints.findByName(markers, name);
        if (waypoint == null) {
            playerRef.sendMessage(Message.raw("Waypoint '" + name + "' not found!"));
            return;
        }

        Waypoints.teleport(ref, world, waypoint);
        playerRef.sendMessage(Message.raw("Teleported to '" + waypoint.getName() + "'!"));
    }
}
