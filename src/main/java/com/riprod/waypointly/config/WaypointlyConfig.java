package com.riprod.waypointly.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.riprod.configly.Config;
import com.riprod.configly.Configly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class WaypointlyConfig extends Config {

    @Nonnull
    public static final String TYPE = "Waypointly";

    public static final int UNLIMITED_WAYPOINTS = -1;

    @Nonnull
    public static final WaypointlyConfig DEFAULTS = new WaypointlyConfig();

    @Nonnull
    public static final BuilderCodec<WaypointlyConfig> CODEC = BuilderCodec
            .builder(WaypointlyConfig.class, WaypointlyConfig::new)
            .append(new KeyedCodec<>("MaxWaypoints", Codec.INTEGER),
                    (config, i) -> config.maxWaypoints = i,
                    config -> config.maxWaypoints)
            .documentation("Maximum personal waypoints one player may hold per world. -1 is unlimited.")
            .addValidator(Validators.min(UNLIMITED_WAYPOINTS))
            .add()
            .append(new KeyedCodec<>("MaxSharedWaypoints", Codec.INTEGER),
                    (config, i) -> config.maxSharedWaypoints = i,
                    config -> config.maxSharedWaypoints)
            .documentation("Maximum shared waypoints one player may contribute per world. -1 is unlimited.")
            .addValidator(Validators.min(UNLIMITED_WAYPOINTS))
            .add()
            .append(new KeyedCodec<>("MaxNameLength", Codec.INTEGER),
                    (config, i) -> config.maxNameLength = i,
                    config -> config.maxNameLength)
            .documentation("Longest waypoint name accepted. The engine rejects names over 24 characters on "
                    + "markers the client creates, so going higher only affects waypoints made through this mod.")
            .addValidator(Validators.min(1))
            .add()
            .append(new KeyedCodec<>("AllowTeleportForEveryone", Codec.BOOLEAN),
                    (config, b) -> config.allowTeleportForEveryone = b,
                    config -> config.allowTeleportForEveryone)
            .documentation("Give every player the warp button regardless of permissions. The engine's own "
                    + "map-marker teleport stays Creative-only either way; this covers the in-mod warp.")
            .add()
            .append(new KeyedCodec<>("AllowSharedWaypoints", Codec.BOOLEAN),
                    (config, b) -> config.allowSharedWaypoints = b,
                    config -> config.allowSharedWaypoints)
            .documentation("Whether players may publish waypoints to the world-wide marker store that "
                    + "everyone in the world sees.")
            .add()
            .append(new KeyedCodec<>("TeleportCooldownSeconds", Codec.FLOAT),
                    (config, f) -> config.teleportCooldownSeconds = f,
                    config -> config.teleportCooldownSeconds)
            .documentation("Seconds a player must wait between warps. 0 disables the cooldown.")
            .addValidator(Validators.min(0f))
            .add()
            .append(new KeyedCodec<>("TeleportWarmupSeconds", Codec.FLOAT),
                    (config, f) -> config.teleportWarmupSeconds = f,
                    config -> config.teleportWarmupSeconds)
            .documentation("Seconds between requesting a warp and being moved. Taking damage during the "
                    + "warmup does not cancel it. 0 warps immediately.")
            .addValidator(Validators.min(0f))
            .add()
            .append(new KeyedCodec<>("DefaultIcon", Codec.STRING),
                    (config, s) -> config.defaultIcon = s,
                    config -> config.defaultIcon)
            .documentation("Image file name preselected for a new waypoint. Must match an Icons entry.")
            .add()
            .append(new KeyedCodec<>("IconTexturePath", Codec.STRING),
                    (config, s) -> config.iconTexturePath = s,
                    config -> config.iconTexturePath)
            .documentation("Prefix joined to each icon Image to build the UI swatch texture path. Runtime "
                    + "texture paths are rooted at Common/UI/Custom. Point this at your own pack folder to use custom art.")
            .add()
            .append(new KeyedCodec<>("Icons", WaypointIcon.ARRAY_CODEC),
                    (config, icons) -> config.icons = icons,
                    config -> config.icons)
            .documentation("Every icon offered in the picker. Add an entry and drop the matching image into "
                    + "the folder IconTexturePath points at to use your own.")
            .add()
            .build();

    private int maxWaypoints = UNLIMITED_WAYPOINTS;
    private int maxSharedWaypoints = UNLIMITED_WAYPOINTS;
    private int maxNameLength = 24;
    private boolean allowTeleportForEveryone = false;
    private boolean allowSharedWaypoints = true;
    private float teleportCooldownSeconds = 0f;
    private float teleportWarmupSeconds = 0f;
    private String defaultIcon = "Coordinate.png";
    private String iconTexturePath = "Markers/";

    private WaypointIcon[] icons = {
        WaypointIcon.of("Campfire", "Campfire.png"),
        WaypointIcon.of("Coordinate", "Coordinate.png"),
        WaypointIcon.of("Death", "Death.png"),
        WaypointIcon.of("Home", "Home.png"),
        WaypointIcon.of("Player", "Player.png"),
        WaypointIcon.of("Portal", "Portal.png"),
        WaypointIcon.of("Portal Invasion", "PortalInvasion.png"),
        WaypointIcon.of("Prefab", "Prefab.png"),
        WaypointIcon.of("Spawn", "Spawn.png"),
        WaypointIcon.of("Temple Gateway", "Temple_Gateway.png"),
        WaypointIcon.of("Warp", "Warp.png")
    };

    private WaypointlyConfig() {
    }

    @Nonnull
    public static WaypointlyConfig get() {
        return Configly.getOrElse(TYPE, WaypointlyConfig.class, DEFAULTS);
    }

    public int getMaxWaypoints() {
        return maxWaypoints;
    }

    public int getMaxSharedWaypoints() {
        return maxSharedWaypoints;
    }

    public int getMaxNameLength() {
        return maxNameLength;
    }

    public boolean allowsTeleportForEveryone() {
        return allowTeleportForEveryone;
    }

    public boolean allowsSharedWaypoints() {
        return allowSharedWaypoints;
    }

    public float getTeleportCooldownSeconds() {
        return teleportCooldownSeconds;
    }

    public float getTeleportWarmupSeconds() {
        return teleportWarmupSeconds;
    }

    @Nonnull
    public String getDefaultIcon() {
        return resolveIcon(defaultIcon).getImage();
    }

    @Nonnull
    public WaypointIcon[] getIcons() {
        return icons;
    }

    @Nonnull
    public String getTexturePath(@Nullable String image) {
        return iconTexturePath + resolveIcon(image).getImage();
    }

    @Nonnull
    public WaypointIcon resolveIcon(@Nullable String image) {
        var fallback = WaypointIcon.of(defaultIcon, defaultIcon);
        for (var icon : icons) {
            if (icon.getImage().equals(image)) {
                return icon;
            }
            if (icon.getImage().equals(defaultIcon)) {
                fallback = icon;
            }
        }
        return fallback;
    }
}
