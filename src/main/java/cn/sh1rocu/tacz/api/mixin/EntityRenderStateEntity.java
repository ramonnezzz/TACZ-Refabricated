package cn.sh1rocu.tacz.api.mixin;

import net.minecraft.world.entity.Entity;

public interface EntityRenderStateEntity {
    Entity tacz$getEntity();

    void tacz$setEntity(Entity entity);
}
