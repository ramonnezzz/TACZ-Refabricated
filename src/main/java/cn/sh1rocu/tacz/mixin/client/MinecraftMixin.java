package cn.sh1rocu.tacz.mixin.client;

import cn.sh1rocu.tacz.api.event.AddPackFindersEvent;
import cn.sh1rocu.tacz.api.event.InputEvent;
import cn.sh1rocu.tacz.api.mixin.PackRepositoryExtension;
import cn.sh1rocu.tacz.util.forge.ClientHooks;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    public abstract PackRepository getResourcePackRepository();

    @Shadow
    @Final
    public Options options;

    @Shadow
    @Final
    public ParticleEngine particleEngine;

    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;reload()V"))
    private void tacz$addPacks(GameConfig gameConfig, CallbackInfo ci) {
        AddPackFindersEvent event = new AddPackFindersEvent(PackType.CLIENT_RESOURCES, ((PackRepositoryExtension) this.getResourcePackRepository())::tacz$addPackFinder, false);
        AddPackFindersEvent.CALLBACK.invoker().onAddPackFinders(event);
    }

    @Inject(method = "clearClientLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetData()V"))
    private void tacz$disconnect(Screen screen, CallbackInfo ci) {
        ClientHooks.firePlayerLogout(this.gameMode, this.player);
    }

    @Unique
    private InputEvent.InteractionKeyMappingTriggered tacz$onClickInput(int button, KeyMapping keyMapping, InteractionHand hand) {
        InputEvent.InteractionKeyMappingTriggered event = new InputEvent.InteractionKeyMappingTriggered(button, keyMapping, hand);
        InputEvent.InteractionKeyMappingTriggered.EVENT.invoker().onInteractionKeyMappingTriggered(event);
        return event;
    }

    @Inject(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/BlockHitResult;getDirection()Lnet/minecraft/core/Direction;"), cancellable = true)
    private void tacz$onClickInputEvent(boolean leftClick, CallbackInfo ci, @Local BlockHitResult blockHitResult, @Local BlockPos blockPos, @Share("event") LocalRef<InputEvent.InteractionKeyMappingTriggered> eventRef) {
        InputEvent.InteractionKeyMappingTriggered inputEvent = tacz$onClickInput(0, this.options.keyAttack, InteractionHand.MAIN_HAND);
        eventRef.set(inputEvent);
        if (inputEvent.isCanceled()) {
            if (inputEvent.shouldSwingHand()) {
                // ParticleEngine.crack(BlockPos, Direction) sumiu na 26.2 sem substituto óbvio -
                // perde a partícula de "lascar bloco" nesse swing cancelado (puramente cosmético)
                this.player.swing(InteractionHand.MAIN_HAND);
            }
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
    private boolean tacz$checkHandSwing(boolean original, @Share("event") LocalRef<InputEvent.InteractionKeyMappingTriggered> eventRef) {
        return original && eventRef.get().shouldSwingHand();
    }

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"), cancellable = true)
    private void tacz$onAttackClickInputEvent(CallbackInfoReturnable<Boolean> cir, @Share("inputEvent") LocalRef<InputEvent.InteractionKeyMappingTriggered> inputEvent, @Local boolean flag) {
        inputEvent.set(tacz$onClickInput(0, this.options.keyAttack, InteractionHand.MAIN_HAND));

        if (inputEvent.get().isCanceled()) {
            if (inputEvent.get().shouldSwingHand())
                this.player.swing(InteractionHand.MAIN_HAND);

            cir.setReturnValue(flag);
        }
    }

    // startUseItem() não tem mais um shouldSwing() único: virou um loop por InteractionHand com
    // 3 pontos de swing() inline (interact/useItemOn/useItem), cada um checando
    // swingSource()==CLIENT direto. Em vez de tentar ancorar nisso, aplica o mesmo
    // WrapWithCondition de startAttack nos 3 swing() de startUseItem também - mesmo efeito líquido
    @WrapWithCondition(method = {"startAttack", "startUseItem"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private boolean tacz$swingHandIfEventPermits(LocalPlayer instance, InteractionHand interactionHand, @Share("inputEvent") LocalRef<InputEvent.InteractionKeyMappingTriggered> inputEvent) {
        return inputEvent.get() == null || inputEvent.get().shouldSwingHand();
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;", ordinal = 0), cancellable = true)
    private void tacz$callForgeUseInputEvent(CallbackInfo ci, @Share("inputEvent") LocalRef<InputEvent.InteractionKeyMappingTriggered> inputEvent, @Local InteractionHand hand) {
        inputEvent.set(tacz$onClickInput(1, this.options.keyUse, hand));

        if (inputEvent.get().isCanceled()) {
            if (inputEvent.get().shouldSwingHand())
                this.player.swing(hand);

            ci.cancel();
        }
    }

    // pickBlockOrEntity() não lê mais Abilities.instabuild diretamente (agora usa
    // hasControlDown() pro parâmetro que passa pra handlePickItemFromBlock/Entity) - injeta
    // no início do método em vez de ancorar num field-read que sumiu
    @Inject(method = "pickBlockOrEntity", at = @At("HEAD"), cancellable = true)
    private void tacz$callInteractionPickInput(CallbackInfo ci) {
        if (tacz$onClickInput(2, this.options.keyPickItem, InteractionHand.MAIN_HAND).isCanceled())
            ci.cancel();
    }
}
