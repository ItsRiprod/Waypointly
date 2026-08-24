package com.riprod.waypointly;

import java.util.Set;

public final class IconNames {
    public static final Set<String> VALID_ICON_NAMES = Set.of(
            "IconSpawn",
            "IconCampfire",
            "IconCoordinate",
            "IconDeath",
            "IconHome",
            "IconPlayer",
            "IconPortal",
            "IconPortalInvasion",
            "IconPrefab",
            "IconTemple_Gateway",
            "IconWarp"
    );

    private static final String DEFAULT_ICON_UI = "Pages/Icons/IconSpawn.ui";

    private IconNames() {}

    public static String stripPng(String fileName) {
        if (fileName == null) return null;
        if (fileName.endsWith(".png")) return fileName.substring(0, fileName.length() - 4);
        return fileName;
    }

    public static String resolveIconUiPath(String fileName) {
        String base = stripPng(fileName);
        if (base == null) return DEFAULT_ICON_UI;

        String plainName = base.startsWith("Icon") ? base.substring(4) : base;

        if (VALID_ICON_NAMES.contains(base) || VALID_ICON_NAMES.contains(plainName) || VALID_ICON_NAMES.contains("Icon" + plainName)) {
            return "Pages/Icons/Icon" + plainName + ".ui";
        }

        return DEFAULT_ICON_UI;
    }
}
