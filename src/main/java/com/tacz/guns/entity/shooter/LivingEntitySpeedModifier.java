package com.tacz.guns.entity.shooter;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.custom.ExtraMovementModifier;
import com.tacz.guns.resource.modifier.custom.WeightModifier;
import com.tacz.guns.GunMod;
import com.tacz.guns.resource.pojo.data.gun.MoveSpeed;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

// AttributeModifier trocou o id de UUID pra Identifier (não precisa mais de nome legível
// separado - o Identifier já serve como nome/id único)
public class LivingEntitySpeedModifier {
    private static final Identifier EXTRA_SPEED_MODIFIER_UUID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "extra_gun_speed_modifier");
    private static final Identifier WEIGHT_SPEED_MODIFIER_UUID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "gun_speed_modifier");
    private final LivingEntity shooter;
    private final ShooterDataHolder dataHolder;

    public LivingEntitySpeedModifier(LivingEntity shooter, ShooterDataHolder dataHolder) {
        this.shooter = shooter;
        this.dataHolder = dataHolder;
    }

    public void updateSpeedModifier() {
        if (!shooter.isAlive()) return;

        ItemStack stack = shooter.getMainHandItem();
        var speedModifier = shooter.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);
        if (speedModifier == null) return;

        if (stack.getItem() instanceof AbstractGunItem) {
            // 处理重量带来的修正
            AttachmentCacheProperty cacheProperty = IGunOperator.fromLivingEntity(shooter).getCacheProperty();
            if (cacheProperty != null) {
                double weightFactor = SyncConfig.WEIGHT_SPEED_MULTIPLIER.get();
                if (weightFactor > 0) {
                    float targetSpeed = cacheProperty.getCache(WeightModifier.ID);
                    targetSpeed *= (float) -weightFactor;
                    AttributeModifier currentModifier = speedModifier.getModifier(WEIGHT_SPEED_MODIFIER_UUID);
                    if (currentModifier == null || currentModifier.amount() != targetSpeed) {
                        speedModifier.removeModifier(WEIGHT_SPEED_MODIFIER_UUID);
                        speedModifier.addTransientModifier(new AttributeModifier(WEIGHT_SPEED_MODIFIER_UUID,
                                targetSpeed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                    }
                }

                MoveSpeed speed = cacheProperty.getCache(ExtraMovementModifier.ID);
                if (speed != null) {
                    double targetSpeed = getTargetSpeed(speed);
                    AttributeModifier currentModifier = speedModifier.getModifier(EXTRA_SPEED_MODIFIER_UUID);
                    if (currentModifier == null || currentModifier.amount() != targetSpeed) {
                        speedModifier.removeModifier(EXTRA_SPEED_MODIFIER_UUID);
                        speedModifier.addTransientModifier(new AttributeModifier(EXTRA_SPEED_MODIFIER_UUID,
                                targetSpeed, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                    }
                }
            }
        } else {
            speedModifier.removeModifier(WEIGHT_SPEED_MODIFIER_UUID);
            speedModifier.removeModifier(EXTRA_SPEED_MODIFIER_UUID);
        }
    }

    private double getTargetSpeed(MoveSpeed moveSpeed) {
        if (dataHolder.reloadStateType.isReloading()) {
            return moveSpeed.getReloadMultiplier();
        }
        if (dataHolder.isAiming) {
            return moveSpeed.getAimMultiplier();
        }
        return moveSpeed.getBaseMultiplier();
    }

}
