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
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.riprod.waypointly.Constants;
import com.riprod.waypointly.Icons;
import com.riprod.waypointly.config.WaypointsConfig;
import com.riprod.waypointly.util.Waypoints;

import javax.annotation.Nonnull;

public class AddWaypointPage extends InteractiveCustomUIPage<AddWaypointPage.AddWaypointPageData> {

    private final Config<WaypointsConfig> config;
    private String selectedIcon = "Coordinate.png";
    private String selectedIconDisplayName = "Coordinate";
    private String savedName = null;
    private String savedX = null;
    private String savedZ = null;

    public static class AddWaypointPageData {
        public String action;
        public String name;
        public String x;
        public String z;

        public static final BuilderCodec<AddWaypointPageData> CODEC = BuilderCodec.builder(AddWaypointPageData.class, AddWaypointPageData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (AddWaypointPageData o, String v) -> o.action = v, (AddWaypointPageData o) -> o.action)
                .add()
                .append(new KeyedCodec<>("@Name", Codec.STRING), (AddWaypointPageData o, String v) -> o.name = v, (AddWaypointPageData o) -> o.name)
                .add()
                .append(new KeyedCodec<>("@X", Codec.STRING), (AddWaypointPageData o, String v) -> o.x = v, (AddWaypointPageData o) -> o.x)
                .add()
                .append(new KeyedCodec<>("@Z", Codec.STRING), (AddWaypointPageData o, String v) -> o.z = v, (AddWaypointPageData o) -> o.z)
                .add()
                .build();
    }

    public AddWaypointPage(@Nonnull PlayerRef playerRef, Config<WaypointsConfig> config) {
        super(playerRef, CustomPageLifetime.CanDismiss, AddWaypointPageData.CODEC);
        this.config = config;
    }

    public void setSelectedIcon(String iconFileName) {
        this.selectedIcon = iconFileName;
        for (Icons.Icon icon : Icons.getDefaultIcons()) {
            if (icon.getFileName().equals(iconFileName)) {
                this.selectedIconDisplayName = icon.getDisplayName();
                break;
            }
        }
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/AddWaypointPage.ui");

        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        var position = transformComponent.getSentTransform().position;

        if (savedName != null) {
            uiCommandBuilder.set("#WaypointNameInput.Value", savedName);
        }
        uiCommandBuilder.set("#XInput.Value", savedX != null ? savedX : String.format("%.2f", position.x));
        uiCommandBuilder.set("#ZInput.Value", savedZ != null ? savedZ : String.format("%.2f", position.z));

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
                "#AddButton",
                new EventData()
                        .append("Action", "Add")
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
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull AddWaypointPageData data) {
        Player player = store.getComponent(ref, Player.getComponentType());
        var world = player.getWorld();

        switch (data.action) {
            case "ChooseIcon":
                savedName = data.name;
                savedX = data.x;
                savedZ = data.z;

                player.getPageManager().openCustomPage(ref, store, new IconPickerPage(playerRef, selectedIcon, this));
                break;

            case "Add": {
                if (data.name == null || data.name.trim().isEmpty()) {
                    playerRef.sendMessage(Message.raw("Error: Waypoint name cannot be empty."));
                    return;
                }

                var perWorldData = player.getPlayerConfigData().getPerWorldData(world.getName());
                int maxWaypoints = config.get().getMaxWaypoints();
                if (maxWaypoints != Constants.DEFAULT_MAX_WAYPOINTS && perWorldData.getUserMapMarkers().size() >= maxWaypoints) {
                    playerRef.sendMessage(Message.raw("Error: You have reached the maximum number of waypoints (" + maxWaypoints + ")."));
                    return;
                }

                try {
                    float x = Float.parseFloat(data.x);
                    float z = Float.parseFloat(data.z);

                    perWorldData.addUserMapMarker(Waypoints.create(playerRef, data.name, x, z, selectedIcon));
                    playerRef.sendMessage(Message.raw("Waypoint created: " + data.name));

                    player.getPageManager().openCustomPage(ref, store, new WaypointPage(playerRef, Waypoints.markers(player, world.getName()), config));
                } catch (NumberFormatException e) {
                    playerRef.sendMessage(Message.raw("Error: Invalid coordinates. Please enter valid numbers. Coordinates sent: X=" + data.x + ", Z=" + data.z));
                }
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
