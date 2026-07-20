package cn.sh1rocu.tacz.mixin.common;

import cn.sh1rocu.tacz.api.extension.IItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// ItemStack.getMaxStackSize() virou um método default de ItemInstance sem corpo físico em
// ItemStack.class, então @ModifyReturnValue não tem onde injetar. Em vez disso, declara o
// método inteiro aqui - Mixin funde ele em ItemStack.class, onde passa a sobrescrever de
// verdade o default herdado.
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract Item getItem();

    public int getMaxStackSize() {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() instanceof IItem item) {
            return item.tacz$getMaxStackSize(self);
        }
        return self.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }
}
