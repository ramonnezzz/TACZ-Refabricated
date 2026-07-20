package com.tacz.guns.network;

import cn.sh1rocu.tacz.api.extension.IEntityAdditionalSpawnData;
import com.tacz.guns.network.message.*;
import com.tacz.guns.network.message.event.*;
import com.tacz.guns.network.message.handshake.AcknowledgeC2SPacket;
import com.tacz.guns.network.message.handshake.SyncedEntityDataMappingS2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.mixin.networking.client.accessor.ClientHandshakePacketListenerImplAccessor;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class NetworkHandler {
    // Comum a ambos os lados: registra os codecs de todos os payloads (C2S e S2C) antes de
    // qualquer envio/recebimento acontecer, já que quem envia precisa do codec pra codificar
    // e quem recebe precisa dele pra decodificar, independente do lado em que rodamos.
    public static void registerC2SPackets() {
        PayloadTypeRegistry.serverboundPlay().register(ClientMessagePlayerShoot.TYPE, ClientMessagePlayerShoot.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClientMessagePlayerReloadGun.TYPE, ClientMessagePlayerReloadGun.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClientMessagePlayerCancelReload.TYPE, ClientMessagePlayerCancelReload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClientMessagePlayerFireSelect.TYPE, ClientMessagePlayerFireSelect.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClientMessagePlayerAim.TYPE, ClientMessagePlayerAim.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClientMessagePlayerCrawl.TYPE, ClientMessagePlayerCrawl.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClientMessagePlayerDrawGun.TYPE, ClientMessagePlayerDrawGun.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClientMessageCraft.TYPE, ClientMessageCraft.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClientMessagePlayerZoom.TYPE, ClientMessagePlayerZoom.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClientMessageRefitGun.TYPE, ClientMessageRefitGun.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClientMessageUnloadAttachment.TYPE, ClientMessageUnloadAttachment.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClientMessagePlayerBoltGun.TYPE, ClientMessagePlayerBoltGun.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClientMessagePlayerMelee.TYPE, ClientMessagePlayerMelee.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClientMessageSyncBaseTimestamp.TYPE, ClientMessageSyncBaseTimestamp.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ClientMessageLaserColor.TYPE, ClientMessageLaserColor.STREAM_CODEC);

        PayloadTypeRegistry.clientboundPlay().register(ServerMessageSound.TYPE, ServerMessageSound.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageCraft.TYPE, ServerMessageCraft.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageRefreshRefitScreen.TYPE, ServerMessageRefreshRefitScreen.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageSwapItem.TYPE, ServerMessageSwapItem.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageLevelUp.TYPE, ServerMessageLevelUp.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageGunHurt.TYPE, ServerMessageGunHurt.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageGunKill.TYPE, ServerMessageGunKill.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageUpdateEntityData.TYPE, ServerMessageUpdateEntityData.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageSyncGunPack.TYPE, ServerMessageSyncGunPack.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageSyncGunSmithTableRecipes.TYPE, ServerMessageSyncGunSmithTableRecipes.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageGunDraw.TYPE, ServerMessageGunDraw.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageGunFire.TYPE, ServerMessageGunFire.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageGunFireSelect.TYPE, ServerMessageGunFireSelect.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageGunMelee.TYPE, ServerMessageGunMelee.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageGunReload.TYPE, ServerMessageGunReload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageGunShoot.TYPE, ServerMessageGunShoot.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerMessageSyncBaseTimestamp.TYPE, ServerMessageSyncBaseTimestamp.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(IEntityAdditionalSpawnData.ExtraSpawnDataPayload.TYPE, IEntityAdditionalSpawnData.ExtraSpawnDataPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ClientMessagePlayerShoot.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ServerPlayNetworking.registerGlobalReceiver(ClientMessagePlayerReloadGun.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ServerPlayNetworking.registerGlobalReceiver(ClientMessagePlayerCancelReload.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ServerPlayNetworking.registerGlobalReceiver(ClientMessagePlayerFireSelect.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ServerPlayNetworking.registerGlobalReceiver(ClientMessagePlayerAim.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ServerPlayNetworking.registerGlobalReceiver(ClientMessagePlayerCrawl.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ServerPlayNetworking.registerGlobalReceiver(ClientMessagePlayerDrawGun.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ServerPlayNetworking.registerGlobalReceiver(ClientMessageCraft.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ServerPlayNetworking.registerGlobalReceiver(ClientMessagePlayerZoom.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ServerPlayNetworking.registerGlobalReceiver(ClientMessageRefitGun.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ServerPlayNetworking.registerGlobalReceiver(ClientMessageUnloadAttachment.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ServerPlayNetworking.registerGlobalReceiver(ClientMessagePlayerBoltGun.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ServerPlayNetworking.registerGlobalReceiver(ClientMessagePlayerMelee.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ServerPlayNetworking.registerGlobalReceiver(ClientMessageSyncBaseTimestamp.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ServerPlayNetworking.registerGlobalReceiver(ClientMessageLaserColor.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));

        HandshakeNetworking.register(AcknowledgeC2SPacket.ID, AcknowledgeC2SPacket.class);
        HandshakeNetworking.register(SyncedEntityDataMappingS2CPacket.TYPE, SyncedEntityDataMappingS2CPacket.STREAM_CODEC, SyncedEntityDataMappingS2CPacket.class);
    }

    @Environment(EnvType.CLIENT)
    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(IEntityAdditionalSpawnData.ExtraSpawnDataPayload.TYPE, (payload, context) -> context.client().execute(() -> {
            Entity entity = Objects.requireNonNull(context.client().level).getEntity(payload.entityId());
            if (entity instanceof IEntityAdditionalSpawnData extra) {
                extra.readSpawnData(new FriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(payload.data())));
            }
        }));

        ClientPlayNetworking.registerGlobalReceiver(ServerMessageSound.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageCraft.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageRefreshRefitScreen.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageSwapItem.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageLevelUp.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunHurt.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunKill.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageUpdateEntityData.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageSyncGunPack.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageSyncGunSmithTableRecipes.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunDraw.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunFire.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunFireSelect.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunMelee.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunReload.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageGunShoot.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
        ClientPlayNetworking.registerGlobalReceiver(ServerMessageSyncBaseTimestamp.TYPE, (payload, context) -> payload.handle(context.player(), context.responseSender()));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Environment(EnvType.CLIENT)
    static <T extends IHandshakeMessage> void registerHandshake(CustomPacketPayload.Type<T> type, StreamCodec<FriendlyByteBuf, T> codec) {
        ClientLoginNetworking.registerGlobalReceiver(type.id(), (client, handler, buf, listenerAdder) -> {
            T packet = codec.decode(buf);
            Connection connection = ((ClientHandshakePacketListenerImplAccessor) handler).getConnection();
            IHandshakeMessage.IResponsePacket responsePacket = packet.handle(connection, listenerAdder);
            FriendlyByteBuf response = FriendlyByteBufs.create();
            if (responsePacket != null) {
                response.writeIdentifier(responsePacket.getId());
                responsePacket.write(response);
            }
            return CompletableFuture.completedFuture(response);
        });
    }

    public static void sendToClientPlayer(CustomPacketPayload message, ServerPlayer player) {
        ServerPlayNetworking.send(player, message);
    }

    /**
     * 发送给所有监听此实体的玩家
     */
    public static void sendToTrackingEntityAndSelf(Entity centerEntity, CustomPacketPayload message) {
        if (centerEntity instanceof ServerPlayer player) {
            sendToClientPlayer(message, player);
        }
        sendToTrackingEntity(message, centerEntity);
    }

    public static void sendToAllPlayers(CustomPacketPayload message, MinecraftServer server) {
        for (ServerPlayer player : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(player, message);
        }
    }

    public static void sendToTrackingEntity(CustomPacketPayload message, final Entity centerEntity) {
        for (ServerPlayer player : PlayerLookup.tracking(centerEntity)) {
            ServerPlayNetworking.send(player, message);
        }
    }

    public static void sendToDimension(CustomPacketPayload message, final Entity centerEntity) {
        if (centerEntity.level() instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : PlayerLookup.level(serverLevel)) {
                ServerPlayNetworking.send(player, message);
            }
        }
    }
}
