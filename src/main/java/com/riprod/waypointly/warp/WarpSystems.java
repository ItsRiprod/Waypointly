package com.riprod.waypointly.warp;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.waypointly.util.Waypoints;

import javax.annotation.Nonnull;

public class WarpSystems {

    public static class Tick extends EntityTickingSystem<EntityStore> {

        @Nonnull
        private final Query<EntityStore> query;

        public Tick() {
            this.query = Query.and(PlayerRef.getComponentType(), WarpComponent.getComponentType());
        }

        @Override
        public void tick(float dt, int index, @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer) {
            final var warp = archetypeChunk.getComponent(index, WarpComponent.getComponentType());
            assert warp != null;

            final var ref = archetypeChunk.getReferenceTo(index);

            if (warp.tickWarmupComplete(dt)) {
                final var playerRef = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
                assert playerRef != null;

                Waypoints.teleport(ref, store.getExternalData().getWorld(), warp.getTargetX(), warp.getTargetZ());
                playerRef.sendMessage(Message.raw("Warped to '" + warp.getTargetName() + "'!"));
            }

            if (warp.isExpired()) {
                commandBuffer.removeComponent(ref, WarpComponent.getComponentType());
            }
        }

        @Nonnull
        @Override
        public Query<EntityStore> getQuery() {
            return query;
        }
    }
}
