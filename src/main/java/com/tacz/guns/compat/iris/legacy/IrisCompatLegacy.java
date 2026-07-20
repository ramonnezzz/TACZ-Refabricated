package com.tacz.guns.compat.iris.legacy;

import net.irisshaders.iris.shadows.ShadowRenderingState;

public final class IrisCompatLegacy {
    public static boolean isRenderShadow() {
        return ShadowRenderingState.areShadowsCurrentlyBeingRendered();
    }
}
