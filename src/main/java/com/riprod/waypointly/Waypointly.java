package com.riprod.waypointly;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import com.riprod.waypointly.commands.waypoints.ListWaypointsCommand;
import com.riprod.waypointly.commands.waypoints.ResetWaypointsCommand;
import com.riprod.waypointly.commands.waypoints.WaypointCommand;
import com.riprod.waypointly.commands.waypoints.WaypointTeleportCommand;
import com.riprod.waypointly.config.WaypointsConfig;

import javax.annotation.Nonnull;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 */
public class Waypointly extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final Config<WaypointsConfig> config;

    public Waypointly(@Nonnull JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
        
        // Initialize config
        this.config = this.withConfig("waypoints_config", WaypointsConfig.CODEC);
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        
        // Save config to disk
        this.config.save();
        
        this.getCommandRegistry().registerCommand(new WaypointCommand(config));
        this.getCommandRegistry().registerCommand(new ResetWaypointsCommand());
        this.getCommandRegistry().registerCommand(new ListWaypointsCommand());
        this.getCommandRegistry().registerCommand(new WaypointTeleportCommand());

    }
    
    public Config<WaypointsConfig> getConfig() {
        return config;
    }
}