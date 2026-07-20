package cn.sh1rocu.tacz.mixin.client;

import cn.sh1rocu.tacz.api.mixin.ChannelAccessHandleInjection;
import cn.sh1rocu.tacz.util.SoundConsumerStorage;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.audio.Channel;
import com.mojang.blaze3d.audio.Library;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {
    // From Kilt
    // play() agora retorna SoundEngine.PlayResult (era void) - precisa de CallbackInfoReturnable
    @Inject(method = "play", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
    private void tacz$prepareChannelInfo(SoundInstance soundInstance, CallbackInfoReturnable<SoundEngine.PlayResult> ci, @Local ChannelAccess.ChannelHandle channelHandle, @Local Sound sound) {
        var injection = ((ChannelAccessHandleInjection) channelHandle);

        if (sound.shouldStream())
            injection.tacz$setPool(Library.Pool.STREAMING);
        else
            injection.tacz$setPool(Library.Pool.STATIC);

        injection.tacz$setSoundInstance(soundInstance);
        injection.tacz$setSoundEngine((SoundEngine) (Object) this);
    }

    // From Kilt
    // method_19757/method_19758 (nomes intermediários do Yarn, sem refmap pra remapear) viraram
    // os corpos lambda sintéticos de play() - lambda$play$1 é o caso fonte/estático (SoundBuffer),
    // lambda$play$3 é o de streaming (AudioStream)
    @ModifyArg(method = "lambda$play$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V"))
    private static Consumer<Channel> tacz$storeSourceConsumer(Consumer<Channel> consumer) {
        SoundConsumerStorage.soundConsumerChannels.add(consumer);
        return consumer;
    }

    // 暂时用不到
    // From Kilt
/*    @ModifyArg(method = "lambda$play$3", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/ChannelAccess$ChannelHandle;execute(Ljava/util/function/Consumer;)V"))
    private static Consumer<Channel> tacz$storeStreamConsumer(Consumer<Channel> consumer) {
        SoundConsumerStorage.soundConsumerChannels.add(consumer);
        return consumer;
    }*/
}
