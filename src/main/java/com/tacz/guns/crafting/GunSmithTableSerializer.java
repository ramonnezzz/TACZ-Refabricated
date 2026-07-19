package com.tacz.guns.crafting;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.tacz.guns.crafting.result.GunSmithTableResult;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.pojo.data.recipe.TableRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作台配方序列化器。
 * <p>
 * RecipeSerializer deixou de ser uma interface (agora é um record que só empacota um
 * MapCodec + StreamCodec), então esta classe não implementa mais nada — só expõe os dois
 * codecs que o ModRecipe usa pra construir o RecipeSerializer de verdade.
 */
public class GunSmithTableSerializer {
    public static final MapCodec<GunSmithTableRecipe> CODEC = MapCodec.assumeMapUnsafe(
            Codec.PASSTHROUGH.comapFlatMap(GunSmithTableSerializer::fromDynamic, GunSmithTableSerializer::toDynamic)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GunSmithTableRecipe> STREAM_CODEC = StreamCodec.of(
            GunSmithTableSerializer::toNetwork, GunSmithTableSerializer::fromNetwork
    );

    private static DataResult<GunSmithTableRecipe> fromDynamic(Dynamic<?> dynamic) {
        Object jsonValue = dynamic.convert(JsonOps.INSTANCE).getValue();
        if (!(jsonValue instanceof JsonObject jsonObject)) {
            return DataResult.error(() -> "Not a JSON object");
        }
        TableRecipe tableRecipe = CommonAssetsManager.GSON.fromJson(jsonObject, TableRecipe.class);
        if (tableRecipe == null) {
            return DataResult.error(() -> "Failed to parse gun smith table recipe");
        }
        // O id de verdade só existe no RecipeHolder que o RecipeManager cria em volta disso;
        // GunSmithTableRecipe#withId corrige esse placeholder assim que o recipe é lido de volta.
        return DataResult.success(new GunSmithTableRecipe(Identifier.fromNamespaceAndPath("tacz", "unknown"), tableRecipe));
    }

    private static Dynamic<?> toDynamic(GunSmithTableRecipe recipe) {
        throw new UnsupportedOperationException("GunSmithTableRecipe nao suporta serializar de volta pra JSON");
    }

    private static GunSmithTableRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<GunSmithTableIngredient> ingredients = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ingredients.add(new GunSmithTableIngredient(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), buffer.readInt()));
        }
        ItemStack resultItem = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        Identifier group = buffer.readIdentifier();
        GunSmithTableResult result = new GunSmithTableResult(resultItem, group);
        return new GunSmithTableRecipe(Identifier.fromNamespaceAndPath("tacz", "unknown"), result, ingredients);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buffer, GunSmithTableRecipe recipe) {
        buffer.writeInt(recipe.getInputs().size());
        for (GunSmithTableIngredient ingredient : recipe.getInputs()) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient.getIngredient());
            buffer.writeInt(ingredient.getCount());
        }
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.getResult().getResult());
        buffer.writeIdentifier(recipe.getResult().getGroup());
    }
}
