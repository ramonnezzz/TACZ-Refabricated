package com.tacz.guns.init;

import com.tacz.guns.compat.iris.IrisCompat;
import net.fabricmc.loader.api.FabricLoader;

public class CompatRegistry {
    public static final String CLOTH_CONFIG = "cloth-config";
    public static final String IRIS = "iris";

    public static void onEnqueue() {
        checkModLoad(IRIS, IrisCompat::initCompat);
        // Carry On sem build pra 26.2 (ver build.gradle) - BlackList.addBlackList() removido
    }

    public static void checkModLoad(String modId, Runnable runnable) {
        if (FabricLoader.getInstance().isModLoaded(modId)) {
            runnable.run();
        }
    }
}
