package com.tacz.guns.api.item.nbt;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.function.Consumer;

public interface AmmoBoxItemDataAccessor extends IAmmoBox {
    String AMMO_ID_TAG = "AmmoId";
    String AMMO_COUNT_TAG = "AmmoCount";
    String CREATIVE_TAG = "Creative";
    String ALL_TYPE_CREATIVE_TAG = "AllTypeCreative";
    String LEVEL_TAG = "Level";

    private static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    private static void mutateTag(ItemStack stack, Consumer<CompoundTag> mutator) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, mutator);
    }

    @Override
    default Identifier getAmmoId(ItemStack ammoBox) {
        CompoundTag tag = getTag(ammoBox);
        if (tag.contains(AMMO_ID_TAG)) {
            return Identifier.parse(tag.getStringOr(AMMO_ID_TAG, ""));
        }
        return DefaultAssets.EMPTY_AMMO_ID;
    }

    @Override
    default void setAmmoId(ItemStack ammoBox, Identifier ammoId) {
        mutateTag(ammoBox, tag -> tag.putString(AMMO_ID_TAG, ammoId.toString()));
    }

    @Override
    default int getAmmoCount(ItemStack ammoBox) {
        if (isAllTypeCreative(ammoBox) || isCreative(ammoBox)) {
            return Integer.MAX_VALUE;
        }
        return getTag(ammoBox).getIntOr(AMMO_COUNT_TAG, 0);
    }

    @Override
    default void setAmmoCount(ItemStack ammoBox, int count) {
        int stored = isCreative(ammoBox) ? Integer.MAX_VALUE : count;
        mutateTag(ammoBox, tag -> tag.putInt(AMMO_COUNT_TAG, stored));
    }

    @Override
    default boolean isAmmoBoxOfGun(ItemStack gun, ItemStack ammoBox) {
        if (gun.getItem() instanceof IGun iGun && ammoBox.getItem() instanceof IAmmoBox iAmmoBox) {
            if (isAllTypeCreative(ammoBox)) {
                return true;
            }
            Identifier ammoId = iAmmoBox.getAmmoId(ammoBox);
            if (ammoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
                return false;
            }
            Identifier gunId = iGun.getGunId(gun);
            return TimelessAPI.getCommonGunIndex(gunId).map(gunIndex -> gunIndex.getGunData().getAmmoId().equals(ammoId)).orElse(false);
        }
        return false;
    }

    @Override
    default ItemStack setAmmoLevel(ItemStack ammoBox, int level) {
        mutateTag(ammoBox, tag -> tag.putInt(LEVEL_TAG, Math.max(level, 0)));
        return ammoBox;
    }

    @Override
    default int getAmmoLevel(ItemStack ammoBox) {
        return getTag(ammoBox).getIntOr(LEVEL_TAG, 0);
    }

    @Override
    default boolean isCreative(ItemStack ammoBox) {
        return getTag(ammoBox).getBooleanOr(CREATIVE_TAG, false);
    }

    @Override
    default boolean isAllTypeCreative(ItemStack ammoBox) {
        return getTag(ammoBox).getBooleanOr(ALL_TYPE_CREATIVE_TAG, false);
    }

    @Override
    default ItemStack setCreative(ItemStack ammoBox, boolean isAllType) {
        mutateTag(ammoBox, tag -> {
            if (isAllType) {
                // 移除可能存在的创造模式标签
                tag.remove(CREATIVE_TAG);
                tag.putBoolean(ALL_TYPE_CREATIVE_TAG, true);
            } else {
                // 移除可能存在的全类型标签
                tag.remove(ALL_TYPE_CREATIVE_TAG);
                tag.putBoolean(CREATIVE_TAG, true);
            }
        });
        return ammoBox;
    }
}
