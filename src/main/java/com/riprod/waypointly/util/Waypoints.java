package com.riprod.waypointly.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Waypoints {

    private Waypoints() {
    }

    @Nonnull
    public static List<UserMapMarker> markers(@Nonnull Player player, @Nonnull String worldName) {
        return new ArrayList<>(player.getPlayerConfigData().getPerWorldData(worldName).getUserMapMarkers());
    }

    @Nonnull
    public static UserMapMarker create(@Nonnull PlayerRef playerRef, @Nonnull String name, float x, float z, @Nonnull String icon) {
        var marker = new UserMapMarker();
        marker.setId("user_personal_" + UUID.randomUUID());
        marker.setPosition(x, z);
        marker.setName(name);
        marker.setIcon(icon);
        marker.withCreatedByUuid(playerRef.getUuid());
        marker.withCreatedByName(playerRef.getUsername());
        return marker;
    }

    @Nullable
    public static UserMapMarker findByName(@Nonnull List<UserMapMarker> markers, @Nonnull String name) {
        for (var marker : markers) {
            var markerName = marker.getName();
            if (markerName != null && markerName.equalsIgnoreCase(name)) {
                return marker;
            }
        }
        return null;
    }

    // user markers store only x/z, so the vertical target is resolved from the column height like the
    // engine does for its own map-marker teleports
    public static void teleport(@Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull UserMapMarker marker) {
        final int blockX = MathUtil.floor(marker.getX());
        final int blockZ = MathUtil.floor(marker.getZ());

        world.getChunkStore().getChunkReferenceAsync(ChunkUtil.indexChunkFromBlock(blockX, blockZ))
            .thenAcceptAsync(chunkRef -> {
                if (!ref.isValid()) return;

                final var entityStore = ref.getStore();
                final var blockChunk = chunkRef.getStore().getComponent(chunkRef, BlockChunk.getComponentType());
                final var position = new Vector3d(marker.getX(), blockChunk.getHeight(blockX, blockZ) + 2, marker.getZ());
                final var headRotation = entityStore.getComponent(ref, HeadRotation.getComponentType());
                final var rotation = headRotation != null ? headRotation.getRotation() : Rotation3f.ZERO;

                entityStore.addComponent(ref, Teleport.getComponentType(), Teleport.createForPlayer(null, position, rotation));
            }, world);
    }
}
