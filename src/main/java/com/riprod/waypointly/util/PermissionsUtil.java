package com.riprod.waypointly.util;

import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.riprod.waypointly.Constants;
import com.riprod.waypointly.config.WaypointlyConfig;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;

public final class PermissionsUtil {

    private PermissionsUtil() {
    }

    public static void registerPermissions() {
        PermissionsModule.registerPermission(Constants.PERMISSION_WAYPOINT_OPEN);
        PermissionsModule.registerPermission(Constants.PERMISSION_WAYPOINT_TELEPORT,
                HytalePermissionsProvider.GROUP_WORLD_EDITOR);
        PermissionsModule.registerPermission(Constants.PERMISSION_WAYPOINT_SHARED,
                HytalePermissionsProvider.GROUP_BUILDER);
        PermissionsModule.registerPermission(Constants.PERMISSION_WAYPOINT_ADMIN,
                HytalePermissionsProvider.GROUP_SERVER_EDITOR);
    }

    public static boolean canOpenWaypointUI(@Nonnull final PlayerRef playerRef) {
        return PermissionsModule.get().hasPermission(playerRef.getUuid(), Constants.PERMISSION_WAYPOINT_OPEN, true);
    }

    public static boolean canTeleport(@Nonnull final PlayerRef playerRef) {
        return WaypointlyConfig.get().allowsTeleportForEveryone()
            || PermissionsModule.get().hasPermission(playerRef.getUuid(), Constants.PERMISSION_WAYPOINT_TELEPORT);
    }

    public static boolean canShare(@Nonnull final PlayerRef playerRef) {
        return WaypointlyConfig.get().allowsSharedWaypoints()
            && PermissionsModule.get().hasPermission(playerRef.getUuid(), Constants.PERMISSION_WAYPOINT_SHARED);
    }

    public static boolean canAdminister(@Nonnull final PlayerRef playerRef) {
        return PermissionsModule.get().hasPermission(playerRef.getUuid(), Constants.PERMISSION_WAYPOINT_ADMIN);
    }

    public static void grant(@Nonnull final UUID uuid, @Nonnull final String node) {
        var permissions = PermissionsModule.get();
        permissions.removeUserPermission(uuid, Set.of("-" + node));
        permissions.addUserPermission(uuid, Set.of(node));
    }

    public static void revoke(@Nonnull final UUID uuid, @Nonnull final String node) {
        var permissions = PermissionsModule.get();
        permissions.removeUserPermission(uuid, Set.of(node));
        permissions.addUserPermission(uuid, Set.of("-" + node));
    }
}
