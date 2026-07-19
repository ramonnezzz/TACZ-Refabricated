package com.tacz.guns.network.message.event;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.event.common.GunDrawEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ServerMessageGunDraw implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerMessageGunDraw> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "s2c_gundraw"));
    public static final StreamCodec<FriendlyByteBuf, ServerMessageGunDraw> STREAM_CODEC = CustomPacketPayload.codec(ServerMessageGunDraw::write, ServerMessageGunDraw::new);

    private final int entityId;
    private final ItemStack previousGunItem;
    private final ItemStack currentGunItem;

    public ServerMessageGunDraw(FriendlyByteBuf buf) {
        this(buf.readVarInt(), ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf), ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf));
    }

    public ServerMessageGunDraw(int entityId, ItemStack previousGunItem, ItemStack currentGunItem) {
        this.entityId = entityId;
        this.previousGunItem = previousGunItem;
        this.currentGunItem = currentGunItem;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, previousGunItem);
        ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, currentGunItem);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    public void handle(LocalPlayer player, PacketSender responseSender) {
        doClientEvent(this);
    }

    @Environment(EnvType.CLIENT)
    private static void doClientEvent(ServerMessageGunDraw message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (level.getEntity(message.entityId) instanceof LivingEntity livingEntity) {
            GunDrawEvent gunDrawEvent = new GunDrawEvent(livingEntity, message.previousGunItem, message.currentGunItem, LogicalSide.CLIENT);
            GunDrawEvent.CALLBACK.invoker().post(gunDrawEvent);
        }
    }
}
