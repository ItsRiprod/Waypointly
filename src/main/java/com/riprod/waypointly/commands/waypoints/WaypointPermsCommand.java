package com.riprod.waypointly.commands.waypoints;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.waypointly.Constants;
import com.riprod.waypointly.commands.args.WaypointPermissionArgType;
import com.riprod.waypointly.util.PermissionsUtil;

import javax.annotation.Nonnull;

public class WaypointPermsCommand extends AbstractPlayerCommand {

    public WaypointPermsCommand() {
        super("perms", "Grant or revoke Waypointly permissions for a player");
        addSubCommand(new Grant());
        addSubCommand(new Revoke());
        addSubCommand(new List());
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        playerRef.sendMessage(Message.raw("Usage: /waypoint perms <grant|revoke|list> <player> [ui|teleport|shared]"));
    }

    private static boolean denyUnprivileged(@Nonnull PlayerRef playerRef) {
        if (PermissionsUtil.canAdminister(playerRef)) {
            return false;
        }
        playerRef.sendMessage(Message.raw("You do not have permission to manage Waypointly permissions."));
        return true;
    }

    private static class Grant extends AbstractPlayerCommand {
        private final RequiredArg<PlayerRef> targetArg;
        private final RequiredArg<String> nodeArg;

        Grant() {
            super("grant", "Grant a Waypointly permission to a player");
            this.targetArg = withRequiredArg("player", "The player to grant the permission to", ArgTypes.PLAYER_REF);
            this.nodeArg = withRequiredArg("permission", "One of ui, teleport or shared", WaypointPermissionArgType.WAYPOINT_PERMISSION);
        }

        @Override
        protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            if (denyUnprivileged(playerRef)) return;

            var target = context.get(this.targetArg);
            var node = context.get(this.nodeArg);
            if (target == null || node == null) return;

            PermissionsUtil.grant(target.getUuid(), node);
            playerRef.sendMessage(Message.raw("Granted " + node + " to " + target.getUsername() + "."));
            target.sendMessage(Message.raw("You were granted the Waypointly permission " + node + "."));
        }
    }

    private static class Revoke extends AbstractPlayerCommand {
        private final RequiredArg<PlayerRef> targetArg;
        private final RequiredArg<String> nodeArg;

        Revoke() {
            super("revoke", "Revoke a Waypointly permission from a player");
            this.targetArg = withRequiredArg("player", "The player to revoke the permission from", ArgTypes.PLAYER_REF);
            this.nodeArg = withRequiredArg("permission", "One of ui, teleport or shared", WaypointPermissionArgType.WAYPOINT_PERMISSION);
        }

        @Override
        protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            if (denyUnprivileged(playerRef)) return;

            var target = context.get(this.targetArg);
            var node = context.get(this.nodeArg);
            if (target == null || node == null) return;

            PermissionsUtil.revoke(target.getUuid(), node);
            playerRef.sendMessage(Message.raw("Revoked " + node + " from " + target.getUsername() + "."));
        }
    }

    private static class List extends AbstractPlayerCommand {
        private final RequiredArg<PlayerRef> targetArg;

        List() {
            super("list", "Show which Waypointly permissions a player has");
            this.targetArg = withRequiredArg("player", "The player to inspect", ArgTypes.PLAYER_REF);
        }

        @Override
        protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            if (denyUnprivileged(playerRef)) return;

            var target = context.get(this.targetArg);
            if (target == null) return;

            var permissions = PermissionsModule.get();
            var uuid = target.getUuid();

            playerRef.sendMessage(Message.raw("Waypointly permissions for " + target.getUsername() + ":"));
            playerRef.sendMessage(Message.raw("  ui: " + permissions.hasPermission(uuid, Constants.PERMISSION_WAYPOINT_OPEN, true)));
            playerRef.sendMessage(Message.raw("  teleport: " + PermissionsUtil.canTeleport(target)));
            playerRef.sendMessage(Message.raw("  shared: " + PermissionsUtil.canShare(target)));
        }
    }
}
