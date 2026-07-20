package com.tacz.guns.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.tacz.guns.GunMod;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.minecraftforge.common.ForgeConfigSpec;
import net.neoforged.fml.config.ModConfig;

import java.nio.file.Path;

public class PreLoadConfig {
    public static void init() {
        ConfigRegistry.INSTANCE.register(GunMod.MOD_ID, ModConfig.Type.COMMON, spec, "tacz-pre.toml");
    }

    private static ForgeConfigSpec spec;
    public static ForgeConfigSpec.BooleanValue override;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("gunpack");
        builder.comment("When enabled, the mod will not try to overwrite the default pack under .minecraft/tacz\n" +
                "Since 1.0.4, the overwriting will only run when you start client or a dedicated server");
        override = builder.define("DefaultPackDebug", false);
        builder.pop();
        spec = builder.build();
    }

    // forgeconfigapiport 26.2.1: ModConfig virou final e perdeu os hooks internos (getHandler(),
    // ConfigTracker exposto) que a versão antiga usava pra montar um ModConfig falso e ler esse
    // arquivo de um diretório customizado (gamedir/tacz, não gamedir/config) antes do registro
    // normal. Em vez disso, carrega o CommentedFileConfig direto e aplica no spec - o spec
    // (ForgeConfigSpec) não depende de ModConfig/ConfigTracker pra funcionar sozinho.
    public static void load(Path configBasePath) {
        if (spec.isLoaded()) return;
        CommentedFileConfig configData = CommentedFileConfig.builder(configBasePath.resolve("tacz-pre.toml")).sync().build();
        configData.load();
        spec.correct(configData);
        spec.acceptConfig(configData);
        configData.save();
    }
}
