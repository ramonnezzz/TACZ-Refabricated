package com.tacz.guns.compat.shouldersurfing;

// ShoulderSurfing sem build pra 26.2 (ver build.gradle) - ShoulderSurfingCompat.showCrosshair()
// só chama isso quando INSTALLED, o que nunca acontece sem o mod real; vira stub sem as classes
// dele pra compilar (ShoulderSurfingPlugin, o outro arquivo que referenciava a API real, foi
// excluído do build)
public class ShoulderSurfingCompatInner {
    public static boolean showCrosshair() {
        return false;
    }
}
