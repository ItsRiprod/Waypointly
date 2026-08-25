package com.riprod.waypointly.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

import javax.annotation.Nonnull;

public final class WaypointIcon {

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
            .documentation("Image file name, resolved against IconTexturePath for the UI swatch and sent "
                    + "verbatim to the client as the map marker image")
            .add()
            .build();

    @Nonnull
    public static final ArrayCodec<WaypointIcon> ARRAY_CODEC = new ArrayCodec<>(CODEC, WaypointIcon[]::new);

    private String name;
    private String image;

    private WaypointIcon() {
    }

    @Nonnull
    static WaypointIcon of(@Nonnull String name, @Nonnull String image) {
        var icon = new WaypointIcon();
        icon.name = name;
        icon.image = image;
        return icon;
    }

    @Nonnull
    public String getName() {
        return name != null ? name : getImage();
    }

    @Nonnull
    public String getImage() {
        return image != null ? image : "";
    }
}
