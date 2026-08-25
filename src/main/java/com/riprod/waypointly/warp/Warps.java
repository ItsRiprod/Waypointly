package com.riprod.waypointly.warp;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.riprod.waypointly.config.WaypointlyConfig;
import com.riprod.waypointly.util.PermissionsUtil;
import com.riprod.waypointly.util.Waypoints;

import javax.annotation.Nonnull;

public final class Warps {

    private Warps() {
    }

    public static void request(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                               @Nonnull PlayerRef playerRef, @Nonnull World world, @Nonnull UserMapMarker marker) {
        if (!PermissionsUtil.canTeleport(playerRef)) {
            playerRef.sendMessage(Message.raw("You do not have permission to warp to waypoints."));
            return;
        }

        final var existing = store.getComponent(ref, WarpComponent.getComponentType());
        if (existing != null && existing.isOnCooldown()) {
            playerRef.sendMessage(Message.raw("You must wait " + format(existing.getCooldownRemaining()) + " before warping again."));
            return;
        }
        if (existing != null && existing.isWarmingUp()) {
            playerRef.sendMessage(Message.raw("You are already warping."));
            return;
        }

        final var config = WaypointlyConfig.get();
        final float cooldown = config.getTeleportCooldownSeconds();
        final float warmup = config.getTeleportWarmupSeconds();

        if (warmup <= 0) {
            Waypoints.teleport(ref, world, marker);
            playerRef.sendMessage(Message.raw("Warped to '" + Waypoints.displayName(marker) + "'!"));
        } else {
            playerRef.sendMessage(Message.raw("Warping to '" + Waypoints.displayName(marker) + "' in " + format(warmup) + "..."));
        }

        if (cooldown <= 0 && warmup <= 0) {
            return;
        }

        world.execute(() -> {
            if (!ref.isValid()) return;

            var warp = store.getComponent(ref, WarpComponent.getComponentType());
            if (warp == null) {
                warp = new WarpComponent();
                store.addComponent(ref, WarpComponent.getComponentType(), warp);
            }

            warp.startCooldown(cooldown);
            if (warmup > 0) {
                warp.startWarmup(warmup, marker.getX(), marker.getZ(), Waypoints.displayName(marker));
            }
        });
    }

    private static String format(float seconds) {
        return String.format("%.1fs", seconds);
    }
}
