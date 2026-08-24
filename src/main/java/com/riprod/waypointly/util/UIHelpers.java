package com.riprod.waypointly.util;

import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.riprod.waypointly.Constants;
import com.riprod.waypointly.Icons;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;

import javax.annotation.Nonnull;

/**
 * Utility class for common UI operations across waypoint pages
 */
public class UIHelpers {

    /**
     * Helper method to populate the icon dropdown with available icons
     * @param uiCommandBuilder the UI command builder to use
     * @param selectedIcon the currently selected icon filename
     */
    public static void populateIconDropdown(@Nonnull UICommandBuilder uiCommandBuilder, String selectedIcon) {
        java.util.List<Icons.Icon> icons = Icons.getDefaultIcons();
        DropdownEntryInfo[] iconEntries = new DropdownEntryInfo[icons.size()];
        for (int i = 0; i < icons.size(); i++) {
            Icons.Icon icon = icons.get(i);
            iconEntries[i] = new DropdownEntryInfo(
                LocalizableString.fromString(icon.getDisplayName()), 
                Constants.ICON_PATH_PREFIX + icon.getFileName()
            );
        }
        uiCommandBuilder.set("#IconDropdown.Entries", iconEntries);
        uiCommandBuilder.set("#IconDropdown.Value", Constants.ICON_PATH_PREFIX + selectedIcon);
    }
}
