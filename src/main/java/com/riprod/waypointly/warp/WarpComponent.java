package com.riprod.waypointly.warp;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WarpComponent implements Component<EntityStore> {

    private static ComponentType<EntityStore, WarpComponent> componentType;

    public static ComponentType<EntityStore, WarpComponent> getComponentType() {
        return componentType;
    }

    public static void setComponentType(@Nonnull ComponentType<EntityStore, WarpComponent> componentType) {
        WarpComponent.componentType = componentType;
    }

    private float cooldownRemaining;
    private float warmupRemaining;
    private float targetX;
    private float targetZ;
    @Nullable
    private String targetName;

    @Nonnull
    @Override
    public Component<EntityStore> clone() {
        var clone = new WarpComponent();
        clone.cooldownRemaining = cooldownRemaining;
        clone.warmupRemaining = warmupRemaining;
        clone.targetX = targetX;
        clone.targetZ = targetZ;
        clone.targetName = targetName;
        return clone;
    }

    public boolean isOnCooldown() {
        return cooldownRemaining > 0;
    }

    public float getCooldownRemaining() {
        return cooldownRemaining;
    }

    public void startCooldown(float seconds) {
        this.cooldownRemaining = seconds;
    }

    public boolean isWarmingUp() {
        return warmupRemaining > 0;
    }

    public void startWarmup(float seconds, float x, float z, @Nullable String name) {
        this.warmupRemaining = seconds;
        this.targetX = x;
        this.targetZ = z;
        this.targetName = name;
    }

    public boolean tickWarmupComplete(float dt) {
        if (cooldownRemaining > 0) {
            cooldownRemaining -= dt;
        }
        if (warmupRemaining <= 0) {
            return false;
        }

        warmupRemaining -= dt;
        return warmupRemaining <= 0;
    }

    public boolean isExpired() {
        return cooldownRemaining <= 0 && warmupRemaining <= 0;
    }

    public float getTargetX() {
        return targetX;
    }

    public float getTargetZ() {
        return targetZ;
    }

    @Nullable
    public String getTargetName() {
        return targetName;
    }
}
