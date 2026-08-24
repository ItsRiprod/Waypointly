package com.riprod.waypointly.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.riprod.waypointly.Constants;

/**
 * Configuration class for Waypoints plugin
 * Contains settings that can be modified by server administrators
 */
public class WaypointsConfig {
    
    /**
     * Maximum number of waypoints a player can create per world
     * Use {@link Constants#DEFAULT_MAX_WAYPOINTS} for the default (unlimited sentinel)
     */
    private int maxWaypoints = Constants.DEFAULT_MAX_WAYPOINTS;

    /**
     * Codec for serializing and deserializing the configuration
     */
    public static final BuilderCodec<WaypointsConfig> CODEC = ((BuilderCodec.Builder<WaypointsConfig>)
            BuilderCodec.builder(WaypointsConfig.class, WaypointsConfig::new))
            .append(
                    new KeyedCodec<>("MaxWaypoints", Codec.INTEGER),
                    (WaypointsConfig config, Integer value) -> config.maxWaypoints = value,
                    WaypointsConfig::getMaxWaypoints
            )
            .add()
            .build();

    /**
     * Get the maximum number of waypoints allowed per world
     * @return Maximum waypoints (use {@link Constants#DEFAULT_MAX_WAYPOINTS} for unlimited)
     */
    public int getMaxWaypoints() {
        return maxWaypoints;
    }
}
