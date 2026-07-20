package com.tacz.guns.mixin.client;

import cn.sh1rocu.tacz.api.mixin.EntityRenderStateEntity;
import com.tacz.guns.client.animation.third.InnerThirdPersonManager;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// setupAnim(LivingEntity,F,F,F,F,F) virou setupAnim(T extends HumanoidRenderState) - os 5
// floats agora são campos do render state (ageInTicks herdado, walkAnimationPos/
// walkAnimationSpeed = limbSwing/limbSwingAmount, e netHeadYaw/headPitch viraram os próprios
// state.yRot/state.xRot - confirmado direto no bytecode de setupAnim: head.xRot = state.xRot *
// DEG_TO_RAD, head.yRot = state.yRot * DEG_TO_RAD). A entidade em si não vem mais no render
// state - pega ela via EntityRenderStateEntity (mesmo mixin que EntityBulletRenderer/
// StatueRenderer já usam pra isso)
@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends HumanoidRenderState> {
    @Shadow
    @Final
    public ModelPart head;
    @Shadow
    @Final
    public ModelPart body;
    @Shadow
    @Final
    public ModelPart leftArm;
    @Shadow
    @Final
    public ModelPart rightArm;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At(value = "TAIL"))
    private void setRotationAnglesHead(T renderState, CallbackInfo ci) {
        if (renderState.ageInTicks == 0) {
            return;
        }
        if (!(((EntityRenderStateEntity) renderState).tacz$getEntity() instanceof LivingEntity entityIn)) {
            return;
        }
        InnerThirdPersonManager.setRotationAnglesHead(entityIn, rightArm, leftArm, body, head, renderState.walkAnimationSpeed);
    }
}
