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
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.protocol.Position;
import com.riprod.waypointly.util.IconSwatch;
import com.riprod.waypointly.util.PermissionsUtil;
import com.riprod.waypointly.util.Waypoints;
import com.riprod.waypointly.warp.Warps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WaypointPage extends InteractiveCustomUIPage<WaypointPage.WaypointPageData> {
    private final List<UserMapMarker> waypoints;
    private final String WAYPOINTS_LIST_REF = "#WaypointsList";
    private final String WAYPOINT_ITEM_UI = "Pages/WaypointItem.ui";
    private String query = "";
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

    public WaypointPage(@Nonnull PlayerRef playerRef, @Nonnull List<UserMapMarker> waypoints) {
        super(playerRef, CustomPageLifetime.CanDismiss, WaypointPageData.CODEC);
        this.waypoints = waypoints;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append("Pages/WaypointPage.ui");
        ui.clear(WAYPOINTS_LIST_REF);

        events.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#SearchInput",
            new EventData().append("Action", "Search").append("@Query", "#SearchInput.Value"),
            false
        );

        DropdownEntryInfo[] sortEntries = new DropdownEntryInfo[]{
            new DropdownEntryInfo(LocalizableString.fromString("Distance"), "distance"),
            new DropdownEntryInfo(LocalizableString.fromString("Name"), "name")
        };
        ui.set("#SortDropdown.Entries", sortEntries);
        ui.set("#SortDropdown.Value", this.currentSort);
        events.addEventBinding(
            CustomUIEventBindingType.ValueChanged,
            "#SortDropdown",
            new EventData().append("Action", "Sort").append("@Sort", "#SortDropdown.Value"),
            false
        );

        if (!this.query.isEmpty()) {
            ui.set("#SearchInput.Value", this.query);
        }

        populateList(ref, store, ui, events, sorted(ref, store, filtered()));

        events.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CloseButton",
            new EventData().append("Action", "Close"),
            false
        );
    }

    private void refresh(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull List<UserMapMarker> markers) {
        var ordered = sorted(ref, store, markers);
        if (ordered.stream().map(w -> w.waypoint.getId()).toList().equals(this.displayedIds)) {
            return;
        }

        UICommandBuilder ui = new UICommandBuilder();
        UIEventBuilder events = new UIEventBuilder();

        ui.clear(WAYPOINTS_LIST_REF);
        ui.set("#SearchInput.Value", this.query);

        populateList(ref, store, ui, events, ordered);

        this.sendUpdate(ui, events, false);
    }

    @Nonnull
    private List<UserMapMarker> filtered() {
        if (query.isEmpty()) return waypoints;

        var lower = query.toLowerCase();
        List<UserMapMarker> matches = new ArrayList<>();
        for (var marker : waypoints) {
            if (Waypoints.displayName(marker).toLowerCase().contains(lower)) {
                matches.add(marker);
            }
        }
        return matches;
    }

    @Nonnull
    private List<WaypointWithDistance> sorted(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull List<UserMapMarker> markers) {
        Position playerPosition = store.getComponent(ref, TransformComponent.getComponentType()).getSentTransform().position;

        List<WaypointWithDistance> withDistance = new ArrayList<>();
        for (var waypoint : markers) {
            withDistance.add(new WaypointWithDistance(waypoint, horizontalDistance(playerPosition, waypoint)));
        }

        if ("name".equalsIgnoreCase(this.currentSort)) {
            withDistance.sort(Comparator.comparing(w -> Waypoints.displayName(w.waypoint).toLowerCase()));
        } else {
            withDistance.sort(Comparator.comparingDouble(w -> w.distance));
        }

        return withDistance;
    }

    private void populateList(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull List<WaypointWithDistance> ordered) {
        boolean canWarp = PermissionsUtil.canTeleport(playerRef);

        this.displayedIds = ordered.stream().map(w -> w.waypoint.getId()).toList();

        events.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CreateWaypointButton",
            new EventData().append("Action", "Create"),
            false
        );

        if (ordered.isEmpty()) {
            ui.appendInline(WAYPOINTS_LIST_REF, "Label { Text: \"No waypoints\"; Anchor: (Height: 40); Style: (FontSize: 14, TextColor: #6e7da1, HorizontalAlignment: Center, VerticalAlignment: Center); }");
            return;
        }

        int i = 0;
        for (var entry : ordered) {
            String selector = "#WaypointsList[" + i + "]";
            ui.append(WAYPOINTS_LIST_REF, WAYPOINT_ITEM_UI);

            String waypointId = entry.waypoint.getId();
            String coordinatesText = String.format("X: %.0f  Z: %.0f  -  %.1f blocks away%s",
                entry.waypoint.getX(), entry.waypoint.getZ(), entry.distance,
                Waypoints.isShared(entry.waypoint) ? "  -  shared" : "");

            ui.set(selector + " #WaypointName.Text", Waypoints.displayName(entry.waypoint));
            ui.set(selector + " #WaypointCoordinates.Text", coordinatesText);
            IconSwatch.apply(ui, selector + " #IconContainer", entry.waypoint.getIcon());
            ui.set(selector + " #TeleportButton.Visible", canWarp);

            if (canWarp) {
                events.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    selector + " #TeleportButton",
                    new EventData().append("Action", "Warp").append("WaypointId", waypointId),
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

    private record WaypointWithDistance(UserMapMarker waypoint, double distance) {
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull WaypointPageData data) {
        Player player = store.getComponent(ref, Player.getComponentType());
        var world = player.getWorld();

        switch (data.action) {
            case "Warp": {
                var waypoint = resolve(player, world, data.waypointId);
                if (waypoint == null) break;

                Warps.request(ref, store, playerRef, world, waypoint);
                break;
            }
            case "Edit": {
                var waypoint = resolve(player, world, data.waypointId);
                if (waypoint == null) break;

                if (!canModify(waypoint)) {
                    playerRef.sendMessage(Message.raw("That shared waypoint belongs to someone else."));
                    break;
                }

                player.getPageManager().openCustomPage(ref, store, new EditWaypointPage(playerRef, waypoint));
                break;
            }
            case "Remove": {
                var waypoint = resolve(player, world, data.waypointId);
                if (waypoint == null) break;

                if (!canModify(waypoint)) {
                    playerRef.sendMessage(Message.raw("That shared waypoint belongs to someone else."));
                    break;
                }

                Waypoints.store(player, world, Waypoints.isShared(waypoint)).removeUserMapMarker(waypoint.getId());
                playerRef.sendMessage(Message.raw("Waypoint removed successfully."));
                player.getPageManager().openCustomPage(ref, store, new WaypointPage(playerRef, Waypoints.markers(player, world)));
                break;
            }
            case "Create":
                player.getPageManager().openCustomPage(ref, store, new AddWaypointPage(playerRef));
                break;
            case "Search":
                this.query = data.query != null ? data.query.trim() : "";
                refresh(ref, store, filtered());
                break;
            case "Sort":
                this.currentSort = data.sort != null ? data.sort : "distance";
                refresh(ref, store, filtered());
                break;
            case "Close":
                this.close();
                break;
            default:
                break;
        }
    }

    @Nullable
    private UserMapMarker resolve(@Nonnull Player player, @Nonnull World world, String waypointId) {
        if (waypointId == null || waypointId.isEmpty()) return null;

        var waypoint = Waypoints.find(player, world, waypointId);
        if (waypoint == null) {
            playerRef.sendMessage(Message.raw("No waypoint was found with that ID."));
        }
        return waypoint;
    }

    private boolean canModify(@Nonnull UserMapMarker waypoint) {
        return !Waypoints.isShared(waypoint)
            || playerRef.getUuid().equals(waypoint.getCreatedByUuid())
            || PermissionsUtil.canAdminister(playerRef);
    }
}
