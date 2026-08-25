# Changelog

## [2.1.0] Changes

- Feat: Fixed permission issue
- Feat: Enabled teleporting to waypoints from the map
- Feat: Embedded Configly for better configurations
- Feat: Updated for the reworked map marker API
- Feat: Manage permissions by player name with `/waypoint perms grant|revoke|list <player>`, no UUIDs to paste
- Feat: `AllowTeleportForEveryone` config option to hand every player the warp button without granting nodes
- Feat: Shared waypoints, published to the whole world so every player sees them
- Feat: Per-waypoint color tint
- Feat: Optional warp cooldown and warmup
- Feat: Icons are fully config-driven, add your own without a rebuild
- Feat: New options for max waypoints, max shared waypoints, max name length, default icon and icon folder
- Feat: `/warp` accepts a waypoint name or a marker id
- Fix: Waypoints no longer store a Y coordinate, warping resolves the landing height from the terrain
- Fix: `/warp`, `/listmarkers` and `/resetmarkers` were unusable without operator
