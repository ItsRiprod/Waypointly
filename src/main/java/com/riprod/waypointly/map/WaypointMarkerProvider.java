package com.riprod.waypointly.map;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.packets.worldmap.ContextMenuItem;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.PlacedByMarkerComponent;
import com.hypixel.hytale.protocol.packets.worldmap.TintComponent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.riprod.waypointly.util.PermissionsUtil;
import com.riprod.waypointly.util.Waypoints;

import javax.annotation.Nonnull;

public class WaypointMarkerProvider implements WorldMapManager.MarkerProvider {

    public static final String PERSONAL_KEY = "personal";
    public static final String SHARED_KEY = "shared";

    public static final WaypointMarkerProvider PERSONAL = new WaypointMarkerProvider(false);
    public static final WaypointMarkerProvider SHARED = new WaypointMarkerProvider(true);

    private final boolean shared;

    private WaypointMarkerProvider(boolean shared) {
        this.shared = shared;
    }

    @Override
    public void update(@Nonnull World world, @Nonnull Player player, @Nonnull MarkersCollector collector) {
        final boolean canWarp = PermissionsUtil.canTeleport(player.getPlayerRef());

        for (var marker : Waypoints.store(player, world, shared).getUserMapMarkers()) {
            collector.addIgnoreViewDistance(build(marker, canWarp));
        }
    }

    @Nonnull
    private MapMarker build(@Nonnull UserMapMarker marker, boolean canWarp) {
        var builder = new MapMarkerBuilder(marker.getId(), marker.getIcon(), new Transform(marker.getX(), 100, marker.getZ()))
            .withCustomName(Waypoints.displayName(marker));

        if (marker.getColorTint() != null) {
            builder.withComponent(new TintComponent(marker.getColorTint()));
        }

        if (marker.getCreatedByName() != null) {
            builder.withComponent(new PlacedByMarkerComponent(
                Message.raw(marker.getCreatedByName()).getFormattedMessage(), marker.getCreatedByUuid()));
        }

        if (canWarp) {
            builder.withContextMenuItem(new ContextMenuItem("Warp", "warp " + marker.getId()));
        }

        return builder.build();
    }
}
