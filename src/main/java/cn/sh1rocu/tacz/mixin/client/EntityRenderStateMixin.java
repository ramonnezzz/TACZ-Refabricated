package cn.sh1rocu.tacz.mixin.client;

import cn.sh1rocu.tacz.api.mixin.EntityRenderStateEntity;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

// EntityRenderState não guarda mais referência pra Entity de origem (extractRenderState/submit
// trabalham só com o snapshot) - esse mixin guarda a entidade pra quem precisar dela em submit(),
// como ItemInHandLayerMixin (ver EntityRenderStateExtractMixin)
@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityRenderStateEntity {
    @Unique
    private Entity tacz$entity;

    @Override
    public Entity tacz$getEntity() {
        return tacz$entity;
    }

    @Override
    public void tacz$setEntity(Entity entity) {
        this.tacz$entity = entity;
    }
}
