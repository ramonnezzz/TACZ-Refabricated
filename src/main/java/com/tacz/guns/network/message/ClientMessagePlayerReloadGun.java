package com.tacz.guns.network.message;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.entity.IGunOperator;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class ClientMessagePlayerReloadGun implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientMessagePlayerReloadGun> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "c2s_player_reload"));
    public static final StreamCodec<FriendlyByteBuf, ClientMessagePlayerReloadGun> STREAM_CODEC = CustomPacketPayload.codec(ClientMessagePlayerReloadGun::write, ClientMessagePlayerReloadGun::new);

    public ClientMessagePlayerReloadGun() {

    }

    public ClientMessagePlayerReloadGun(FriendlyByteBuf buf) {
        this();
    }

    public void write(FriendlyByteBuf buf) {

    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player, PacketSender responseSender) {
        IGunOperator.fromLivingEntity(player).reload();
    }
}
