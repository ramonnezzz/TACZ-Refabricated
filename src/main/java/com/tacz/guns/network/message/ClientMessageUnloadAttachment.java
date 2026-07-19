package com.tacz.guns.network.message;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ClientMessageUnloadAttachment implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientMessageUnloadAttachment> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "c2s_unload_attachment"));
    public static final StreamCodec<FriendlyByteBuf, ClientMessageUnloadAttachment> STREAM_CODEC = CustomPacketPayload.codec(ClientMessageUnloadAttachment::write, ClientMessageUnloadAttachment::new);

    private final int gunSlotIndex;
    private final AttachmentType attachmentType;

    public ClientMessageUnloadAttachment(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readEnum(AttachmentType.class));
    }

    public ClientMessageUnloadAttachment(int gunSlotIndex, AttachmentType attachmentType) {
        this.gunSlotIndex = gunSlotIndex;
        this.attachmentType = attachmentType;
    }

        public void write(FriendlyByteBuf buf) {
        buf.writeInt(gunSlotIndex);
        buf.writeEnum(attachmentType);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player, PacketSender responseSender) {
        Inventory inventory = player.getInventory();
        ItemStack gunItem = inventory.getItem(gunSlotIndex);
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun != null) {
            // 服务端校验配件锁
            if (iGun.hasAttachmentLock(gunItem)) {
                return;
            }
            ItemStack attachmentItem = iGun.getAttachment(gunItem, attachmentType);
            if (!attachmentItem.isEmpty() && inventory.add(attachmentItem)) {
                iGun.unloadAttachment(gunItem, attachmentType);
                // 刷新配件数据
                AttachmentPropertyManager.postChangeEvent(player, gunItem);
                // 如果卸载的是扩容弹匣，吐出所有子弹
                if (attachmentType == AttachmentType.EXTENDED_MAG) {
                    iGun.dropAllAmmo(player, gunItem);
                }
                player.inventoryMenu.broadcastChanges();
                NetworkHandler.sendToClientPlayer(new ServerMessageRefreshRefitScreen(), player);
            }
        }
    }
}
