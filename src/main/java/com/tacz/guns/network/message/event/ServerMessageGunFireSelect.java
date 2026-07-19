package com.tacz.guns.network.message.event;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.event.common.GunFireSelectEvent;
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

public class ServerMessageGunFireSelect implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerMessageGunFireSelect> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "s2c_gunfire_select"));
    public static final StreamCodec<FriendlyByteBuf, ServerMessageGunFireSelect> STREAM_CODEC = CustomPacketPayload.codec(ServerMessageGunFireSelect::write, ServerMessageGunFireSelect::new);

    private final int shooterId;
    private final ItemStack gunItemStack;

    public ServerMessageGunFireSelect(FriendlyByteBuf buf) {
        this(buf.readVarInt(), ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf));
    }

    public ServerMessageGunFireSelect(int shooterId, ItemStack gunItemStack) {
        this.shooterId = shooterId;
        this.gunItemStack = gunItemStack;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(shooterId);
        ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, gunItemStack);
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
    private static void doClientEvent(ServerMessageGunFireSelect message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (level.getEntity(message.shooterId) instanceof LivingEntity shooter) {
            GunFireSelectEvent gunFireSelectEvent = new GunFireSelectEvent(shooter, message.gunItemStack, LogicalSide.CLIENT);
            GunFireSelectEvent.CALLBACK.invoker().post(gunFireSelectEvent);
        }
    }
}
