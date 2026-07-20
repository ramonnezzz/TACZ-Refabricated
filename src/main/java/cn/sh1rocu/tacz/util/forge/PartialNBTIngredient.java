package cn.sh1rocu.tacz.util.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.advancements.predicates.NbtPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PartialNBTIngredient implements CustomIngredient {
    private static final Codec<Set<Item>> ITEMS_CODEC = BuiltInRegistries.ITEM.byNameCodec().listOf()
            .xmap(HashSet::new, list -> list.stream().toList());
    private static final StreamCodec<RegistryFriendlyByteBuf, Set<Item>> ITEMS_STREAM_CODEC =
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.registry(Registries.ITEM));

    private final Set<Item> items;
    private final CompoundTag nbt;
    private final NbtPredicate predicate;

    protected PartialNBTIngredient(Set<Item> items, CompoundTag nbt) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cannot create a PartialNBTIngredient with no items");
        }
        this.items = Collections.unmodifiableSet(items);
        this.nbt = nbt;
        this.predicate = new NbtPredicate(nbt);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given NBT
     */
    public static PartialNBTIngredient of(CompoundTag nbt, ItemLike... items) {
        return new PartialNBTIngredient(Arrays.stream(items).map(ItemLike::asItem).collect(Collectors.toSet()), nbt);
    }

    /**
     * Creates a new ingredient matching the given item, containing the given NBT
     */
    public static PartialNBTIngredient of(ItemLike item, CompoundTag nbt) {
        return new PartialNBTIngredient(Set.of(item.asItem()), nbt);
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null)
            return false;
        return items.contains(input.getItem()) && predicate.matches(input);
    }

    @Override
    public Stream<Holder<Item>> items() {
        return items.stream().map(Item::builtInRegistryHolder);
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<PartialNBTIngredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static final Identifier ID = Identifier.fromNamespaceAndPath("forge", "partial_nbt");

    public static class Serializer implements CustomIngredientSerializer<PartialNBTIngredient> {
        public static final Serializer INSTANCE = new Serializer();

        public static final MapCodec<PartialNBTIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ITEMS_CODEC.fieldOf("items").forGetter(i -> i.items),
                CompoundTag.CODEC.fieldOf("nbt").forGetter(i -> i.nbt)
        ).apply(instance, PartialNBTIngredient::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PartialNBTIngredient> STREAM_CODEC = StreamCodec.composite(
                ITEMS_STREAM_CODEC, i -> i.items,
                ByteBufCodecs.TRUSTED_COMPOUND_TAG, i -> i.nbt,
                PartialNBTIngredient::new
        );

        @Override
        public Identifier getIdentifier() {
            return ID;
        }

        @Override
        public MapCodec<PartialNBTIngredient> getCodec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PartialNBTIngredient> getStreamCodec() {
            return STREAM_CODEC;
        }
    }
}
