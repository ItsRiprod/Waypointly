package com.riprod.waypointly.commands.args;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import com.riprod.waypointly.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public final class WaypointPermissionArgType extends SingleArgumentType<String> {

    public static final WaypointPermissionArgType WAYPOINT_PERMISSION = new WaypointPermissionArgType();

    private static final List<String> NAMES = List.of("ui", "teleport", "shared");

    private static final Map<String, String> NODES = Map.of(
            "ui", Constants.PERMISSION_WAYPOINT_OPEN,
            "waypoint", Constants.PERMISSION_WAYPOINT_OPEN,
            "teleport", Constants.PERMISSION_WAYPOINT_TELEPORT,
            "warp", Constants.PERMISSION_WAYPOINT_TELEPORT,
            "shared", Constants.PERMISSION_WAYPOINT_SHARED,
            "share", Constants.PERMISSION_WAYPOINT_SHARED
    );

    private WaypointPermissionArgType() {
        super("server.commands.parsing.argtype.string.name", "server.commands.parsing.argtype.string.usage",
                "ui", "teleport", "shared");
    }

    @Nullable
    @Override
    public String parse(@Nonnull String input, @Nonnull ParseResult parseResult) {
        var node = NODES.get(input.toLowerCase());
        if (node == null) {
            parseResult.fail(Message.raw("Unknown permission '" + input + "'. Use ui, teleport or shared."));
        }
        return node;
    }

    @Override
    public void suggest(@Nonnull CommandSender sender, @Nonnull String textAlreadyEntered, int numParametersTyped, @Nonnull SuggestionResult result) {
        var lowerInput = textAlreadyEntered.toLowerCase();
        for (var name : NAMES) {
            if (name.startsWith(lowerInput)) {
                result.suggest(name);
            }
        }
    }
}
