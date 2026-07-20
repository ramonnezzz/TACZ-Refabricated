package com.tacz.guns.network.message;

import com.tacz.guns.GunMod;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.crafting.GunSmithTableSerializer;
import com.tacz.guns.init.ModRecipe;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * RecipeAccess do cliente na 26.2 só expõe RecipePropertySet (pra recipe book), não mais
 * Recipe/RecipeHolder completos por id - GunSmithTableScreen não tem mais como enumerar as
 * próprias receitas via Level#getRecipeManager(). Sincroniza a lista completa por conta própria,
 * reaproveitando o StreamCodec que já existe pra rede (GunSmithTableSerializer), no mesmo
 * gatilho (SYNC_DATA_PACK_CONTENTS) que o resto do sync desse mod usa (ver CommonAssetsManager).
 */
public class ServerMessageSyncGunSmithTableRecipes implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerMessageSyncGunSmithTableRecipes> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "s2c_sync_gun_smith_table_recipes"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerMessageSyncGunSmithTableRecipes> STREAM_CODEC = StreamCodec.of(
            ServerMessageSyncGunSmithTableRecipes::write, ServerMessageSyncGunSmithTableRecipes::new
    );

    private final Map<Identifier, GunSmithTableRecipe> recipes;

    public ServerMessageSyncGunSmithTableRecipes(Map<Identifier, GunSmithTableRecipe> recipes) {
        this.recipes = recipes;
    }

    private ServerMessageSyncGunSmithTableRecipes(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        Map<Identifier, GunSmithTableRecipe> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Identifier id = buffer.readIdentifier();
            GunSmithTableRecipe recipe = GunSmithTableSerializer.STREAM_CODEC.decode(buffer).withId(id);
            map.put(id, recipe);
        }
        this.recipes = map;
    }

    private static void write(RegistryFriendlyByteBuf buffer, ServerMessageSyncGunSmithTableRecipes message) {
        buffer.writeVarInt(message.recipes.size());
        for (Map.Entry<Identifier, GunSmithTableRecipe> entry : message.recipes.entrySet()) {
            buffer.writeIdentifier(entry.getKey());
            GunSmithTableSerializer.STREAM_CODEC.encode(buffer, entry.getValue());
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    public void handle(LocalPlayer player, PacketSender responseSender) {
        ModRecipe.setClientGunSmithTableRecipes(recipes);
    }
}
