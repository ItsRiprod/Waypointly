package com.riprod.waypointly.commands.waypoints;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;
import com.riprod.waypointly.config.WaypointlyConfig;
import com.riprod.waypointly.util.Waypoints;

import javax.annotation.Nonnull;

public class AddWaypointCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> nameArg;

    public AddWaypointCommand() {
        super("add", "Add a waypoint at your current position");
        this.nameArg = withRequiredArg("name", "The waypoint name", ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {

        String markerName = commandContext.get(this.nameArg);
        if (markerName == null || markerName.trim().isEmpty()) {
            playerRef.sendMessage(Message.raw("You must specify a name for the waypoint. Usage: /waypoint add <name>"));
            return;
        }

        var config = WaypointlyConfig.get();
        if (markerName.length() > config.getMaxNameLength()) {
            playerRef.sendMessage(Message.raw("Waypoint names are limited to " + config.getMaxNameLength() + " characters."));
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (Waypoints.isAtLimit(player, world, playerRef.getUuid(), false)) {
            playerRef.sendMessage(Message.raw("You have reached the maximum number of waypoints (" + config.getMaxWaypoints() + ")."));
            return;
        }

        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        var position = transformComponent.getSentTransform().position;

        var marker = Waypoints.create(playerRef, markerName, (float) position.x, (float) position.z,
                config.getDefaultIcon(), null, false);
        Waypoints.store(player, world, false).addUserMapMarker(marker);

        playerRef.sendMessage(Message.raw("Waypoint created: " + markerName));
    }
}
