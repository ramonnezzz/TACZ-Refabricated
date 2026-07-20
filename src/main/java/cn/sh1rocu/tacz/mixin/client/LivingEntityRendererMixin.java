package cn.sh1rocu.tacz.mixin.client;

import cn.sh1rocu.tacz.api.event.RenderLivingEvent;
import cn.sh1rocu.tacz.api.mixin.EntityRenderStateEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// render(entity,yaw,partialTicks,poseStack,buffer,light) virou submit(state,poseStack,collector,
// cameraRenderState) - a entidade em si some do state, então reusa o mesmo mixin
// (EntityRenderStateEntity/EntityRendererMixin) que já resgata a entidade original pro resto do
// pipeline de renderização
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("TAIL"))
    public void tacz$onPostEvent(LivingEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        Object entity = ((EntityRenderStateEntity) state).tacz$getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            var event = new RenderLivingEvent.Post(livingEntity, 1.0F, poseStack, collector, state.lightCoords);
            RenderLivingEvent.POST.invoker().post(event);
        }
    }
}
