package com.riprod.waypointly.pages;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.waypointly.config.WaypointlyConfig;
import com.riprod.waypointly.util.Waypoints;

import javax.annotation.Nonnull;

public class AddWaypointPage extends WaypointFormPage {

    public AddWaypointPage(@Nonnull PlayerRef playerRef) {
        super(playerRef);
        this.selectedIcon = WaypointlyConfig.get().getDefaultIcon();
    }

    @Override
    protected String documentPath() {
        return "Pages/AddWaypointPage.ui";
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        if (x == null || z == null) {
            var position = store.getComponent(ref, TransformComponent.getComponentType()).getSentTransform().position;
            x = String.format("%.2f", position.x);
            z = String.format("%.2f", position.z);
        }
        super.build(ref, ui, events, store);
    }

    @Override
    protected void submit(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                          @Nonnull Player player, @Nonnull World world, float x, float z) {
        var config = WaypointlyConfig.get();
        if (Waypoints.isAtLimit(player, world, playerRef.getUuid(), shared)) {
            int limit = shared ? config.getMaxSharedWaypoints() : config.getMaxWaypoints();
            playerRef.sendMessage(Message.raw("You have reached the maximum number of waypoints (" + limit + ")."));
            return;
        }

        var marker = Waypoints.create(playerRef, name, x, z, selectedIcon, Waypoints.parseTint(tint), shared);
        Waypoints.store(player, world, shared).addUserMapMarker(marker);

        playerRef.sendMessage(Message.raw("Waypoint created: " + name));
        openList(ref, store, player, world);
    }
}
