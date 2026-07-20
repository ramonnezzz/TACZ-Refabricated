package cn.sh1rocu.tacz.api.extension;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface IItem {
    // getMaxStackSize() virou método default de ItemInstance sem corpo físico em ItemStack.class
    // (ver ItemStackMixin, que agora declara o método inteiro em vez de usar
    // @ModifyReturnValue). Esse default aqui NÃO pode chamar stack.getMaxStackSize() de volta -
    // isso recairia direto no ItemStackMixin de novo e causaria recursão infinita pra qualquer
    // item que não sobrescreva esse hook - então replica a leitura do DataComponents direto.
    default int tacz$getMaxStackSize(ItemStack stack) {
        return stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }

    default boolean tacz$onEntitySwing(ItemStack stack, LivingEntity entity) {
        return false;
    }

    // getCustomRenderer() saiu daqui: BuiltinItemRendererRegistry (Fabric) não existe mais na
    // 26.2. O registro de renderer 3D custom de item virou o sistema SpecialModelRenderer da
    // vanilla (precisa de mudanças nos JSONs de item model também) - fica pra uma passada
    // dedicada depois.
}
