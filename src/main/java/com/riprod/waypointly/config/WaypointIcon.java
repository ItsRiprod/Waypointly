package com.riprod.waypointly.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;

import javax.annotation.Nonnull;

public final class WaypointIcon {

    public static final String ROOT = "UI/WorldMap/MapMarkers";

    private static final CommonAssetValidator IMAGE_VALIDATOR = new CommonAssetValidator("png", true, ROOT);

    @Nonnull
    public static final BuilderCodec<WaypointIcon> CODEC = BuilderCodec.builder(WaypointIcon.class, WaypointIcon::new)
            .append(new KeyedCodec<>("Name", Codec.STRING),
                    (icon, s) -> icon.name = s,
                    icon -> icon.name)
            .documentation("Label shown in the icon picker")
            .add()
            .append(new KeyedCodec<>("Image", Codec.STRING),
                    (icon, s) -> icon.image = s,
                    icon -> icon.image)
            .documentation("Marker image, picked from " + ROOT + ". This is the same folder the client "
                    + "resolves map marker images from, so the icon shows both in the picker and on the map.")
            .addValidator(IMAGE_VALIDATOR)
            .add()
            .build();

    @Nonnull
    public static final ArrayCodec<WaypointIcon> ARRAY_CODEC = new ArrayCodec<>(CODEC, WaypointIcon[]::new);

    private String name;
    private String image;

    private WaypointIcon() {
    }

    @Nonnull
    static WaypointIcon of(@Nonnull String name, @Nonnull String fileName) {
        var icon = new WaypointIcon();
        icon.name = name;
        icon.image = ROOT + "/" + fileName;
        return icon;
    }

    @Nonnull
    public String getName() {
        return name != null ? name : getFileName();
    }

    @Nonnull
    public String getPath() {
        return image != null ? image : "";
    }

    @Nonnull
    public String getFileName() {
        var path = getPath();
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }

}
