package com.riprod.waypointly;
import java.util.List;

public class Icons {

    public static class Icon {
        private final String displayName;
        private final String fileName;

        public Icon(String displayName, String fileName) {
            this.displayName = displayName;
            this.fileName = fileName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getFileName() {
            return fileName;
        }
    }

    private static final List<Icon> DEFAULT_ICONS = List.of(new Icon[] {
        new Icon("Campfire", "Campfire.png"),
        new Icon("Coordinate", "Coordinate.png"),
        new Icon("Death", "Death.png"),
        new Icon("Home", "Home.png"),
        new Icon("Player", "Player.png"),
        new Icon("Portal", "Portal.png"),
        new Icon("Portal Invasion", "PortalInvasion.png"),
        new Icon("Prefab", "Prefab.png"),
        new Icon("Spawn", "Spawn.png"),
        new Icon("Temple Gateway", "Temple_Gateway.png"),
        new Icon("Warp", "Warp.png")
    });
    
    public static List<Icon> getDefaultIcons() {
        return DEFAULT_ICONS;
    }
}
