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
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.core.util.Config;
import com.riprod.waypointly.Icons;
import com.riprod.waypointly.config.WaypointsConfig;
import com.riprod.waypointly.util.Waypoints;

import javax.annotation.Nonnull;

public class EditWaypointPage extends InteractiveCustomUIPage<EditWaypointPage.EditWaypointPageData> {

    private final UserMapMarker waypoint;
    private final Config<WaypointsConfig> config;
    private String selectedIcon;
    private String selectedIconDisplayName;
    private String savedName = null;
    private String savedX = null;
    private String savedZ = null;

    public static class EditWaypointPageData {
        public String action;
        public String name;
        public String x;
        public String z;

        public static final BuilderCodec<EditWaypointPageData> CODEC = BuilderCodec.builder(EditWaypointPageData.class, EditWaypointPageData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (EditWaypointPageData o, String v) -> o.action = v, (EditWaypointPageData o) -> o.action)
                .add()
                .append(new KeyedCodec<>("@Name", Codec.STRING), (EditWaypointPageData o, String v) -> o.name = v, (EditWaypointPageData o) -> o.name)
                .add()
                .append(new KeyedCodec<>("@X", Codec.STRING), (EditWaypointPageData o, String v) -> o.x = v, (EditWaypointPageData o) -> o.x)
                .add()
                .append(new KeyedCodec<>("@Z", Codec.STRING), (EditWaypointPageData o, String v) -> o.z = v, (EditWaypointPageData o) -> o.z)
                .add()
                .build();
    }

    public EditWaypointPage(@Nonnull PlayerRef playerRef, @Nonnull UserMapMarker waypoint, Config<WaypointsConfig> config) {
        super(playerRef, CustomPageLifetime.CanDismiss, EditWaypointPageData.CODEC);
        this.waypoint = waypoint;
        this.config = config;
        this.selectedIcon = waypoint.getIcon();
        this.selectedIconDisplayName = displayNameFor(waypoint.getIcon());
    }

    public void setSelectedIcon(String iconFileName) {
        this.selectedIcon = iconFileName;
        this.selectedIconDisplayName = displayNameFor(iconFileName);
    }

    private static String displayNameFor(String iconFileName) {
        for (Icons.Icon icon : Icons.getDefaultIcons()) {
            if (icon.getFileName().equals(iconFileName)) {
                return icon.getDisplayName();
            }
        }
        return "Unknown";
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/EditWaypointPage.ui");

        uiCommandBuilder.set("#WaypointNameInput.Value", savedName != null ? savedName : waypoint.getName());
        uiCommandBuilder.set("#XInput.Value", savedX != null ? savedX : String.format("%.2f", waypoint.getX()));
        uiCommandBuilder.set("#ZInput.Value", savedZ != null ? savedZ : String.format("%.2f", waypoint.getZ()));

        uiCommandBuilder.set("#SelectedIconLabel.Text", selectedIconDisplayName);

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#ChooseIconButton",
                new EventData()
                        .append("Action", "ChooseIcon")
                        .append("@Name", "#WaypointNameInput.Value")
                        .append("@X", "#XInput.Value")
                        .append("@Z", "#ZInput.Value"),
                false
        );

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#SaveButton",
                new EventData()
                        .append("Action", "Save")
                        .append("@Name", "#WaypointNameInput.Value")
                        .append("@X", "#XInput.Value")
                        .append("@Z", "#ZInput.Value"),
                false
        );

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#CancelButton",
                new EventData().append("Action", "Cancel"),
                false
        );

        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CloseButton",
            new EventData().append("Action", "Cancel"),
            false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull EditWaypointPageData data) {
        Player player = store.getComponent(ref, Player.getComponentType());
        var world = player.getWorld();
        var perWorldData = player.getPlayerConfigData().getPerWorldData(world.getName());

        switch (data.action) {
            case "ChooseIcon":
                savedName = data.name;
                savedX = data.x;
                savedZ = data.z;

                player.getPageManager().openCustomPage(ref, store, new IconPickerPage(playerRef, selectedIcon, this));
                break;

            case "Save": {
                if (data.name == null || data.name.trim().isEmpty()) {
                    playerRef.sendMessage(Message.raw("Error: Waypoint name cannot be empty."));
                    return;
                }

                var markers = Waypoints.markers(player, world.getName());
                var stored = perWorldData.getUserMapMarker(waypoint.getId());
                if (stored == null) {
                    playerRef.sendMessage(Message.raw("No waypoint was found with that ID."));
                    break;
                }

                try {
                    stored.setPosition(Float.parseFloat(data.x), Float.parseFloat(data.z));
                    stored.setName(data.name);
                    stored.setIcon(selectedIcon);
                } catch (NumberFormatException e) {
                    playerRef.sendMessage(Message.raw("Error: Invalid coordinates. Please enter valid numbers."));
                    break;
                }

                perWorldData.setUserMapMarkers(markers);
                playerRef.sendMessage(Message.raw("Waypoint updated successfully: " + data.name));

                player.getPageManager().openCustomPage(ref, store, new WaypointPage(playerRef, markers, config));
                break;
            }

            case "Cancel":
                player.getPageManager().openCustomPage(ref, store, new WaypointPage(playerRef, Waypoints.markers(player, world.getName()), config));
                break;

            default:
                break;
        }
    }
}
