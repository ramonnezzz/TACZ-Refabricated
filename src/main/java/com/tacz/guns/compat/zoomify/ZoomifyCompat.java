package com.tacz.guns.compat.zoomify;

// Zoomify não tem build pra 26.2 ainda (ver build.gradle), então essa compat vira no-op:
// tanto a dependência quanto o hook ViewportEvent dela (de simplebedrockmodel-fabric,
// também sem build 26.2) estão indisponíveis.
public class ZoomifyCompat {
    public static void init() {
    }

    public static double getFov(double fov, float tickDelta) {
        return fov;
    }
}
