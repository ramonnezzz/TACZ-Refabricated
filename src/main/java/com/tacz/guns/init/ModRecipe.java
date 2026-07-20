package com.tacz.guns.init;

import cn.sh1rocu.tacz.util.forge.PartialNBTIngredient;
import cn.sh1rocu.tacz.util.forge.StrictNBTIngredient;
import com.tacz.guns.GunMod;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.crafting.GunSmithTableSerializer;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ServerMessageSyncGunSmithTableRecipes;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModRecipe {
    public static void init() {
        // Antes disparado por um mixin próprio que interceptava Ingredient#toJson/fromJson (ver
        // histórico de cn.sh1rocu.tacz.mixin.common.IngredientMixin, removido) - Ingredient virou
        // 100% Codec-driven na 26.2 e o próprio Fabric já tem um mixin equivalente, então basta
        // registrar os serializers do jeito nativo agora.
        CustomIngredientSerializer.register(StrictNBTIngredient.Serializer.INSTANCE);
        CustomIngredientSerializer.register(PartialNBTIngredient.Serializer.INSTANCE);
    }

    public static RecipeSerializer<GunSmithTableRecipe> GUN_SMITH_TABLE_RECIPE_SERIALIZER = registerSerializer("gun_smith_table_crafting", new RecipeSerializer<>(GunSmithTableSerializer.CODEC, GunSmithTableSerializer.STREAM_CODEC));
    // RecipeType.register(String) já registra internamente sob o namespace "minecraft" (via
    // Identifier.withDefaultNamespace) - chamar registerRecipe em cima do resultado tentava
    // registrar o MESMO objeto de novo sob "tacz", e registries não permitem reaproveitar a
    // mesma instância em duas chaves. Cria a instância direto (mesmo padrão anônimo que
    // RecipeType.register usa por baixo) e registra só uma vez, com o namespace certo.
    public static RecipeType<GunSmithTableRecipe> GUN_SMITH_TABLE_CRAFTING = registerRecipe("gun_smith_table_crafting", new RecipeType<>() {
        @Override
        public String toString() {
            return "gun_smith_table_crafting";
        }
    });

    /**
     * RecipeManager#getAllRecipesFor sumiu; o id de verdade de cada recipe só existe no
     * RecipeHolder que o RecipeManager cria, então reconstruímos cada GunSmithTableRecipe com
     * esse id (ver GunSmithTableRecipe#withId).
     */
    public static List<GunSmithTableRecipe> getAllGunSmithTableRecipes(RecipeManager recipeManager) {
        List<GunSmithTableRecipe> list = new ArrayList<>();
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            if (holder.value() instanceof GunSmithTableRecipe recipe) {
                list.add(recipe.withId(holder.id().identifier()));
            }
        }
        return list;
    }

    // RecipeAccess do cliente na 26.2 só expõe RecipePropertySet, não Recipe/RecipeHolder
    // completos por id - GunSmithTableScreen não tem mais como perguntar ao level pelas próprias
    // receitas. Sincronizado via ServerMessageSyncGunSmithTableRecipes (ver a classe).
    private static Map<Identifier, GunSmithTableRecipe> clientGunSmithTableRecipes = Map.of();

    public static void setClientGunSmithTableRecipes(Map<Identifier, GunSmithTableRecipe> recipes) {
        clientGunSmithTableRecipes = recipes;
    }

    public static List<GunSmithTableRecipe> getAllGunSmithTableRecipesClient() {
        return new ArrayList<>(clientGunSmithTableRecipes.values());
    }

    public static GunSmithTableRecipe getGunSmithTableRecipeClient(Identifier id) {
        return clientGunSmithTableRecipes.get(id);
    }

    public static void syncGunSmithTableRecipesToPlayer(ServerPlayer player, RecipeManager recipeManager) {
        Map<Identifier, GunSmithTableRecipe> map = new HashMap<>();
        for (GunSmithTableRecipe recipe : getAllGunSmithTableRecipes(recipeManager)) {
            map.put(recipe.getId(), recipe);
        }
        NetworkHandler.sendToClientPlayer(new ServerMessageSyncGunSmithTableRecipes(map), player);
    }

    private static <S extends RecipeSerializer<T>, T extends Recipe<?>> S registerSerializer(String name, S serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(GunMod.MOD_ID, name), serializer);
    }

    private static <T extends Recipe<?>> RecipeType<T> registerRecipe(String name, RecipeType<T> type) {
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(GunMod.MOD_ID, name), type);
    }
}
