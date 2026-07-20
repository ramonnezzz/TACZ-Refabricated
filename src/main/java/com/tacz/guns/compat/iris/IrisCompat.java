package com.tacz.guns.compat.iris;

import com.tacz.guns.compat.iris.legacy.IrisCompatLegacy;
import com.tacz.guns.compat.iris.newly.IrisCompatNewly;
import com.tacz.guns.init.CompatRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.irisshaders.iris.api.v0.IrisApi;

import java.util.function.Supplier;

public final class IrisCompat {
    private static final Version VERSION;

    static {
        try {
            VERSION = Version.parse("1.7.0");
        } catch (VersionParsingException e) {
            throw new RuntimeException(e);
        }
    }

    private static Supplier<Boolean> IS_RENDER_SHADOW_SUPPER;

    public static void initCompat() {
        FabricLoader.getInstance().getModContainer(CompatRegistry.IRIS).ifPresent(mod -> {
            if (mod.getMetadata().getVersion().compareTo(VERSION) >= 0) {
                IS_RENDER_SHADOW_SUPPER = IrisCompatNewly::isRenderShadow;
            } else {
                IS_RENDER_SHADOW_SUPPER = IrisCompatLegacy::isRenderShadow;
            }
        });
    }

    public static boolean isRenderShadow() {
        if (FabricLoader.getInstance().isModLoaded(CompatRegistry.IRIS)) {
            return IS_RENDER_SHADOW_SUPPER.get();
        }
        return false;
    }

    public static boolean isUsingRenderPack() {
        if (FabricLoader.getInstance().isModLoaded(CompatRegistry.IRIS)) {
            return IrisApi.getInstance().isShaderPackInUse();
        }
        return false;
    }
}
