package com.riprod.waypointly.pages;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.riprod.waypointly.util.Waypoints;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public class EditWaypointPage extends WaypointFormPage {

    private final String markerId;

    public EditWaypointPage(@Nonnull PlayerRef playerRef, @Nonnull UserMapMarker waypoint) {
        super(playerRef);
        this.markerId = waypoint.getId();
        this.selectedIcon = waypoint.getIcon();
        this.name = Waypoints.displayName(waypoint);
        this.x = String.format("%.2f", waypoint.getX());
        this.z = String.format("%.2f", waypoint.getZ());
        this.tint = Waypoints.NO_TINT.equals(waypoint.getColorTint()) ? "" : Waypoints.formatTint(waypoint.getColorTint());
        this.shared = Waypoints.isShared(waypoint);
    }

    @Override
    protected String documentPath() {
        return "Pages/EditWaypointPage.ui";
    }

    @Override
    protected boolean canChangeVisibility() {
        return false;
    }

    @Override
    protected void submit(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                          @Nonnull Player player, @Nonnull World world, float x, float z) {
        var markerStore = Waypoints.store(player, world, shared);
        var stored = markerStore.getUserMapMarker(markerId);
        if (stored == null) {
            playerRef.sendMessage(Message.raw("That waypoint no longer exists."));
            openList(ref, store, player, world);
            return;
        }

        var markers = new ArrayList<UserMapMarker>(markerStore.getUserMapMarkers());
        stored.setPosition(x, z);
        stored.setName(name);
        stored.setIcon(selectedIcon);

        var parsedTint = Waypoints.parseTint(tint);
        stored.setColorTint(parsedTint == null ? Waypoints.NO_TINT : parsedTint);

        markerStore.setUserMapMarkers(markers);

        playerRef.sendMessage(Message.raw("Waypoint updated: " + name));
        openList(ref, store, player, world);
    }
}
