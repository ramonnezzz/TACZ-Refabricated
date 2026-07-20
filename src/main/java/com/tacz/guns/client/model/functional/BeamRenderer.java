package com.tacz.guns.client.model.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.client.resource.pojo.display.LaserConfig;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.util.LaserColorUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

// A RenderType customizada (LaserBeamRenderState, com CompositeState/RenderStateShard) foi
// removida: esse jeito de montar um RenderType sumiu na 26.2 (RenderType agora embrulha um
// RenderPipeline). Só é alcançável hoje pela cadeia de renderer de item builtin, desativada
// (ver build.gradle), então usa uma RenderType pronta (entityTranslucentEmissive) como
// aproximação em vez de recriar o pipeline aditivo customizado.
public class BeamRenderer {
    public static final Identifier LASER_BEAM_TEXTURE = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "textures/entity/beam.png");
    private static final LaserConfig DEFAULT_LASER_CONFIG = new LaserConfig();
    // LightTexture.pack(15, 15) - LightTexture sumiu, o formato do int empacotado não mudou
    private static final int FULL_BRIGHT_LIGHT = 15 << 20 | 15 << 4;

    public static void renderLaserBeam(ItemStack stack, PoseStack poseStack, ItemDisplayContext transformType, SubmitNodeCollector collector, @Nonnull List<BedrockPart> path) {
        if (stack == null || !transformType.firstPerson() && !(transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)) {
            return;
        }

        RenderType renderType = RenderTypes.entityTranslucentEmissive(LASER_BEAM_TEXTURE);
        poseStack.pushPose();
        {
            for (int i = 0; i < path.size(); ++i) {
                path.get(i).translateAndRotateAndScale(poseStack);
            }

            LaserConfig laserConfig = getLaserConfig(stack);

            int color = LaserColorUtil.getLaserColor(stack, laserConfig);
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;

            float z = transformType.firstPerson() ? -laserConfig.getLength() : -laserConfig.getLengthThird();
            float width = transformType.firstPerson() ? laserConfig.getWidth() : laserConfig.getWidthThird();
            boolean fadeOut = RenderConfig.ENABLE_LASER_FADE_OUT.get();
            collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) ->
                    stringVertex(z, width, consumer, pose, r, g, b, fadeOut));
        }
        poseStack.popPose();
    }

    private static LaserConfig getLaserConfig(ItemStack stack) {
        if (stack == null) {
            return DEFAULT_LASER_CONFIG;
        }

        if (stack.getItem() instanceof IAttachment iAttachment) {
            return TimelessAPI.getClientAttachmentIndex(iAttachment.getAttachmentId(stack))
                    .map(ClientAttachmentIndex::getLaserConfig)
                    .orElse(DEFAULT_LASER_CONFIG);
        }

        if (stack.getItem() instanceof IGun) {
            return TimelessAPI.getGunDisplay(stack)
                    .map(GunDisplayInstance::getLaserConfig)
                    .orElse(DEFAULT_LASER_CONFIG);
        }

        return DEFAULT_LASER_CONFIG;
    }

    private static void stringVertex(float z, float width, VertexConsumer pConsumer, PoseStack.Pose pPose, int r, int g, int b, boolean fadeOut) {
        float halfWidth = width / 2;
        int startColor = ARGB.colorFromFloat(1.0F, r / 255F, g / 255F, b / 255F);
        int endColor = ARGB.colorFromFloat(fadeOut ? 0 : 1.0F, r / 255F, g / 255F, b / 255F);
        pConsumer.addVertex(pPose.pose(), -halfWidth, -halfWidth, 0).setColor(startColor).setUv(0, 0).setLight(FULL_BRIGHT_LIGHT);
        pConsumer.addVertex(pPose.pose(), -halfWidth, halfWidth, 0).setColor(startColor).setUv(0, 1).setLight(FULL_BRIGHT_LIGHT);
        pConsumer.addVertex(pPose.pose(), -halfWidth, halfWidth, z).setColor(endColor).setUv(1, 1).setLight(FULL_BRIGHT_LIGHT);
        pConsumer.addVertex(pPose.pose(), -halfWidth, -halfWidth, z).setColor(endColor).setUv(1, 0).setLight(FULL_BRIGHT_LIGHT);

        pConsumer.addVertex(pPose.pose(), -halfWidth, halfWidth, 0).setColor(startColor).setUv(0, 0).setLight(FULL_BRIGHT_LIGHT);
        pConsumer.addVertex(pPose.pose(), halfWidth, halfWidth, 0).setColor(startColor).setUv(0, 1).setLight(FULL_BRIGHT_LIGHT);
        pConsumer.addVertex(pPose.pose(), halfWidth, halfWidth, z).setColor(endColor).setUv(1, 1).setLight(FULL_BRIGHT_LIGHT);
        pConsumer.addVertex(pPose.pose(), -halfWidth, halfWidth, z).setColor(endColor).setUv(1, 0).setLight(FULL_BRIGHT_LIGHT);

        pConsumer.addVertex(pPose.pose(), halfWidth, halfWidth, 0).setColor(startColor).setUv(0, 0).setLight(FULL_BRIGHT_LIGHT);
        pConsumer.addVertex(pPose.pose(), halfWidth, -halfWidth, 0).setColor(startColor).setUv(0, 1).setLight(FULL_BRIGHT_LIGHT);
        pConsumer.addVertex(pPose.pose(), halfWidth, -halfWidth, z).setColor(endColor).setUv(1, 1).setLight(FULL_BRIGHT_LIGHT);
        pConsumer.addVertex(pPose.pose(), halfWidth, halfWidth, z).setColor(endColor).setUv(1, 0).setLight(FULL_BRIGHT_LIGHT);

        pConsumer.addVertex(pPose.pose(), halfWidth, -halfWidth, 0).setColor(startColor).setUv(0, 1).setLight(FULL_BRIGHT_LIGHT);
        pConsumer.addVertex(pPose.pose(), -halfWidth, -halfWidth, 0).setColor(startColor).setUv(0, 1).setLight(FULL_BRIGHT_LIGHT);
        pConsumer.addVertex(pPose.pose(), -halfWidth, -halfWidth, z).setColor(endColor).setUv(1, 1).setLight(FULL_BRIGHT_LIGHT);
        pConsumer.addVertex(pPose.pose(), halfWidth, -halfWidth, z).setColor(endColor).setUv(1, 0).setLight(FULL_BRIGHT_LIGHT);
    }
}
