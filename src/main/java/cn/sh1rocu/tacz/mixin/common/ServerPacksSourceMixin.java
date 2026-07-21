package cn.sh1rocu.tacz.mixin.common;

import cn.sh1rocu.tacz.api.event.AddPackFindersEvent;
import cn.sh1rocu.tacz.api.mixin.PackRepositoryExtension;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPacksSource.class)
public class ServerPacksSourceMixin {
    // Em 26.2 o createPackRepository(Path) único virou duas sobrecargas: (Path, DirectoryValidator)
    // — usada na criação de mundo — e (LevelStorageAccess) — usada ao carregar mundo existente.
    // Injeta nas duas; o handler não captura args do alvo, então a mesma assinatura serve para ambas.
    @Inject(method = {
            "createPackRepository(Ljava/nio/file/Path;Lnet/minecraft/world/level/validation/DirectoryValidator;)Lnet/minecraft/server/packs/repository/PackRepository;",
            "createPackRepository(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;)Lnet/minecraft/server/packs/repository/PackRepository;"
    }, at = @At("RETURN"))
    private static void tacz$addPacks(CallbackInfoReturnable<PackRepository> cir) {
        AddPackFindersEvent event = new AddPackFindersEvent(PackType.SERVER_DATA, ((PackRepositoryExtension) cir.getReturnValue())::tacz$addPackFinder, false);
        AddPackFindersEvent.CALLBACK.invoker().onAddPackFinders(event);
    }
}
