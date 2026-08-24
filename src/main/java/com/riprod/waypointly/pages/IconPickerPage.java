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
import com.riprod.waypointly.IconNames;
import com.riprod.waypointly.Icons;

import javax.annotation.Nonnull;

public class IconPickerPage extends InteractiveCustomUIPage<IconPickerPage.IconPickerPageData> {
    
    private final InteractiveCustomUIPage<?> returnPage;
    private final String currentIcon;
    private final String ICON_LIST_REF = "#IconList";
    private final String ICON_PICKER_ITEM_UI = "Pages/IconPickerItem.ui";
    
    public static class IconPickerPageData {
        public String action;
        public String iconFileName;

        public static final BuilderCodec<IconPickerPageData> CODEC = ((BuilderCodec.Builder<IconPickerPageData>) ((BuilderCodec.Builder<IconPickerPageData>)
                BuilderCodec.builder(IconPickerPageData.class, IconPickerPageData::new))
                .append(new KeyedCodec<>("Action", Codec.STRING), (IconPickerPageData o, String v) -> o.action = v, (IconPickerPageData o) -> o.action)
                .add()
                .append(new KeyedCodec<>("IconFileName", Codec.STRING), (IconPickerPageData o, String v) -> o.iconFileName = v, (IconPickerPageData o) -> o.iconFileName)
                .add())
                .build();
    }

    public IconPickerPage(@Nonnull PlayerRef playerRef, String currentIcon, InteractiveCustomUIPage<?> returnPage) {
        super(playerRef, CustomPageLifetime.CanDismiss, IconPickerPageData.CODEC);
        this.currentIcon = currentIcon;
        this.returnPage = returnPage;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Pages/IconPickerPage.ui");
        uiCommandBuilder.clear(ICON_LIST_REF);

        // Add all icons as simple list items
        java.util.List<Icons.Icon> icons = Icons.getDefaultIcons();
        
        for (int i = 0; i < icons.size(); i++) {
            Icons.Icon icon = icons.get(i);
            String iconSelector = ICON_LIST_REF + "[" + i + "]";
            
            // Append the icon item
            uiCommandBuilder.append(ICON_LIST_REF, ICON_PICKER_ITEM_UI);
            
            // Set the button text to the icon name
            uiCommandBuilder.set(iconSelector + " #IconButton.Text", icon.getDisplayName());
            
            // Append the icon image to the icon container
            uiCommandBuilder.append(iconSelector + " #IconContainer", IconNames.resolveIconUiPath(icon.getFileName()));
            
            // Add event binding for this icon button
            uiEventBuilder.addEventBinding(
                    CustomUIEventBindingType.Activating,
                    iconSelector + " #IconButton",
                    new EventData()
                            .append("Action", "Select")
                            .append("IconFileName", icon.getFileName()),
                    false
            );
        }

        // Add event binding for Back button
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#BackButton",
                new EventData().append("Action", "Back"),
                false
        );

        // Add event binding for Close (container close button) -> behave like Back
        uiEventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#CloseButton",
            new EventData().append("Action", "Back"),
            false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull IconPickerPageData data) {
        Player player = store.getComponent(ref, Player.getComponentType());

        switch (data.action) {
            case "Select":
                if (returnPage instanceof AddWaypointPage) {
                    ((AddWaypointPage) returnPage).setSelectedIcon(data.iconFileName);
                } else if (returnPage instanceof EditWaypointPage) {
                    ((EditWaypointPage) returnPage).setSelectedIcon(data.iconFileName);
                }
                player.getPageManager().openCustomPage(ref, store, returnPage);
                break;

            case "Back":
                player.getPageManager().openCustomPage(ref, store, returnPage);
                break;

            default:
                break;
        }
    }
}
