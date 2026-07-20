package com.tacz.guns.client.renderer.block;

import cn.sh1rocu.tacz.api.mixin.BlockEntityRenderStateEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.block.TargetBlock;
import com.tacz.guns.block.entity.TargetBlockEntity;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.resource.InternalAssetLoader;
import com.tacz.guns.config.client.RenderConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

// render(...) virou createRenderState()/extractRenderState()/submit() na 26.2, igual
// StatueRenderer/EntityBulletRenderer - mesmo truque de stash da BlockEntity via
// BlockEntityRenderStateEntity (ver BlockEntityRenderDispatcherMixin/BlockEntityRenderStateMixin)
public class TargetRenderer implements BlockEntityRenderer<TargetBlockEntity, TargetRenderer.TargetRenderState> {
    private static final String UPPER_NAME = "target_upper";
    private static final String HEAD_NAME = "head";

    public TargetRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static Optional<BedrockModel> getModel() {
        return InternalAssetLoader.getBedrockModel(InternalAssetLoader.TARGET_MODEL_LOCATION);
    }

    @Override
    public TargetRenderState createRenderState() {
        return new TargetRenderState();
    }

    @Override
    public void extractRenderState(TargetBlockEntity blockEntity, TargetRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, overlay);
        state.partialTick = partialTick;
    }

    @Override
    public void submit(TargetRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        TargetBlockEntity blockEntity = (TargetBlockEntity) ((BlockEntityRenderStateEntity) state).tacz$getBlockEntity();
        if (blockEntity == null) {
            return;
        }
        float partialTick = state.partialTick;
        int combinedLightIn = state.lightCoords;
        getModel().ifPresent(model -> {
            BlockState blockState = blockEntity.getBlockState();
            Direction facing = blockState.getValue(TargetBlock.FACING);
            BedrockPart headModel = model.getNode(HEAD_NAME);
            BedrockPart upperModel = model.getNode(UPPER_NAME);
            float deg = -Mth.lerp(partialTick, blockEntity.oRot, blockEntity.rot);
            upperModel.xRot = (float) Math.toRadians(deg);
            headModel.visible = false;

            poseStack.pushPose();
            poseStack.translate(0.5, 0.225, 0.5);
            poseStack.mulPose(Axis.YN.rotationDegrees(facing.get2DDataValue() * 90));
            poseStack.mulPose(Axis.ZN.rotationDegrees(180));
            poseStack.translate(0, -1.275, 0.0125);
            RenderType renderType = RenderTypes.entityTranslucent(InternalAssetLoader.TARGET_TEXTURE_LOCATION);
            model.render(poseStack, ItemDisplayContext.NONE, collector, renderType, combinedLightIn, 0);
            if (blockEntity.getOwner() != null) {
                poseStack.translate(0, 1.25, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(deg));
                // SkinManager.getInsecureSkinInformation/registerTexture sumiram - a busca de
                // skin agora é assíncrona (createLookup retorna um Supplier com fallback pro
                // skin padrão enquanto carrega, em vez do Map direto de antes)
                Minecraft minecraft = Minecraft.getInstance();
                PlayerSkin playerSkin = minecraft.getSkinManager().createLookup(blockEntity.getOwner(), false).get();
                Identifier skin = playerSkin.body().texturePath();
                headModel.visible = true;
                RenderType skullRenderType = RenderTypes.entityCutout(skin);
                headModel.render(poseStack, ItemDisplayContext.NONE, collector, skullRenderType, combinedLightIn, OverlayTexture.NO_OVERLAY);
            }
            poseStack.popPose();
        });
    }

    @Override
    public int getViewDistance() {
        return RenderConfig.TARGET_RENDER_DISTANCE.get();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static class TargetRenderState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        public float partialTick;
    }
}
