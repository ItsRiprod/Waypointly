package com.riprod.waypointly.util;

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.riprod.waypointly.config.WaypointlyConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class IconSwatch {

    private IconSwatch() {
    }

    public static void apply(@Nonnull UICommandBuilder ui, @Nonnull String containerSelector, @Nullable String image) {
        ui.set(containerSelector + ".Background", WaypointlyConfig.get().getTexturePath(image));
    }
}
