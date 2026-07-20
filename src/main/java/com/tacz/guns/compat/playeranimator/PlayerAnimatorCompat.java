package com.tacz.guns.compat.playeranimator;

import com.tacz.guns.client.resource.GunDisplayInstance;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.world.entity.LivingEntity;

import java.io.File;
import java.util.function.Consumer;
import java.util.zip.ZipFile;

// player-animation-lib ainda não tem build pra 26.2 (ver build.gradle) - o pacote
// compat/playeranimator/animation está excluído da compilação, então isso vira um wrapper
// no-op até a lib publicar uma versão compatível.
public class PlayerAnimatorCompat {
    public static void init() {
    }

    public static boolean loadAnimationFromZip(ZipFile zipFile, String zipPath) {
        return false;
    }

    public static void loadAnimationFromFile(File file) {
    }

    public static void clearAllAnimationCache() {
    }

    public static boolean hasPlayerAnimator3rd(LivingEntity livingEntity, GunDisplayInstance display) {
        return false;
    }

    public static void stopAllAnimation(LivingEntity livingEntity) {
    }

    public static void stopAllAnimation(LivingEntity livingEntity, int fadeTime) {
    }

    public static void playAnimation(LivingEntity livingEntity, GunDisplayInstance display, float limbSwingAmount) {
    }

    public static boolean isInstalled() {
        return false;
    }

    public static void registerReloadListener(Consumer<IdentifiableResourceReloadListener> register) {
    }
}
