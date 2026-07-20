package com.tacz.guns.entity;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.api.extension.IMinecart;
import com.mojang.authlib.GameProfile;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.entity.ITargetEntity;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.config.common.OtherConfig;
import com.tacz.guns.init.ModBlocks;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.init.ModSounds;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.event.ServerMessageGunHurt;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// AbstractMinecart mudou de pacote e de arquitetura na 26.2 (agora usa MinecartBehavior por
// composição, e o antigo enum Type virou dois booleans: isRideable()/isFurnace()). O bloqueio de
// montaria (que antes vinha só do AbstractMinecartMixin interceptando a comparação com
// Type.RIDEABLE dentro de tick()) agora é direto via isRideable(), então o mixin genérico virou
// desnecessário e foi removido (ver tacz.fabric.mixins.json).
public class TargetMinecart extends AbstractMinecart implements ITargetEntity, IMinecart {
    // EntityType.Builder.build(String) virou build(ResourceKey<EntityType<?>>)
    public static EntityType<TargetMinecart> TYPE = EntityType.Builder.<TargetMinecart>of(TargetMinecart::new, MobCategory.MISC)
            .sized(0.75F, 2.4F)
            .clientTrackingRange(8)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "target_minecart")));

    private @Nullable GameProfile gameProfile = null;

    public TargetMinecart(EntityType<TargetMinecart> type, Level world) {
        super(type, world);
    }

    public TargetMinecart(Level level, double x, double y, double z) {
        super(TYPE, level, x, y, z);
    }

    @Override
    public void onProjectileHit(Entity entity, EntityHitResult result, DamageSource source, float damage) {
        if (this.level().isClientSide() || this.isRemoved()) {
            return;
        }
        // isIndirect() saiu de DamageSource - o equivalente é a negação de isDirect()
        if (source.isDirect()) {
            return;
        }
        Entity sourceEntity = source.getEntity();
        // displayClientMessage saiu de Player - agora é sendSystemMessage, só em ServerPlayer
        if (sourceEntity instanceof ServerPlayer player) {
            this.setHurtDir(-1);
            this.setHurtTime(10);
            this.markHurt();
            this.setDamage(10);
            double dis = this.position().distanceTo(sourceEntity.position());
            player.sendSystemMessage(Component.translatable("message.tacz.target_minecart.hit", String.format("%.1f", damage), String.format("%.2f", dis)), true);
            // 原版的声音传播距离由 volume 决定
            // 当声音大于 1 时，距离为 = 16 * volume
            float volume = OtherConfig.TARGET_SOUND_DISTANCE.get() / 16.0f;
            volume = Math.max(volume, 0);
            level().playSound(null, this, ModSounds.TARGET_HIT, SoundSource.BLOCKS, volume, this.level().getRandom().nextFloat() * 0.1F + 0.9F);

            if (entity instanceof EntityKineticBullet projectile) {
                boolean isHeadshot = false;
                float headshotMultiplier = 1;
                EntityHurtByGunEvent.Post event = new EntityHurtByGunEvent.Post(projectile, this, player, projectile.getGunId(), projectile.getGunDisplayId(), damage, Pair.of(source, source), isHeadshot, headshotMultiplier, LogicalSide.SERVER);
                EntityHurtByGunEvent.POST.invoker().post(event);
                NetworkHandler.sendToDimension(new ServerMessageGunHurt(projectile.getId(), this.getId(), player.getId(), projectile.getGunId(), projectile.getGunDisplayId(), damage, isHeadshot, headshotMultiplier), this);
            }
        }
    }

    @Override
    protected boolean shouldSourceDestroy(DamageSource source) {
        // isInvulnerableTo(DamageSource) saiu de Entity - o equivalente mais próximo aqui
        // (impedir que a explosão efetivamente destrua o carrinho) é este hook do VehicleEntity
        return !source.is(DamageTypeTags.IS_EXPLOSION) && super.shouldSourceDestroy(source);
    }

    @Override
    public boolean tacz$canBeRidden() {
        return false;
    }

    @Override
    public boolean isRideable() {
        return tacz$canBeRidden();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double size = this.getBoundingBox().getSize();
        if (Double.isNaN(size)) {
            size = 1.0;
        }
        size *= RenderConfig.TARGET_RENDER_DISTANCE.get() * getViewScale();
        return distance < size * size;
    }

    @Override
    protected void destroy(ServerLevel level, DamageSource source) {
        this.remove(Entity.RemovalReason.KILLED);
        if (level.getGameRules().get(GameRules.ENTITY_DROPS)) {
            ItemStack itemStack = new ItemStack(ModItems.TARGET_MINECART);
            if (this.hasCustomName()) {
                itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, this.getCustomName());
            }
            this.spawnAtLocation(level, itemStack);
        }
    }

    @Override
    protected @NotNull Item getDropItem() {
        return ModItems.TARGET_MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        ItemStack itemStack = new ItemStack(ModItems.TARGET_MINECART);
        if (this.hasCustomName()) {
            itemStack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, this.getCustomName());
        }
        return itemStack;
    }

    @Nullable
    public GameProfile getGameProfile() {
        // SkullBlockEntity.updateGameprofile (resolução assíncrona de textura) saiu da API;
        // esse perfil só é usado pra nome, então atribui direto sem resolver textura.
        if (this.gameProfile == null && this.getCustomName() != null) {
            this.gameProfile = new GameProfile(null, this.getCustomName().getString());
        }
        return gameProfile;
    }

    @Override
    @NotNull
    public BlockState getDefaultDisplayBlockState() {
        return ModBlocks.TARGET.defaultBlockState();
    }

    @Override
    protected double getMaxSpeed(ServerLevel level) {
        return 0.2F;
    }
}
