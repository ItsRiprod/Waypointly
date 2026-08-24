package com.riprod.waypointly.util;

import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.riprod.waypointly.Constants;

import javax.annotation.Nonnull;

public final class PermissionsUtil {

    private PermissionsUtil() {
    }

    public static boolean hasNegatedPermission(@Nonnull final PlayerRef playerRef, @Nonnull final String permission) {
        return PermissionsModule.get().hasPermission(playerRef.getUuid(), "-" + permission);
    }

    public static boolean canOpenWaypointUI(@Nonnull final PlayerRef playerRef) {
        return isAdmin(playerRef) || !hasNegatedPermission(playerRef, Constants.PERMISSION_WAYPOINT_OPEN);
    }

    public static boolean canTeleport(@Nonnull final PlayerRef playerRef) {
        return isAdmin(playerRef)
            || PermissionsModule.get().hasPermission(playerRef.getUuid(), Constants.PERMISSION_WAYPOINT_TELEPORT);
    }

    public static boolean isAdmin(@Nonnull final PlayerRef playerRef) {
        final var groups = PermissionsModule.get().getGroupsForUser(playerRef.getUuid());
        return groups != null && groups.contains("OP");
    }
}
