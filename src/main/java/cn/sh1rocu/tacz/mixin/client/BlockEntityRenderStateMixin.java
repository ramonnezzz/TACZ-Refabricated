package cn.sh1rocu.tacz.mixin.client;

import cn.sh1rocu.tacz.api.mixin.BlockEntityRenderStateEntity;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

// Mesmo truque de EntityRenderStateMixin: BlockEntityRenderState também não guarda mais
// referência pra BlockEntity de origem (extractRenderState/submit trabalham só com o snapshot)
@Mixin(BlockEntityRenderState.class)
public class BlockEntityRenderStateMixin implements BlockEntityRenderStateEntity {
    @Unique
    private BlockEntity tacz$blockEntity;

    @Override
    public BlockEntity tacz$getBlockEntity() {
        return tacz$blockEntity;
    }

    @Override
    public void tacz$setBlockEntity(BlockEntity blockEntity) {
        this.tacz$blockEntity = blockEntity;
    }
}
