package com.tacz.guns.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;

@Environment(EnvType.CLIENT)
public final class RenderHelper {
    // blit()/innerBlit() removidos: eram código morto (nenhum caller), usavam a API antiga de
    // Tesselator/BufferBuilder/BufferUploader que sumiu na 26.2

    // enableItemEntityStencilTest/disableItemEntityStencilTest removidos: sem callers (a
    // máscara de stencil do lens do scope já foi removida em BedrockAttachmentModel, já que
    // RenderSystem não expõe mais controles de stencil na 26.2), e Minecraft.getMainRenderTarget()
    // também sumiu

    // PlayerRenderer.renderRightHand/renderLeftHand sumiram na 26.2 (movidos pro novo pipeline
    // submit/extractRenderState) - só usada pelo caminho de renderer de item builtin, que está
    // desativado por ora (ver build.gradle), então vira no-op até termos um chamador real
    public static void renderFirstPersonArm(LocalPlayer player, HumanoidArm hand, PoseStack matrixStack, int combinedLight) {
    }
}
