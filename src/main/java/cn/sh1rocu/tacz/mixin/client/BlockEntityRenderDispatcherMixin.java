package cn.sh1rocu.tacz.mixin.client;

import cn.sh1rocu.tacz.api.mixin.BlockEntityRenderStateEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Inject(method = "tryExtractRenderState", at = @At("RETURN"))
    private <E extends BlockEntity, S extends BlockEntityRenderState> void tacz$stashBlockEntity(E blockEntity, float partialTick, ModelFeatureRenderer.CrumblingOverlay overlay, boolean bl, CallbackInfoReturnable<S> cir) {
        S state = cir.getReturnValue();
        if (state != null) {
            ((BlockEntityRenderStateEntity) state).tacz$setBlockEntity(blockEntity);
        }
    }
}
