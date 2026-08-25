# Waypointly
*Waypointly is a fork of [Waypoint Manager](https://www.curseforge.com/hytale/mods/waypoint-manager) updated for the latest version of hytale*

A waypoint management system for Hytale with an in-game UI.

![Compass](https://i.imgur.com/4CCCdaT.png) ![Waypoints main view](https://i.imgur.com/lqG4f1F.jpeg)

## Features

*   **UI-based waypoint manager** - Create, edit, and remove waypoints through a graphical interface
*   **Warping** - Warp to a waypoint from the UI, with optional cooldown and warmup
*   **Shared waypoints** - Publish a waypoint to the whole world so every player sees it
*   **Per-waypoint tint** - Recolor any icon with a hex tint
*   **Configurable icon set** - Every icon comes from config, so packs can add their own
*   **Distance display** - Horizontal distance in blocks from your position to each waypoint
*   **Sorting and search** - Order by distance or name, filter by name
*   **Map markers** - Waypoints appear on your in-game map

## Usage

Use `/waypoint` or `/wp` to open the waypoint manager.

Each waypoint row shows three buttons:

*   **WARP** (green) - Warp to the waypoint
*   **EDIT** (blue) - Change its name, position, icon or tint
*   **REMOVE** (red) - Delete it

![Waypoint edit view](https://i.imgur.com/zLlJpmr.jpeg)

### Vertical position

Hytale stores map markers with an X and a Z only, so a waypoint has no saved Y. Warping resolves the
landing height from the terrain at the target column, the same way the vanilla map's own marker teleport
does. A waypoint over a cave or an upper floor lands on the surface above it.

## Commands

| Command | Description |
| --- | --- |
| `/waypoint`, `/wp` | Open the waypoint UI |
| `/waypoint add <name>` | Add a waypoint at your position |
| `/waypoint remove <name>` | Remove a waypoint (tab-completes your waypoint names) |
| `/warp <name>` | Warp to a waypoint (tab-completes your waypoint names) |
| `/waypoint perms grant <player> <ui\|teleport\|shared>` | Grant a permission to an online player |
| `/waypoint perms revoke <player> <ui\|teleport\|shared>` | Revoke a permission from an online player |
| `/waypoint perms list <player>` | Show which Waypointly permissions a player has |
| `/listmarkers` | List your waypoints in chat |
| `/resetmarkers` | Delete all of your personal waypoints in this world |

## Permissions

| Node | Default | Grants |
| --- | --- | --- |
| `riprod.waypoints.command.waypoint` | everyone | Access to the `/waypoint` UI |
| `riprod.waypoints.command.teleport` | WorldEditor and above | The WARP button and `/warp` |
| `riprod.waypoints.command.shared` | Builder and above | Publishing shared waypoints |
| `riprod.waypoints.command.admin` | ServerEditor and above | Managing permissions, editing anyone's shared waypoints |

### Granting permissions

The engine's own `/perm user add` takes a raw UUID. Waypointly's `perms` subcommand takes a **player name**
with tab-completion instead, so you never have to look a UUID up:

```
/waypoint perms grant Steve teleport
/waypoint perms revoke Steve ui
/waypoint perms list Steve
```

The player has to be online for their name to resolve. For offline players, or to change a whole group, use
the engine's `/perm` commands.

On singleplayer, `/op self` grants everything.

To hand the WARP button to **everyone** regardless of permissions, set `AllowTeleportForEveryone` to `true`
in the config rather than granting the node to each player.

> The vanilla map screen has its own "teleport to marker" button. That one is engine-controlled and stays
> Creative-only no matter what you configure here. Waypointly's WARP button works in Adventure too.

## Configuration

Waypointly uses [Configly](https://maven.hytalemodding.dev), which is bundled inside the jar - there is
nothing extra to install. The config lives at `Server/Configs/Waypointly.json`.

### Editing the config

Open the **asset editor**, find `Server/Configs/Waypointly.json`, change what you want, then hit
**Override asset** and save it into a custom pack. That writes your edited copy into your own pack so it
survives mod updates. Every field is a typed control with inline documentation, because Configly registers
the config's codec with the editor rather than handing it a raw JSON blob.

### Options

| Key | Default | Meaning |
| --- | --- | --- |
| `MaxWaypoints` | `-1` | Personal waypoints per player per world. `-1` is unlimited. |
| `MaxSharedWaypoints` | `-1` | Shared waypoints one player may contribute per world. `-1` is unlimited. |
| `MaxNameLength` | `24` | Longest accepted waypoint name. |
| `AllowTeleportForEveryone` | `false` | Give every player the WARP button regardless of permissions. |
| `AllowSharedWaypoints` | `true` | Whether shared waypoints can be created at all. |
| `TeleportCooldownSeconds` | `0` | Seconds between warps. `0` disables it. |
| `TeleportWarmupSeconds` | `0` | Delay between requesting a warp and being moved. `0` warps instantly. |
| `DefaultIcon` | `Coordinate.png` | Icon preselected for a new waypoint. |
| `IconTexturePath` | `Markers/` | Prefix joined to each icon's `Image` to locate its texture. |
| `Icons` | 11 built-ins | Every icon offered in the picker. |

### Custom icons

`Icons` is a plain list of `Name` (the picker label) and `Image` (the file). Add an entry and drop the
matching `.png` into the folder `IconTexturePath` points at:

```json
"Icons": [
  { "Name": "Coordinate", "Image": "Coordinate.png" },
  { "Name": "My Guild", "Image": "GuildBanner.png" }
]
```

`IconTexturePath` is rooted at `Common/UI/Custom`, so `Markers/` means `Common/UI/Custom/Markers`.
Point it somewhere else to serve icons entirely from your own pack. The same
`Image` value is sent to the client as the map marker image, so one entry covers both the picker swatch and
the marker on the map.

## TODO

*   Translations

_Questions or suggestions? Feel free to drop a comment below!_
