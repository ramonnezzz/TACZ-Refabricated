package cn.sh1rocu.tacz.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.loot.LootTableInjectorModifier;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// getRandomItems(LootContext) sumiu na 26.2 - só sobraram os overloads baseados em LootParams
@Mixin(LootTable.class)
public class LootTableMixin {
    @ModifyReturnValue(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;J)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At(value = "RETURN"))
    private ObjectArrayList<ItemStack> tacz$globalModifier(ObjectArrayList<ItemStack> list, LootParams params, long seed) {
        return LootTableInjectorModifier.doApply(list, params, (LootTable) (Object) this);
    }
}