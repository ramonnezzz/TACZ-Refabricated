package com.tacz.guns.init;

import com.tacz.guns.GunMod;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.crafting.GunSmithTableSerializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;

public class ModRecipe {
    public static void init() {

    }

    public static RecipeSerializer<GunSmithTableRecipe> GUN_SMITH_TABLE_RECIPE_SERIALIZER = registerSerializer("gun_smith_table_crafting", new RecipeSerializer<>(GunSmithTableSerializer.CODEC, GunSmithTableSerializer.STREAM_CODEC));
    public static RecipeType<GunSmithTableRecipe> GUN_SMITH_TABLE_CRAFTING = registerRecipe("gun_smith_table_crafting", RecipeType.register("gun_smith_table_crafting"));

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

    private static <S extends RecipeSerializer<T>, T extends Recipe<?>> S registerSerializer(String name, S serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Identifier.fromNamespaceAndPath(GunMod.MOD_ID, name), serializer);
    }

    private static <T extends Recipe<?>> RecipeType<T> registerRecipe(String name, RecipeType<T> type) {
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, Identifier.fromNamespaceAndPath(GunMod.MOD_ID, name), type);
    }
}
