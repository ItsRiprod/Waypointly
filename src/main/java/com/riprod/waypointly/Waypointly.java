package com.riprod.waypointly;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.configly.Configly;
import com.riprod.waypointly.commands.waypoints.ListWaypointsCommand;
import com.riprod.waypointly.commands.waypoints.ResetWaypointsCommand;
import com.riprod.waypointly.commands.waypoints.WaypointCommand;
import com.riprod.waypointly.commands.waypoints.WaypointTeleportCommand;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.riprod.waypointly.config.WaypointlyConfig;
import com.riprod.waypointly.map.WaypointMarkerProvider;
import com.riprod.waypointly.util.PermissionsUtil;
import com.riprod.waypointly.warp.WarpComponent;
import com.riprod.waypointly.warp.WarpSystems;

import javax.annotation.Nonnull;

public class Waypointly extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public Waypointly(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up %s %s", getName(), getManifest().getVersion());

        Configly.register(WaypointlyConfig.TYPE, WaypointlyConfig.class, WaypointlyConfig.CODEC);
        PermissionsUtil.registerPermissions();

        final var entityStoreRegistry = getEntityStoreRegistry();
        var warpComponentType = entityStoreRegistry.registerComponent(WarpComponent.class, WarpComponent::new);
        WarpComponent.setComponentType(warpComponentType);
        entityStoreRegistry.registerSystem(new WarpSystems.Tick());

        getEventRegistry().registerGlobal(AddWorldEvent.class, event -> {
            var worldMapManager = event.getWorld().getWorldMapManager();
            worldMapManager.addMarkerProvider(WaypointMarkerProvider.PERSONAL_KEY, WaypointMarkerProvider.PERSONAL);
            worldMapManager.addMarkerProvider(WaypointMarkerProvider.SHARED_KEY, WaypointMarkerProvider.SHARED);
        });

        getCommandRegistry().registerCommand(new WaypointCommand());
        getCommandRegistry().registerCommand(new ResetWaypointsCommand());
        getCommandRegistry().registerCommand(new ListWaypointsCommand());
        getCommandRegistry().registerCommand(new WaypointTeleportCommand());
    }
}
