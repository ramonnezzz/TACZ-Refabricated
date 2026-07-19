package cn.sh1rocu.tacz.mixin.accessor;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractSliderButton.class)
public interface AbstractSliderButtonAccessor {
    @Accessor("SLIDER_LOCATION")
    static Identifier tacz$getSliderLocation() {
        throw new AssertionError();
    }

    @Invoker("getTextureY")
    int tacz$getTextureY();

    @Invoker("getHandleTextureY")
    int tacz$getHandleTextureY();
}
