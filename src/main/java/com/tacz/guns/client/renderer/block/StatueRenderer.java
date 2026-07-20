package com.tacz.guns.client.renderer.block;

import cn.sh1rocu.tacz.api.mixin.BlockEntityRenderStateEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.block.TargetBlock;
import com.tacz.guns.block.entity.StatueBlockEntity;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.resource.InternalAssetLoader;
import com.tacz.guns.config.client.RenderConfig;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

// render(blockEntity, partialTick, poseStack, buffer, light, overlay) virou createRenderState()/
// extractRenderState()/submit() na 26.2, igual EntityRenderer. BlockEntityRenderState também não
// guarda a BlockEntity de origem - reusa o mesmo truque de stash (ver
// BlockEntityRenderDispatcherMixin/BlockEntityRenderStateMixin) pra manter a lógica quase igual.
public class StatueRenderer implements BlockEntityRenderer<StatueBlockEntity, StatueRenderer.StatueRenderState> {
    // LightTexture.pack(15, 15) - LightTexture sumiu, o formato do int empacotado não mudou
    private static final int FULL_BRIGHT_LIGHT = 15 << 20 | 15 << 4;

    public StatueRenderer(BlockEntityRendererProvider.Context context) {
    }

    public static Optional<BedrockModel> getModel() {
        return InternalAssetLoader.getBedrockModel(InternalAssetLoader.STATUE_MODEL_LOCATION);
    }

    @Override
    public StatueRenderState createRenderState() {
        return new StatueRenderState();
    }

    @Override
    public void extractRenderState(StatueBlockEntity blockEntity, StatueRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, overlay);
        state.partialTick = partialTick;
    }

    @Override
    public void submit(StatueRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        StatueBlockEntity blockEntity = (StatueBlockEntity) ((BlockEntityRenderStateEntity) state).tacz$getBlockEntity();
        if (blockEntity == null) {
            return;
        }
        int combinedLightIn = state.lightCoords;
        int combinedOverlayIn = 0;
        getModel().ifPresent(model -> {
            Level level = blockEntity.getLevel();
            if (level == null) {
                return;
            }

            poseStack.pushPose();
            {
                BlockState blockState = blockEntity.getBlockState();
                Direction facing = blockState.getValue(TargetBlock.FACING);

                poseStack.translate(0.5, 1.5, 0.5);

                poseStack.mulPose(Axis.YN.rotationDegrees((facing.get2DDataValue() + 2) % 4 * 90));
                poseStack.mulPose(Axis.ZN.rotationDegrees(180));

                RenderType renderType = RenderConfig.BLOCK_ENTITY_TRANSLUCENT.get() ?
                        RenderTypes.entityTranslucent(getTextureLocation()) :
                        RenderTypes.entityCutout(getTextureLocation());
                model.render(poseStack, ItemDisplayContext.NONE, collector, renderType, combinedLightIn, combinedOverlayIn);

                poseStack.scale(0.5f, 0.5f, 0.5f);
                poseStack.translate(0, -0.875, -1.2);
                poseStack.mulPose(Axis.ZP.rotationDegrees(180));

                double offset = Math.sin(Util.getMillis() / 500.0) * 0.1;
                poseStack.translate(0, offset, 0);

                ItemStack stack = blockEntity.getGunItem();

                // ItemRenderer.renderStatic(...) sumiu - vira ItemStackRenderState preenchido
                // por ItemModelResolver e desenhado via submit()
                ItemStackRenderState itemRenderState = new ItemStackRenderState();
                Minecraft.getInstance().getItemModelResolver().updateForTopItem(itemRenderState, stack, ItemDisplayContext.FIXED, level, null, 0);
                itemRenderState.submit(poseStack, collector, FULL_BRIGHT_LIGHT, EntityRenderState.NO_OUTLINE, -1);
            }
            poseStack.popPose();
        });
    }

    public static Identifier getTextureLocation() {
        return InternalAssetLoader.STATUE_TEXTURE_LOCATION;
    }

    @Override
    public int getViewDistance() {
        return RenderConfig.TARGET_RENDER_DISTANCE.get();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public boolean shouldRender(StatueBlockEntity pBlockEntity, Vec3 pCameraPos) {
        return Vec3.atCenterOf(pBlockEntity.getBlockPos().above()).closerThan(pCameraPos, this.getViewDistance());
    }

    public static class StatueRenderState extends BlockEntityRenderState {
        public float partialTick;
    }
}
