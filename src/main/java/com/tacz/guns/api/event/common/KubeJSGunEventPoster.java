package com.tacz.guns.api.event.common;

import cn.sh1rocu.tacz.api.event.BaseEvent;

// KubeJS ainda não tem build pra 26.2 (ver build.gradle) - compat/kubejs está excluído da
// compilação, então esses métodos viram no-op até a lib publicar uma versão compatível.
public interface KubeJSGunEventPoster<E extends BaseEvent> {
    default void postEventToKubeJS(E event) {
    }

    //客户端事件应调用此方法
    default void postClientEventToKubeJS(E event) {
    }

    //服务端事件应调用此方法
    default void postServerEventToKubeJS(E event) {
    }
}
