package com.riprod.waypointly.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarkersStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore.WorldMarkersResource;
import com.riprod.waypointly.config.WaypointlyConfig;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Waypoints {

    public static final Color NO_TINT = new Color((byte) 0, (byte) 0, (byte) 0);

    private static final String PERSONAL_PREFIX = "user_personal_";
    private static final String SHARED_PREFIX = "user_shared_";

    private Waypoints() {
    }

    @Nonnull
    public static UserMapMarkersStore store(@Nonnull Player player, @Nonnull World world, boolean shared) {
        return shared
            ? world.getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType())
            : player.getPlayerConfigData().getPerWorldData(world.getName());
    }

    public static boolean isShared(@Nonnull UserMapMarker marker) {
        return marker.getId().startsWith(SHARED_PREFIX);
    }

    @Nonnull
    public static List<UserMapMarker> markers(@Nonnull Player player, @Nonnull World world) {
        List<UserMapMarker> markers = new ArrayList<>(store(player, world, false).getUserMapMarkers());
        markers.addAll(store(player, world, true).getUserMapMarkers());
        return markers;
    }

    @Nonnull
    public static List<UserMapMarker> ownedBy(@Nonnull Player player, @Nonnull World world, @Nonnull UUID uuid, boolean shared) {
        return new ArrayList<>(store(player, world, shared).getUserMapMarkers(uuid));
    }

    @Nullable
    public static UserMapMarker find(@Nonnull Player player, @Nonnull World world, @Nonnull String markerId) {
        var personal = store(player, world, false).getUserMapMarker(markerId);
        return personal != null ? personal : store(player, world, true).getUserMapMarker(markerId);
    }

    @Nonnull
    public static UserMapMarker create(@Nonnull PlayerRef playerRef, @Nonnull String name, float x, float z,
                                       @Nonnull String icon, @Nullable Color tint, boolean shared) {
        var marker = new UserMapMarker();
        marker.setId((shared ? SHARED_PREFIX : PERSONAL_PREFIX) + UUID.randomUUID());
        marker.setPosition(x, z);
        marker.setName(name);
        marker.setIcon(icon);
        marker.setColorTint(tint == null ? NO_TINT : tint);
        marker.withCreatedByUuid(playerRef.getUuid());
        marker.withCreatedByName(playerRef.getUsername());
        return marker;
    }

    @Nonnull
    public static String displayName(@Nonnull UserMapMarker marker) {
        var name = marker.getName();
        return name == null || name.isBlank()
            ? String.format("%.0f, %.0f", marker.getX(), marker.getZ())
            : name;
    }

    @Nullable
    public static UserMapMarker findByName(@Nonnull List<UserMapMarker> markers, @Nonnull String name) {
        for (var marker : markers) {
            if (displayName(marker).equalsIgnoreCase(name)) {
                return marker;
            }
        }
        return null;
    }

    @Nullable
    public static Color parseTint(@Nullable String hex) {
        if (hex == null) return null;

        var trimmed = hex.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.length() != 6) return null;

        try {
            int rgb = Integer.parseInt(trimmed, 16);
            return new Color((byte) (rgb >> 16), (byte) (rgb >> 8), (byte) rgb);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nonnull
    public static String formatTint(@Nullable Color color) {
        if (color == null) return "";
        return String.format("#%02X%02X%02X", color.red & 0xFF, color.green & 0xFF, color.blue & 0xFF);
    }

    public static void teleport(@Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull UserMapMarker marker) {
        teleport(ref, world, marker.getX(), marker.getZ());
    }

    public static void teleport(@Nonnull Ref<EntityStore> ref, @Nonnull World world, float x, float z) {
        final int blockX = MathUtil.floor(x);
        final int blockZ = MathUtil.floor(z);

        world.getChunkStore().getChunkReferenceAsync(ChunkUtil.indexChunkFromBlock(blockX, blockZ))
            .thenAcceptAsync(chunkRef -> {
                if (!ref.isValid()) return;

                final var entityStore = ref.getStore();
                final var blockChunk = chunkRef.getStore().getComponent(chunkRef, BlockChunk.getComponentType());
                final var position = new Vector3d(x, blockChunk.getHeight(blockX, blockZ) + 2, z);
                final var headRotation = entityStore.getComponent(ref, HeadRotation.getComponentType());
                final var rotation = headRotation != null ? headRotation.getRotation() : Rotation3f.ZERO;

                entityStore.addComponent(ref, Teleport.getComponentType(), Teleport.createForPlayer(null, position, rotation));
            }, world);
    }

    public static boolean isAtLimit(@Nonnull Player player, @Nonnull World world, @Nonnull UUID uuid, boolean shared) {
        var config = WaypointlyConfig.get();
        int limit = shared ? config.getMaxSharedWaypoints() : config.getMaxWaypoints();
        return limit != WaypointlyConfig.UNLIMITED_WAYPOINTS && ownedBy(player, world, uuid, shared).size() >= limit;
    }
}
