package com.tacz.guns.compat.controllable;

import com.tacz.guns.api.item.gun.FireMode;
import net.minecraft.world.item.ItemStack;

// Controllable ainda não tem build pra 26.2 (dependência comentada no build.gradle) -
// ControllableInner está excluído da compilação, então isso vira no-op até a lib publicar
// uma versão compatível.
public class ControllableCompat {
    public static void init() {
    }

    public static void onGunShoot(ItemStack gunItem, FireMode fireMode) {
    }
}
