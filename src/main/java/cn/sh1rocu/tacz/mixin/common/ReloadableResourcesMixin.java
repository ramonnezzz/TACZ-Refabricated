package cn.sh1rocu.tacz.mixin.common;

import cn.sh1rocu.tacz.util.forge.EventHooks;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.ArrayList;
import java.util.List;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableResourcesMixin {
    // Em 26.2 a chamada a SimpleReloadInstance.create acontece dentro de um lambda de
    // loadResources, com a instância recém-criada disponível no local "result" — mesmo ponto de
    // injeção usado pelo fabric-resource-loader-v1 (ReloadableServerResourcesMixin).
    @ModifyArg(
            method = "lambda$loadResources$2",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/SimpleReloadInstance;create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
    private static List<PreparableReloadListener> tacz$addReloadListener(List<PreparableReloadListener> original,
                                                                         @Local(argsOnly = true) ReloadableServerRegistries.LoadResult loadResult,
                                                                         @Local(name = "result") ReloadableServerResources serverResources) {
        ArrayList<PreparableReloadListener> listeners = new ArrayList<>(original);
        listeners.addAll(EventHooks.onResourceReload(serverResources, loadResult.layers().compositeAccess()));
        return listeners;
    }
}
