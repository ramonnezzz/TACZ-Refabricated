package com.tacz.guns.resource.pojo.data.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;

import com.mojang.serialization.JsonOps;

import java.util.ArrayList;
import java.util.List;

public record LootTableInjection(List<Identifier> lootTables, LootTable lootTable, Identifier id) {
    // Deserializers.createLootTableSerializer() (Gson) sumiu na 26.2 - loot table virou
    // Codec puro (LootTable.DIRECT_CODEC), sem os registry lookups usados no CODEC normal
    // (enchantment predicates etc), suficiente pra essa injeção estática de pools
    public static LootTableInjection fromJson(Identifier fileId, JsonElement element) {
        JsonObject object = GsonHelper.convertToJsonObject(element, "loot injection");
        List<Identifier> lootTables = readLootTables(fileId, object);
        if (!object.has("pools")) {
            throw new JsonParseException("Loot injection " + fileId + " must define pools");
        }

        LootTable lootTable = LootTable.DIRECT_CODEC.parse(JsonOps.INSTANCE, object).getOrThrow();
        return new LootTableInjection(lootTables, lootTable, fileId);
    }

    private static List<Identifier> readLootTables(Identifier fileId, JsonObject object) {
        List<Identifier> lootTables = new ArrayList<>();
        if (object.has("loot_tables")) {
            for (JsonElement table : GsonHelper.getAsJsonArray(object, "loot_tables")) {
                lootTables.add(Identifier.parse(GsonHelper.convertToString(table, "loot table")));
            }
        } else if (object.has("loot_table")) {
            lootTables.add(Identifier.parse(GsonHelper.getAsString(object, "loot_table")));
        } else {
            throw new JsonParseException("Loot injection " + fileId + " must define loot_table or loot_tables");
        }
        return List.copyOf(lootTables);
    }

    public List<ItemStack> createStacks(LootParams params) {
        List<ItemStack> stacks = new ArrayList<>();
        lootTable.getRandomItemsRaw(params, stacks::add);
        return stacks;
    }
}