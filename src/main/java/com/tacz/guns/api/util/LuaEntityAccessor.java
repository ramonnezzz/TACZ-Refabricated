package com.tacz.guns.api.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

@SuppressWarnings("unused")
public record LuaEntityAccessor(LivingEntity entity) {
    public void sendSystemMessage(Component message) {
        if (entity instanceof Player player) {
            player.sendSystemMessage(message);
        }
    }

    public void sendActionBar(Component message) {
        if (entity instanceof ServerPlayer player) {
            player.sendSystemMessage(message, true);
        }
    }

    public float getHealth() {
        return entity.getHealth();
    }

    // hurt(DamageSource, float) saiu de LivingEntity - virou hurtServer(ServerLevel, ...)
    public boolean hurt(float amount) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        return entity.hurtServer(serverLevel, entity.level().damageSources().generic(), amount);
    }

    public Component literal(String text) {
        return Component.literal(text);
    }

    public Component translatable(String key) {
        return Component.translatable(key);
    }

    public Component translatable(String key, Component... components) {
        return Component.translatable(key, (Object[]) components);
    }
}
