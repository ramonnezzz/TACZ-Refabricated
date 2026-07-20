package com.tacz.guns.api.item.nbt;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public interface GunItemDataAccessor extends IGun {
    String GUN_ID_TAG = "GunId";
    String GUN_FIRE_MODE_TAG = "GunFireMode";
    String GUN_HAS_BULLET_IN_BARREL = "HasBulletInBarrel";
    String GUN_CURRENT_AMMO_COUNT_TAG = "GunCurrentAmmoCount";
    String GUN_ATTACHMENT_BASE = "Attachment";
    String GUN_EXP_TAG = "GunLevelExp";
    String GUN_DUMMY_AMMO = "DummyAmmo";
    String GUN_MAX_DUMMY_AMMO = "MaxDummyAmmo";
    String GUN_ATTACHMENT_LOCK = "AttachmentLock";
    String GUN_DISPLAY_ID_TAG = "GunDisplayId";
    String LASER_COLOR_TAG = "LaserColor";
    String GUN_OVERHEAT_TAG = "HeatAmount";
    String GUN_OVERHEAT_LOCK_TAG = "OverHeated";

    // Não há mais tag NBT "crua" no ItemStack: o estado da arma vive dentro do DataComponent
    // DataComponents.CUSTOM_DATA. getTag()/mutateTag() imitam o antigo getOrCreateTag() (leitura
    // e escrita in-place respectivamente) por cima dessa API nova.
    private static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    private static void mutateTag(ItemStack stack, Consumer<CompoundTag> mutator) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, mutator);
    }

    // Attachments ficam guardados como um ItemStack inteiro serializado dentro da tag da arma.
    // ItemStack.save(CompoundTag)/ItemStack.of(CompoundTag) saíram da API; agora a serialização
    // de ItemStack é feita via codec, que exige um RegistryOps. Os componentes usados aqui
    // (Item, CustomData) não dependem de registries dinâmicos, então RegistryAccess.EMPTY basta.
    // Campos de interface são sempre public static, então isso fica dentro dos métodos privados
    // abaixo em vez de virar uma constante exposta.
    private static CompoundTag saveItemStack(ItemStack stack) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, RegistryAccess.EMPTY);
        return (CompoundTag) ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
    }

    private static ItemStack loadItemStack(CompoundTag tag) {
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, RegistryAccess.EMPTY);
        return ItemStack.CODEC.parse(ops, tag).resultOrPartial().orElse(ItemStack.EMPTY);
    }

    @Override
    default boolean useDummyAmmo(ItemStack gun) {
        return getTag(gun).contains(GUN_DUMMY_AMMO);
    }

    @Override
    default int getDummyAmmoAmount(ItemStack gun) {
        return Math.max(0, getTag(gun).getIntOr(GUN_DUMMY_AMMO, 0));
    }

    @Override
    default void setDummyAmmoAmount(ItemStack gun, int amount) {
        mutateTag(gun, nbt -> nbt.putInt(GUN_DUMMY_AMMO, Math.max(amount, 0)));
    }

    @Override
    default void addDummyAmmoAmount(ItemStack gun, int amount) {
        if (!useDummyAmmo(gun)) {
            return;
        }
        int maxDummyAmmo = Integer.MAX_VALUE;
        if (hasMaxDummyAmmo(gun)) {
            maxDummyAmmo = getMaxDummyAmmoAmount(gun);
        }
        int newAmount = Math.max(Math.min(getDummyAmmoAmount(gun) + amount, maxDummyAmmo), 0);
        mutateTag(gun, nbt -> nbt.putInt(GUN_DUMMY_AMMO, newAmount));
    }

    @Override
    default boolean hasMaxDummyAmmo(ItemStack gun) {
        return getTag(gun).contains(GUN_MAX_DUMMY_AMMO);
    }

    @Override
    default int getMaxDummyAmmoAmount(ItemStack gun) {
        return Math.max(0, getTag(gun).getIntOr(GUN_MAX_DUMMY_AMMO, 0));
    }

    @Override
    default void setMaxDummyAmmoAmount(ItemStack gun, int amount) {
        mutateTag(gun, nbt -> nbt.putInt(GUN_MAX_DUMMY_AMMO, Math.max(amount, 0)));
    }

    @Override
    default boolean hasAttachmentLock(ItemStack gun) {
        return getTag(gun).getBooleanOr(GUN_ATTACHMENT_LOCK, false);
    }

    @Override
    default void setAttachmentLock(ItemStack gun, boolean lock) {
        mutateTag(gun, nbt -> nbt.putBoolean(GUN_ATTACHMENT_LOCK, lock));
    }

    @Override
    @Nonnull
    default Identifier getGunId(ItemStack gun) {
        CompoundTag nbt = getTag(gun);
        if (nbt.contains(GUN_ID_TAG)) {
            Identifier gunId = Identifier.tryParse(nbt.getStringOr(GUN_ID_TAG, ""));
            return Objects.requireNonNullElse(gunId, DefaultAssets.EMPTY_GUN_ID);
        }
        return DefaultAssets.EMPTY_GUN_ID;
    }

    @Override
    default void setGunId(ItemStack gun, @Nullable Identifier gunId) {
        if (gunId != null) {
            mutateTag(gun, nbt -> nbt.putString(GUN_ID_TAG, gunId.toString()));
        }
    }

    @Override
    @NotNull
    default Identifier getGunDisplayId(ItemStack gun) {
        CompoundTag nbt = getTag(gun);
        if (nbt.contains(GUN_DISPLAY_ID_TAG)) {
            Identifier gunDisplayId = Identifier.tryParse(nbt.getStringOr(GUN_DISPLAY_ID_TAG, ""));
            return Objects.requireNonNullElse(gunDisplayId, DefaultAssets.DEFAULT_GUN_DISPLAY_ID);
        }
        return DefaultAssets.DEFAULT_GUN_DISPLAY_ID;
    }

    @Override
    default void setGunDisplayId(ItemStack gun, Identifier displayId) {
        if (displayId != null) {
            mutateTag(gun, nbt -> nbt.putString(GUN_DISPLAY_ID_TAG, displayId.toString()));
        }
    }

    @Override
    default int getLevel(ItemStack gun) {
        CompoundTag nbt = getTag(gun);
        if (nbt.contains(GUN_EXP_TAG)) {
            return getLevel(nbt.getIntOr(GUN_EXP_TAG, 0));
        }
        return 0;
    }

    @Override
    default int getExp(ItemStack gun) {
        return getTag(gun).getIntOr(GUN_EXP_TAG, 0);
    }

    @Override
    default int getExpToNextLevel(ItemStack gun) {
        int exp = getExp(gun);
        int level = getLevel(exp);
        if (level >= getMaxLevel()) {
            return 0;
        }
        int nextLevelExp = getExp(level + 1);
        return nextLevelExp - exp;
    }

    @Override
    default int getExpCurrentLevel(ItemStack gun) {
        int exp = getExp(gun);
        int level = getLevel(exp);
        if (level <= 0) {
            return exp;
        } else {
            return exp - getExp(level - 1);
        }
    }

    @Override
    default FireMode getFireMode(ItemStack gun) {
        CompoundTag nbt = getTag(gun);
        if (nbt.contains(GUN_FIRE_MODE_TAG)) {
            return FireMode.valueOf(nbt.getStringOr(GUN_FIRE_MODE_TAG, FireMode.UNKNOWN.name()));
        }
        return FireMode.UNKNOWN;
    }

    @Override
    default void setFireMode(ItemStack gun, @Nullable FireMode fireMode) {
        String name = fireMode != null ? fireMode.name() : FireMode.UNKNOWN.name();
        mutateTag(gun, nbt -> nbt.putString(GUN_FIRE_MODE_TAG, name));
    }

    @Override
    default int getCurrentAmmoCount(ItemStack gun) {
        return getTag(gun).getIntOr(GUN_CURRENT_AMMO_COUNT_TAG, 0);
    }

    @Override
    default void setCurrentAmmoCount(ItemStack gun, int ammoCount) {
        mutateTag(gun, nbt -> nbt.putInt(GUN_CURRENT_AMMO_COUNT_TAG, Math.max(ammoCount, 0)));
    }

    @Override
    default void reduceCurrentAmmoCount(ItemStack gun) {
        // 只在不使用背包直读的情况下减少 AmmoCount
        if (!useInventoryAmmo(gun)) {
            setCurrentAmmoCount(gun, getCurrentAmmoCount(gun) - 1);
        }
    }

    @Override
    @Nullable
    default CompoundTag getAttachmentTag(ItemStack gun, AttachmentType type) {
        if (!allowAttachmentType(gun, type)) {
            return null;
        }
        CompoundTag nbt = getTag(gun);
        String key = GUN_ATTACHMENT_BASE + type.name();
        if (nbt.contains(key)) {
            ItemStack attachment = loadItemStack(nbt.getCompoundOrEmpty(key));
            CustomData data = attachment.get(DataComponents.CUSTOM_DATA);
            return data == null ? null : data.copyTag();
        }
        return null;
    }

    @Override
    @NotNull
    default ItemStack getBuiltinAttachment(ItemStack gun, AttachmentType type) {
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return ItemStack.EMPTY;
        }
        CommonGunIndex index = TimelessAPI.getCommonGunIndex(iGun.getGunId(gun)).orElse(null);
        if (index != null) {
            var builtin = index.getGunData().getBuiltInAttachments();
            if (builtin.containsKey(type)) {
                return AttachmentItemBuilder.create().setId(builtin.get(type)).build();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    default ItemStack getAttachment(ItemStack gun, AttachmentType type) {
        if (!allowAttachmentType(gun, type)) {
            return ItemStack.EMPTY;
        }
        CompoundTag nbt = getTag(gun);
        String key = GUN_ATTACHMENT_BASE + type.name();
        if (nbt.contains(key)) {
            return loadItemStack(nbt.getCompoundOrEmpty(key));
        }
        return ItemStack.EMPTY;
    }

    @Override
    @NotNull
    default Identifier getBuiltInAttachmentId(ItemStack gun, AttachmentType type) {
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return DefaultAssets.EMPTY_ATTACHMENT_ID;
        }
        CommonGunIndex index = TimelessAPI.getCommonGunIndex(iGun.getGunId(gun)).orElse(null);
        if (index != null) {
            var builtin = index.getGunData().getBuiltInAttachments();
            if (builtin.containsKey(type)) {
                return builtin.get(type);
            }
        }
        return DefaultAssets.EMPTY_ATTACHMENT_ID;
    }

    @Override
    @Nonnull
    default Identifier getAttachmentId(ItemStack gun, AttachmentType type) {
        CompoundTag attachmentTag = this.getAttachmentTag(gun, type);
        if (attachmentTag != null) {
            return AttachmentItemDataAccessor.getAttachmentIdFromTag(attachmentTag);
        }
        return DefaultAssets.EMPTY_ATTACHMENT_ID;
    }

    @Override
    default void installAttachment(@Nonnull ItemStack gun, @Nonnull ItemStack attachment) {
        if (!allowAttachment(gun, attachment)) {
            return;
        }
        IAttachment iAttachment = IAttachment.getIAttachmentOrNull(attachment);
        if (iAttachment == null) {
            return;
        }
        String key = GUN_ATTACHMENT_BASE + iAttachment.getType(attachment).name();
        CompoundTag attachmentTag = saveItemStack(attachment);
        mutateTag(gun, nbt -> nbt.put(key, attachmentTag));
    }

    @Override
    default void unloadAttachment(@Nonnull ItemStack gun, AttachmentType type) {
        if (!allowAttachmentType(gun, type)) {
            return;
        }
        String key = GUN_ATTACHMENT_BASE + type.name();
        mutateTag(gun, nbt -> nbt.remove(key));
    }

    @Override
    default float getAimingZoom(ItemStack gunItem) {
        float zoom = 1;
        Identifier scopeId = this.getAttachmentId(gunItem, AttachmentType.SCOPE);
        boolean builtin = false;
        if (scopeId.equals(DefaultAssets.EMPTY_ATTACHMENT_ID)) {
            scopeId = getBuiltInAttachmentId(gunItem, AttachmentType.SCOPE);
            builtin = true;
        }
        if (!DefaultAssets.isEmptyAttachmentId(scopeId)) {
            CompoundTag attachmentTag = this.getAttachmentTag(gunItem, AttachmentType.SCOPE);
            int zoomNumber = builtin ? 0 : AttachmentItemDataAccessor.getZoomNumberFromTag(attachmentTag);
            float[] zooms = TimelessAPI.getClientAttachmentIndex(scopeId).map(ClientAttachmentIndex::getZoom).orElse(null);
            if (zooms != null) {
                zoom = zooms[zoomNumber % zooms.length];
            }
        } else {
            zoom = TimelessAPI.getGunDisplay(gunItem).map(GunDisplayInstance::getIronZoom).orElse(1f);
        }
        return zoom;
    }

    @Override
    default boolean hasBulletInBarrel(ItemStack gun) {
        return getTag(gun).getBooleanOr(GUN_HAS_BULLET_IN_BARREL, false);
    }

    @Override
    default void setBulletInBarrel(ItemStack gun, boolean bulletInBarrel) {
        mutateTag(gun, nbt -> nbt.putBoolean(GUN_HAS_BULLET_IN_BARREL, bulletInBarrel));
    }

    @Override
    default boolean hasCustomLaserColor(ItemStack gun) {
        return getTag(gun).contains(LASER_COLOR_TAG);
    }

    @Override
    default int getLaserColor(ItemStack gun) {
        return getTag(gun).getIntOr(LASER_COLOR_TAG, 0xFF0000);
    }

    @Override
    default void setLaserColor(ItemStack gun, int color) {
        mutateTag(gun, nbt -> nbt.putInt(LASER_COLOR_TAG, color));
    }

    /**
     * Heat Data
     */
    @Override
    default boolean hasHeatData(ItemStack gun) {
        return getTag(gun).contains(GUN_OVERHEAT_TAG);
    }

    @Override
    default boolean isOverheatLocked(ItemStack gun) {
        return getTag(gun).getBooleanOr(GUN_OVERHEAT_LOCK_TAG, false);
    }

    @Override
    default void setOverheatLocked(ItemStack gun, boolean locked) {
        mutateTag(gun, nbt -> nbt.putBoolean(GUN_OVERHEAT_LOCK_TAG, locked));
    }

    @Override
    default float getHeatAmount(ItemStack gun) {
        if (hasHeatData(gun)) return getTag(gun).getFloatOr(GUN_OVERHEAT_TAG, 0f);
        return 0f;
    }

    @Override
    default void setHeatAmount(ItemStack gun, float amount) {
        float clamped = amount >= 0 ? amount : 0f;
        mutateTag(gun, nbt -> nbt.putFloat(GUN_OVERHEAT_TAG, clamped));
    }

    @Override
    default float lerpRPM(ItemStack gun) {
        return TimelessAPI.getCommonGunIndex(getGunId(gun))
                .map(index -> index.getGunData().getHeatData())
                .map(heatData -> {
                    float heatPercentage = (getHeatAmount(gun) / heatData.getHeatMax());
                    return Mth.lerp(heatPercentage, heatData.getMinRpmMod(), heatData.getMaxRpmMod());
                }).orElse(1f);
    }

    @Override
    default float lerpInaccuracy(ItemStack gun) {
        return TimelessAPI.getCommonGunIndex(getGunId(gun))
                .map(index -> index.getGunData().getHeatData())
                .map(heatData -> {
                    float heatPercentage = (getHeatAmount(gun) / heatData.getHeatMax());
                    return Mth.lerp(heatPercentage, heatData.getMinInaccuracy(), heatData.getMaxInaccuracy());
                }).orElse(1f);
    }
}
