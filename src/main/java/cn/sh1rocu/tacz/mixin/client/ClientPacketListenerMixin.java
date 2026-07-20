package cn.sh1rocu.tacz.mixin.client;

import cn.sh1rocu.tacz.util.forge.ClientHooks;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// campo "minecraft" é herdado de ClientCommonPacketListenerImpl, não declarado direto em
// ClientPacketListener - @Shadow só olha a classe alvo, não a superclasse, então estende ela
// aqui pra acessar this.minecraft normalmente (mesmo padrão de ClientLevelMixin/PlayerMixin)
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {
    protected ClientPacketListenerMixin(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
    }

    // ClientLevel.addPlayer(int,AbstractClientPlayer) virou o addEntity(Entity) genérico
    @Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addEntity(Lnet/minecraft/world/entity/Entity;)V"))
    private void tacz$cloneEvent(ClientboundRespawnPacket packet, CallbackInfo ci, @Local(ordinal = 0) LocalPlayer oldPlayer, @Local(ordinal = 1) LocalPlayer newPlayer) {
        ClientHooks.firePlayerRespawn(this.minecraft.gameMode, oldPlayer, newPlayer, newPlayer.connection.getConnection());
    }
}
