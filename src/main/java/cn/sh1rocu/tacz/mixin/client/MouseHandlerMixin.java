package cn.sh1rocu.tacz.mixin.client;

import cn.sh1rocu.tacz.api.event.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// onPress(long,int,int,int) virou onButton(long,MouseButtonInfo,int) na 26.2 (button+modifiers
// agrupados no record MouseButtonInfo). O ponto de injeção antigo mirava um INVOKE específico
// (Minecraft;getOverlay()) que sumiu (Overlay migrou pra dentro de Gui) - trocado por HEAD, que
// ainda preserva a semântica de "antes do processamento vanilla" pro evento Pre/cancelável.
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void tacz$onMouseButtonPre(long windowPointer, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        InputEvent.MouseButton.Pre event = new InputEvent.MouseButton.Pre(buttonInfo.button(), action, buttonInfo.modifiers());
        InputEvent.MouseButton.Pre.EVENT.invoker().onMousePre(event);

        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onButton", at = @At("TAIL"))
    private void tacz$onMouseButtonPost(long windowPointer, MouseButtonInfo buttonInfo, int action, CallbackInfo ci) {
        if (windowPointer == this.minecraft.getWindow().handle()) {
            InputEvent.MouseButton.Post event = new InputEvent.MouseButton.Post(buttonInfo.button(), action, buttonInfo.modifiers());
            InputEvent.MouseButton.Post.EVENT.invoker().onMousePost(event);
        }
    }
}
