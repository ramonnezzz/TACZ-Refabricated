package com.tacz.guns.compat.iris.newly;

import net.irisshaders.iris.shadows.ShadowRenderingState;

public final class IrisCompatNewly {
    public static boolean isRenderShadow() {
        return ShadowRenderingState.areShadowsCurrentlyBeingRendered();
    }
}
