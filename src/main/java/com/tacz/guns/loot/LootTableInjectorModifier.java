package com.tacz.guns.loot;

import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.pojo.data.loot.LootTableInjection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LootTableInjectorModifier {
    // LootDataManager/LootDataId sumiram na 26.2 - loot tables agora são um Registry<LootTable>
    // de verdade (Registries.LOOT_TABLE), então dá pra pegar o id direto via getKey() em vez do
    // hack antigo de mixin acessando o mapa interno do gerenciador (ver LootManagerAccessor,
    // removido)
    public static @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootParams params, LootTable table) {
        CommonAssetsManager manager = CommonAssetsManager.getInstance();
        if (manager == null) {
            return generatedLoot;
        }

        Identifier lootTableId = params.getLevel().registryAccess().lookupOrThrow(Registries.LOOT_TABLE).getKey(table);
        if (lootTableId == null) {
            return generatedLoot;
        }

        List<LootTableInjection> injections = manager.getLootTableInjections(lootTableId);
        if (injections.isEmpty()) {
            return generatedLoot;
        }

        for (LootTableInjection injection : injections) {
            generatedLoot.addAll(injection.createStacks(params));
        }
        return generatedLoot;
    }
}
