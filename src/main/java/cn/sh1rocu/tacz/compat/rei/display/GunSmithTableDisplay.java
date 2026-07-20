package cn.sh1rocu.tacz.compat.rei.display;

import com.tacz.guns.crafting.GunSmithTableIngredient;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class GunSmithTableDisplay extends BasicDisplay {
    private final GunSmithTableRecipe recipe;
    private final Map.Entry<Identifier, CategoryIdentifier<GunSmithTableDisplay>> entry;

    public GunSmithTableDisplay(GunSmithTableRecipe recipe, Map.Entry<Identifier, CategoryIdentifier<GunSmithTableDisplay>> entry) {
        super(EntryIngredients.ofIngredients(recipe.getInputs().stream().map(GunSmithTableIngredient::getIngredient).toList()), Collections.singletonList(EntryIngredients.of(recipe.getOutput())), Optional.ofNullable(entry.getKey()));
        this.recipe = recipe;
        this.entry = entry;
    }

    public GunSmithTableRecipe getRecipe() {
        return recipe;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return entry.getValue();
    }

    // Nunca sincronizado do servidor via REI (as receitas já vêm do datapack normal do jogo),
    // então não precisa de um DisplaySerializer registrado - null é permitido pela API
    @Override
    public @Nullable DisplaySerializer<? extends Display> getSerializer() {
        return null;
    }
}