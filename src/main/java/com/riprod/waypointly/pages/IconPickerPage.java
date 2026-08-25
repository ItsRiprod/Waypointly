package com.riprod.waypointly.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.waypointly.config.WaypointlyConfig;
import com.riprod.waypointly.util.IconSwatch;

import javax.annotation.Nonnull;

public class IconPickerPage extends InteractiveCustomUIPage<IconPickerPage.IconPickerPageData> {

    private final InteractiveCustomUIPage<?> returnPage;
    private final String ICON_LIST_REF = "#IconList";
    private final String ICON_PICKER_ITEM_UI = "Pages/IconPickerItem.ui";

    public static class IconPickerPageData {
        public String action;
        public String iconFileName;

        public static final BuilderCodec<IconPickerPageData> CODEC = BuilderCodec.builder(IconPickerPageData.class, IconPickerPageData::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (IconPickerPageData o, String v) -> o.action = v, (IconPickerPageData o) -> o.action)
                .add()
                .append(new KeyedCodec<>("IconFileName", Codec.STRING), (IconPickerPageData o, String v) -> o.iconFileName = v, (IconPickerPageData o) -> o.iconFileName)
                .add()
                .build();
    }

    public IconPickerPage(@Nonnull PlayerRef playerRef, InteractiveCustomUIPage<?> returnPage) {
        super(playerRef, CustomPageLifetime.CanDismiss, IconPickerPageData.CODEC);
        this.returnPage = returnPage;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/IconPickerPage.ui");
        uiCommandBuilder.clear(ICON_LIST_REF);

        var icons = WaypointlyConfig.get().getIcons();
        for (int i = 0; i < icons.length; i++) {
            var icon = icons[i];
            String iconSelector = ICON_LIST_REF + "[" + i + "]";

            uiCommandBuilder.append(ICON_LIST_REF, ICON_PICKER_ITEM_UI);
            uiCommandBuilder.set(iconSelector + " #IconButton.Text", icon.getName());
            IconSwatch.apply(uiCommandBuilder, iconSelector + " #IconContainer", icon.getImage());

            uiEventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    iconSelector + " #IconButton",
                    new EventData()
                            .append("Action", "Select")
                            .append("IconFileName", icon.getImage()),
                    false
            );
        }

        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#BackButton",
                new EventData().append("Action", "Back"),
                false
        );

        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CloseButton",
            new EventData().append("Action", "Back"),
            false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull IconPickerPageData data) {
        var player = store.getComponent(ref, Player.getComponentType());

        if ("Select".equals(data.action) && returnPage instanceof IconSelectable selectable) {
            selectable.setSelectedIcon(data.iconFileName);
        }

        player.getPageManager().openCustomPage(ref, store, returnPage);
    }

    public interface IconSelectable {
        void setSelectedIcon(String iconFileName);
    }
}
