package cn.sh1rocu.tacz.api.extension;

import com.tacz.guns.GunMod;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.List;

// Porting_Lib
public interface IEntityAdditionalSpawnData {
    void readSpawnData(FriendlyByteBuf buf);

    void writeSpawnData(FriendlyByteBuf buf);

    static Packet<ClientGamePacketListener> getEntitySpawningPacket(Entity entity) {
        return getEntitySpawningPacket(entity, new ClientboundAddEntityPacket(entity));
    }

    static Packet<ClientGamePacketListener> getEntitySpawningPacket(Entity entity, Packet<ClientGamePacketListener> base) {
        if (entity instanceof IEntityAdditionalSpawnData extra) {
            FriendlyByteBuf buf = FriendlyByteBufs.create();
            extra.writeSpawnData(buf);
            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);
            Packet<ClientCommonPacketListener> extraPacket = ServerPlayNetworking.createClientboundPacket(new ExtraSpawnDataPayload(entity.getId(), data));
            return new ClientboundBundlePacket(List.<Packet<? super ClientGamePacketListener>>of(base, extraPacket));
        }
        return base;
    }

    record ExtraSpawnDataPayload(int entityId, byte[] data) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ExtraSpawnDataPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "extra_entity_spawn_data"));
        public static final StreamCodec<FriendlyByteBuf, ExtraSpawnDataPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, ExtraSpawnDataPayload::entityId,
                ByteBufCodecs.BYTE_ARRAY, ExtraSpawnDataPayload::data,
                ExtraSpawnDataPayload::new
        );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
