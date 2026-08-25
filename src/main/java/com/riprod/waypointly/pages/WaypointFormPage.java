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
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.waypointly.config.WaypointlyConfig;
import com.riprod.waypointly.util.IconSwatch;
import com.riprod.waypointly.util.PermissionsUtil;
import com.riprod.waypointly.util.Waypoints;

import javax.annotation.Nonnull;

public abstract class WaypointFormPage extends InteractiveCustomUIPage<WaypointFormPage.WaypointFormData>
        implements IconPickerPage.IconSelectable {

    protected String selectedIcon;
    protected String name;
    protected String x;
    protected String z;
    protected String tint;
    protected boolean shared;

    public static class WaypointFormData {
        public String action;
        public String name;
        public String x;
        public String z;
        public String tint;

        public static final BuilderCodec<WaypointFormData> CODEC = BuilderCodec.builder(WaypointFormData.class, WaypointFormData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (WaypointFormData o, String v) -> o.action = v, (WaypointFormData o) -> o.action)
                .add()
                .append(new KeyedCodec<>("@Name", Codec.STRING), (WaypointFormData o, String v) -> o.name = v, (WaypointFormData o) -> o.name)
                .add()
                .append(new KeyedCodec<>("@X", Codec.STRING), (WaypointFormData o, String v) -> o.x = v, (WaypointFormData o) -> o.x)
                .add()
                .append(new KeyedCodec<>("@Z", Codec.STRING), (WaypointFormData o, String v) -> o.z = v, (WaypointFormData o) -> o.z)
                .add()
                .append(new KeyedCodec<>("@Tint", Codec.STRING), (WaypointFormData o, String v) -> o.tint = v, (WaypointFormData o) -> o.tint)
                .add()
                .build();
    }

    protected WaypointFormPage(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, WaypointFormData.CODEC);
    }

    protected abstract String documentPath();

    protected boolean canChangeVisibility() {
        return true;
    }

    protected abstract void submit(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                   @Nonnull Player player, @Nonnull World world, float x, float z);

    @Override
    public void setSelectedIcon(String iconFileName) {
        this.selectedIcon = iconFileName;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder ui, @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        ui.append(documentPath());

        var config = WaypointlyConfig.get();

        ui.set("#WaypointNameInput.Value", name == null ? "" : name);
        ui.set("#XInput.Value", x == null ? "" : x);
        ui.set("#ZInput.Value", z == null ? "" : z);
        ui.set("#TintInput.Value", tint == null ? "" : tint);
        ui.set("#SelectedIconLabel.Text", config.resolveIcon(selectedIcon).getName());
        IconSwatch.apply(ui, "#IconContainer", selectedIcon);

        boolean canShare = canChangeVisibility() && PermissionsUtil.canShare(playerRef);
        ui.set("#VisibilityButton.Visible", canShare);
        ui.set("#VisibilityButton.Text", shared ? "SHARED" : "PERSONAL");

        events.addEventBinding(CustomUIEventBindingType.Activating, "#ChooseIconButton", formValues("ChooseIcon"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#SubmitButton", formValues("Submit"), false);

        if (canShare) {
            events.addEventBinding(CustomUIEventBindingType.Activating, "#VisibilityButton", formValues("ToggleVisibility"), false);
        }

        events.addEventBinding(CustomUIEventBindingType.Activating, "#CancelButton",
                new EventData().append("Action", "Cancel"), false);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton",
                new EventData().append("Action", "Cancel"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull WaypointFormData data) {
        Player player = store.getComponent(ref, Player.getComponentType());
        var world = player.getWorld();

        remember(data);

        switch (data.action) {
            case "ChooseIcon":
                player.getPageManager().openCustomPage(ref, store, new IconPickerPage(playerRef, this));
                break;

            case "ToggleVisibility":
                shared = !shared;
                rebuild();
                break;

            case "Submit": {
                var config = WaypointlyConfig.get();
                if (name == null || name.trim().isEmpty()) {
                    playerRef.sendMessage(Message.raw("Waypoint name cannot be empty."));
                    return;
                }
                if (name.length() > config.getMaxNameLength()) {
                    playerRef.sendMessage(Message.raw("Waypoint names are limited to " + config.getMaxNameLength() + " characters."));
                    return;
                }
                if (tint != null && !tint.isBlank() && Waypoints.parseTint(tint) == null) {
                    playerRef.sendMessage(Message.raw("Tint must look like #RRGGBB."));
                    return;
                }

                try {
                    submit(ref, store, player, world, Float.parseFloat(data.x), Float.parseFloat(data.z));
                } catch (NumberFormatException e) {
                    playerRef.sendMessage(Message.raw("Coordinates must be numbers. Got X=" + data.x + ", Z=" + data.z));
                }
                break;
            }

            case "Cancel":
                openList(ref, store, player, world);
                break;

            default:
                break;
        }
    }

    protected void openList(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Player player, @Nonnull World world) {
        player.getPageManager().openCustomPage(ref, store, new WaypointPage(playerRef, Waypoints.markers(player, world)));
    }

    private static EventData formValues(@Nonnull String action) {
        return new EventData()
                .append("Action", action)
                .append("@Name", "#WaypointNameInput.Value")
                .append("@X", "#XInput.Value")
                .append("@Z", "#ZInput.Value")
                .append("@Tint", "#TintInput.Value");
    }

    private void remember(@Nonnull WaypointFormData data) {
        if (data.name != null) name = data.name;
        if (data.x != null) x = data.x;
        if (data.z != null) z = data.z;
        if (data.tint != null) tint = data.tint;
    }
}
