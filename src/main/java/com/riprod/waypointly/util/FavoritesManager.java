package com.riprod.waypointly.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Simple favorites persistence using a properties file.
 * Key: {@code <playerUuid>:<worldName>}
 * Value: comma-separated waypoint IDs
 */
public class FavoritesManager {
    private static final Path FILE = Paths.get("run", "mods", "Boffmedia_Waypoints", "favorites.properties");
    private static final Properties props = new Properties();

    static {
        load();
    }

    private static synchronized void load() {
        if (Files.exists(FILE)) {
            try (InputStream in = Files.newInputStream(FILE)) {
                props.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static synchronized void save() {
        try {
            if (FILE.getParent() != null) {
                Files.createDirectories(FILE.getParent());
            }
            try (OutputStream out = Files.newOutputStream(FILE)) {
                props.store(out, "Waypoints favorites: key=<uuid>:<world> -> comma separated ids");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String key(String playerUuid, String world) {
        return playerUuid + ":" + (world != null ? world : "");
    }

    public static synchronized Set<String> getFavorites(String playerUuid, String world) {
        String value = props.getProperty(key(playerUuid, world), "");
        if (value == null || value.isEmpty()) return Collections.emptySet();
        String[] parts = value.split(",");
        Set<String> out = new HashSet<>();
        Arrays.stream(parts).map(String::trim).filter(s -> !s.isEmpty()).forEach(out::add);
        return out;
    }

    public static synchronized boolean isFavorite(String playerUuid, String world, String waypointId) {
        return getFavorites(playerUuid, world).contains(waypointId);
    }

    public static synchronized void toggleFavorite(String playerUuid, String world, String waypointId) {
        Set<String> favs = new HashSet<>(getFavorites(playerUuid, world));
        if (favs.contains(waypointId)) favs.remove(waypointId);
        else favs.add(waypointId);
        props.setProperty(key(playerUuid, world), String.join(",", favs));
        save();
    }
}
