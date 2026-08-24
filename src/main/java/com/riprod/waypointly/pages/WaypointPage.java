package com.riprod.waypointly.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.core.util.Config;
import com.hypixel.hytale.protocol.Position;
import com.riprod.waypointly.IconNames;
import com.riprod.waypointly.config.WaypointsConfig;
import com.riprod.waypointly.util.PermissionsUtil;
import com.riprod.waypointly.util.Waypoints;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WaypointPage extends InteractiveCustomUIPage<WaypointPage.WaypointPageData> {
    private final List<UserMapMarker> waypoints;
    private final Config<WaypointsConfig> config;
    private final String initialQuery;
    private final String WAYPOINTS_LIST_REF = "#WaypointsList";
    private final String WAYPOINT_ITEM_UI = "Pages/WaypointItem.ui";
    private long lastSearchTimestamp = 0L;
    private String lastSearchQuery = "";
    private String currentSort = "distance";
    private List<String> displayedIds = List.of();

    public static class WaypointPageData {
        public String action;
        public String waypointId;
        public String query;
        public String sort;

        public static final BuilderCodec<WaypointPageData> CODEC = BuilderCodec.builder(WaypointPageData.class, WaypointPageData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (WaypointPageData o, String v) -> o.action = v, (WaypointPageData o) -> o.action)
                .add()
                .append(new KeyedCodec<>("WaypointId", Codec.STRING), (WaypointPageData o, String v) -> o.waypointId = v, (WaypointPageData o) -> o.waypointId)
                .add()
                .append(new KeyedCodec<>("@Query", Codec.STRING), (WaypointPageData o, String v) -> o.query = v, (WaypointPageData o) -> o.query)
                .add()
                .append(new KeyedCodec<>("@Sort", Codec.STRING), (WaypointPageData o, String v) -> o.sort = v, (WaypointPageData o) -> o.sort)
                .add()
                .build();
    }

    public WaypointPage(@Nonnull PlayerRef playerRef, @Nonnull List<UserMapMarker> waypoints, Config<WaypointsConfig> config) {
        this(playerRef, waypoints, config, "");
    }

    public WaypointPage(@Nonnull PlayerRef playerRef, @Nonnull List<UserMapMarker> waypoints, Config<WaypointsConfig> config, String initialQuery) {
        super(playerRef, CustomPageLifetime.CanDismiss, WaypointPageData.CODEC);
        this.waypoints = waypoints;
        this.config = config;
        this.initialQuery = initialQuery != null ? initialQuery : "";
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/WaypointPage.ui");
        uiCommandBuilder.clear(WAYPOINTS_LIST_REF);

        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#SearchInput",
            new EventData().append("Action", "Search").append("@Query", "#SearchInput.Value"),
            false
        );

        DropdownEntryInfo[] sortEntries = new DropdownEntryInfo[]{
            new DropdownEntryInfo(LocalizableString.fromString("Distance"), "distance"),
            new DropdownEntryInfo(LocalizableString.fromString("Name"), "name")
        };
        uiCommandBuilder.set("#SortDropdown.Entries", sortEntries);
        uiCommandBuilder.set("#SortDropdown.Value", this.currentSort);
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#SortDropdown",
            new EventData().append("Action", "Sort").append("@Sort", "#SortDropdown.Value"),
            false
        );

        if (this.initialQuery != null && !this.initialQuery.isEmpty()) {
            uiCommandBuilder.set("#SearchInput.Value", this.initialQuery);
        }

        populateList(ref, store, uiCommandBuilder, uiEventBuilder, sortByCurrentMode(ref, store, this.waypoints));

        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CloseButton",
            new EventData().append("Action", "Close"),
            false
        );
    }

    private void refreshWaypoints(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull List<UserMapMarker> markers, String query) {
        var waypointsWithDistance = sortByCurrentMode(ref, store, markers);
        if (waypointsWithDistance.stream().map(w -> w.waypoint.getId()).toList().equals(this.displayedIds)) {
            return;
        }

        UICommandBuilder ui = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();

        ui.clear(WAYPOINTS_LIST_REF);

        if (query != null) {
            ui.set("#SearchInput.Value", query);
        }

        populateList(ref, store, ui, events, waypointsWithDistance);

        this.sendUpdate(ui, events, false);
    }

    @Nonnull
    private List<WaypointWithDistance> sortByCurrentMode(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull List<UserMapMarker> markers) {
        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        Position playerPosition = transformComponent.getSentTransform().position;

        List<WaypointWithDistance> waypointsWithDistance = new ArrayList<>();
        for (var waypoint : markers) {
            waypointsWithDistance.add(new WaypointWithDistance(waypoint, horizontalDistance(playerPosition, waypoint)));
        }

        if ("name".equalsIgnoreCase(this.currentSort)) {
            waypointsWithDistance.sort(Comparator.comparing(w -> w.waypoint.getName() != null ? w.waypoint.getName().toLowerCase() : ""));
        } else {
            waypointsWithDistance.sort(Comparator.comparingDouble(w -> w.distance));
        }

        return waypointsWithDistance;
    }

    private void populateList(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull List<WaypointWithDistance> waypointsWithDistance) {
        boolean canTeleport = PermissionsUtil.canTeleport(playerRef);

        this.displayedIds = waypointsWithDistance.stream().map(w -> w.waypoint.getId()).toList();

        events.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CreateWaypointButton",
            new EventData().append("Action", "Create"),
            false
        );

        if (waypointsWithDistance.isEmpty()) {
            ui.appendInline(WAYPOINTS_LIST_REF, "Label { Text: \"No waypoints\"; Anchor: (Height: 40); Style: (FontSize: 14, TextColor: #6e7da1, HorizontalAlignment: Center, VerticalAlignment: Center); }");
            return;
        }

        int i = 0;
        for (var waypointData : waypointsWithDistance) {
            String selector = "#WaypointsList[" + i + "]";
            ui.append(WAYPOINTS_LIST_REF, WAYPOINT_ITEM_UI);

            String waypointId = waypointData.waypoint.getId();
            String coordinatesText = String.format("X: %.0f  Z: %.0f  -  %.1f blocks away",
                waypointData.waypoint.getX(), waypointData.waypoint.getZ(), waypointData.distance);

            ui.set(selector + " #WaypointName.Text", waypointData.waypoint.getName());
            ui.set(selector + " #WaypointCoordinates.Text", coordinatesText);
            ui.append(selector + " #IconContainer", IconNames.resolveIconUiPath(waypointData.waypoint.getIcon()));
            ui.set(selector + " #TeleportButton.Visible", canTeleport);

            if (canTeleport) {
                events.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #TeleportButton",
                    new EventData().append("Action", "Teleport").append("WaypointId", waypointId),
                    false
                );
            }

            events.addEventBinding(
                CustomUIEventBindingType.Activating,
                selector + " #EditButton",
                new EventData().append("Action", "Edit").append("WaypointId", waypointId),
                false
            );

            events.addEventBinding(
                CustomUIEventBindingType.Activating,
                selector + " #RemoveButton",
                new EventData().append("Action", "Remove").append("WaypointId", waypointId),
                false
            );

            i++;
        }
    }

    private double horizontalDistance(@Nonnull Position playerPosition, @Nonnull UserMapMarker marker) {
        double dx = marker.getX() - playerPosition.x;
        double dz = marker.getZ() - playerPosition.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static class WaypointWithDistance {
        final UserMapMarker waypoint;
        final double distance;

        WaypointWithDistance(UserMapMarker waypoint, double distance) {
            this.waypoint = waypoint;
            this.distance = distance;
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull WaypointPageData data) {
        Player player = store.getComponent(ref, Player.getComponentType());
        var world = player.getWorld();
        var perWorldData = player.getPlayerConfigData().getPerWorldData(world.getName());

        switch (data.action) {
            case "Teleport": {
                if (data.waypointId == null || data.waypointId.isEmpty()) break;

                var waypoint = perWorldData.getUserMapMarker(data.waypointId);
                if (waypoint == null) {
                    playerRef.sendMessage(Message.raw("No waypoint was found with that ID."));
                    break;
                }

                Waypoints.teleport(ref, world, waypoint);
                playerRef.sendMessage(Message.raw("Teleported to '" + waypoint.getName() + "'!"));
                break;
            }
            case "Edit": {
                if (data.waypointId == null || data.waypointId.isEmpty()) break;

                var waypoint = perWorldData.getUserMapMarker(data.waypointId);
                if (waypoint == null) {
                    playerRef.sendMessage(Message.raw("No waypoint was found with that ID."));
                    break;
                }

                player.getPageManager().openCustomPage(ref, store, new EditWaypointPage(playerRef, waypoint, config));
                break;
            }
            case "Remove": {
                if (data.waypointId == null || data.waypointId.isEmpty()) break;

                if (perWorldData.getUserMapMarker(data.waypointId) == null) {
                    playerRef.sendMessage(Message.raw("No waypoint was found with that ID."));
                    break;
                }

                perWorldData.removeUserMapMarker(data.waypointId);
                playerRef.sendMessage(Message.raw("Waypoint removed successfully."));
                player.getPageManager().openCustomPage(ref, store, new WaypointPage(playerRef, Waypoints.markers(player, world.getName()), config));
                break;
            }
            case "Create":
                player.getPageManager().openCustomPage(ref, store, new AddWaypointPage(playerRef, config));
                break;
            case "Search": {
                String q = data.query != null ? data.query.trim() : "";
                long now = System.currentTimeMillis();
                if (q.equals(this.lastSearchQuery) && (now - this.lastSearchTimestamp) < 1000) {
                    break;
                }
                this.lastSearchTimestamp = now;
                this.lastSearchQuery = q;

                if (q.isEmpty()) {
                    refreshWaypoints(ref, store, Waypoints.markers(player, world.getName()), "");
                    break;
                }

                String qLower = q.toLowerCase();
                List<UserMapMarker> filtered = new ArrayList<>();
                for (var marker : this.waypoints) {
                    String name = marker.getName() != null ? marker.getName() : "";
                    String id = marker.getId() != null ? marker.getId() : "";
                    if (name.toLowerCase().contains(qLower) || id.toLowerCase().contains(qLower)) {
                        filtered.add(marker);
                    }
                }

                refreshWaypoints(ref, store, filtered, q);
                break;
            }
            case "Sort":
                this.currentSort = data.sort != null ? data.sort : "distance";
                refreshWaypoints(ref, store, Waypoints.markers(player, world.getName()), this.lastSearchQuery);
                break;
            case "Close":
                this.close();
                break;
            default:
                break;
        }
    }
}
