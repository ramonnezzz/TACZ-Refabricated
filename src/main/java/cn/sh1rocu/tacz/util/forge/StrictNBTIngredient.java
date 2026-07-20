package cn.sh1rocu.tacz.util.forge;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

public class StrictNBTIngredient implements CustomIngredient {
    private final ItemStack stack;

    protected StrictNBTIngredient(ItemStack stack) {
        this.stack = stack;
    }

    /**
     * Creates a new ingredient matching the given stack and tag
     */
    public static StrictNBTIngredient of(ItemStack stack) {
        return new StrictNBTIngredient(stack);
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null)
            return false;
        //Can't use areItemStacksEqualUsingNBTShareTag because it compares stack size as well
        return this.stack.getItem() == input.getItem() && this.stack.getDamageValue() == input.getDamageValue() && areShareTagsEqual(this.stack, input);
    }

    private static boolean areShareTagsEqual(ItemStack stack, ItemStack other) {
        return Objects.equals(stack.get(DataComponents.CUSTOM_DATA), other.get(DataComponents.CUSTOM_DATA));
    }

    @Override
    public Stream<Holder<Item>> items() {
        return Stream.of(stack.getItem().builtInRegistryHolder());
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<StrictNBTIngredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static final Identifier ID = Identifier.fromNamespaceAndPath("forge", "nbt");

    public static class Serializer implements CustomIngredientSerializer<StrictNBTIngredient> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<StrictNBTIngredient> CODEC = ItemStack.MAP_CODEC.xmap(StrictNBTIngredient::new, i -> i.stack);

        public static final StreamCodec<RegistryFriendlyByteBuf, StrictNBTIngredient> STREAM_CODEC =
                ItemStack.OPTIONAL_STREAM_CODEC.map(StrictNBTIngredient::new, i -> i.stack);

        @Override
        public Identifier getIdentifier() {
            return ID;
        }

        @Override
        public MapCodec<StrictNBTIngredient> getCodec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StrictNBTIngredient> getStreamCodec() {
            return STREAM_CODEC;
        }
    }
}
