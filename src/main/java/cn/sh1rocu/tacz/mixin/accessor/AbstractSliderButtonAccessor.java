package cn.sh1rocu.tacz.mixin.accessor;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// SLIDER_LOCATION (textura única com offsets manuais) sumiu - o widget virou sprite-based
// (4 sprites: normal/highlighted pro corpo e pro handle), escolhidos por getSprite()/
// getHandleSprite() de acordo com o estado (foco/hover/canChangeValue)
@Mixin(AbstractSliderButton.class)
public interface AbstractSliderButtonAccessor {
    @Invoker("getSprite")
    Identifier tacz$getSprite();

    @Invoker("getHandleSprite")
    Identifier tacz$getHandleSprite();
}
