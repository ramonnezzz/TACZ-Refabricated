package com.tacz.guns.api.item.nbt;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IGun;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public interface AmmoItemDataAccessor extends IAmmo {
    String AMMO_ID_TAG = "AmmoId";

    private static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    private static void mutateTag(ItemStack stack, Consumer<CompoundTag> mutator) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, mutator);
    }

    @Override
    @Nonnull
    default Identifier getAmmoId(ItemStack ammo) {
        CompoundTag nbt = getTag(ammo);
        if (nbt.contains(AMMO_ID_TAG)) {
            Identifier gunId = Identifier.tryParse(nbt.getStringOr(AMMO_ID_TAG, ""));
            return Objects.requireNonNullElse(gunId, DefaultAssets.EMPTY_AMMO_ID);
        }
        return DefaultAssets.EMPTY_AMMO_ID;
    }

    @Override
    default void setAmmoId(ItemStack ammo, @Nullable Identifier ammoId) {
        String id = ammoId != null ? ammoId.toString() : DefaultAssets.DEFAULT_AMMO_ID.toString();
        mutateTag(ammo, nbt -> nbt.putString(AMMO_ID_TAG, id));
    }

    @Override
    default boolean isAmmoOfGun(ItemStack gun, ItemStack ammo) {
        if (gun.getItem() instanceof IGun iGun && ammo.getItem() instanceof IAmmo iAmmo) {
            Identifier gunId = iGun.getGunId(gun);
            Identifier ammoId = iAmmo.getAmmoId(ammo);
            return TimelessAPI.getCommonGunIndex(gunId).map(gunIndex -> gunIndex.getGunData().getAmmoId().equals(ammoId)).orElse(false);
        }
        return false;
    }
}
