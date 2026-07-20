package com.tacz.guns.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.client.event.RenderItemInHandBobEvent;
import com.tacz.guns.api.client.event.RenderLevelBobEvent;
import com.tacz.guns.client.renderer.other.GunHurtBobTweak;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Unique
    private boolean tacz$useFovSetting;

    @Shadow
    @Final
    private Minecraft minecraft;

    // bobHurt/bobView perderam o parâmetro float partialTicks (agora só CameraRenderState +
    // PoseStack) - usa o DeltaTracker direto, igual outros pontos já portados nessa base
    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    public void onBobHurt(CameraRenderState cameraRenderState, PoseStack pMatrixStack, CallbackInfo ci) {
        float pPartialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
        // 取消受伤导致的视角摇晃
        if (this.minecraft.getCameraEntity() instanceof LocalPlayer player && !player.isDeadOrDying()) {
            if (GunHurtBobTweak.onHurtBobTweak(player, pMatrixStack, pPartialTicks)) {
                ci.cancel();
                return;
            }
        }
        // 触发其他事件
        boolean cancel;
        if (!tacz$useFovSetting) {
            var event = new RenderItemInHandBobEvent.BobHurt();
            RenderItemInHandBobEvent.HURT.invoker().post(event);
            cancel = event.isCanceled();
        } else {
            var event = new RenderLevelBobEvent.BobHurt();
            RenderLevelBobEvent.HURT.invoker().post(event);
            cancel = event.isCanceled();
        }
        if (cancel) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void onBobView(CameraRenderState cameraRenderState, PoseStack pMatrixStack, CallbackInfo ci) {
        boolean cancel;
        if (!tacz$useFovSetting) {
            var event = new RenderItemInHandBobEvent.BobView();
            RenderItemInHandBobEvent.VIEW.invoker().post(event);
            cancel = event.isCanceled();
        } else {
            var event = new RenderLevelBobEvent.BobView();
            RenderLevelBobEvent.VIEW.invoker().post(event);
            cancel = event.isCanceled();
        }
        if (cancel) {
            ci.cancel();
        }
    }

    // getFov() sumiu (o hack antigo dependia dele pra distinguir contexto de render Level vs
    // HandWithItem). O ponto onde bobHurt/bobView são chamados AGORA já é diretamente
    // renderItemInHand() ou renderLevel() - marca o contexto direto neles, mais robusto que o
    // hack antigo baseado em getFov
    @Inject(method = "renderItemInHand", at = @At("HEAD"))
    public void tacz$markItemInHandRender(CameraRenderState cameraRenderState, float partialTick, org.joml.Matrix4fc matrix4fc, CallbackInfo ci) {
        this.tacz$useFovSetting = false;
    }

    @Inject(method = "renderLevel", at = @At("HEAD"))
    public void tacz$markLevelRender(DeltaTracker deltaTracker, CallbackInfo ci) {
        this.tacz$useFovSetting = true;
    }
}
