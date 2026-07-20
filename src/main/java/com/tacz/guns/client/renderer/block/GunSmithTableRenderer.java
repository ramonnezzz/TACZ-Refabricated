package com.tacz.guns.client.renderer.block;

import cn.sh1rocu.tacz.api.mixin.BlockEntityRenderStateEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IBlock;
import com.tacz.guns.block.AbstractGunSmithTableBlock;
import com.tacz.guns.block.entity.GunSmithTableBlockEntity;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.resource.index.ClientBlockIndex;
import com.tacz.guns.config.client.RenderConfig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

// render(...) virou createRenderState()/extractRenderState()/submit() na 26.2, mesmo padrão de
// StatueRenderer/TargetRenderer
public class GunSmithTableRenderer implements BlockEntityRenderer<GunSmithTableBlockEntity, BlockEntityRenderState> {
    public GunSmithTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    public Optional<ClientBlockIndex> getIndex(GunSmithTableBlockEntity blockEntity) {
        Identifier id = blockEntity.getId();
        if (id == null || id.equals(DefaultAssets.EMPTY_BLOCK_ID)) {
            return Optional.empty();
        }
        return TimelessAPI.getClientBlockIndex(id);
    }

    public static Optional<ClientBlockIndex> getIndex(ItemStack stack) {
        if (stack.getItem() instanceof IBlock iBlock) {
            Identifier id = iBlock.getBlockId(stack);
            if (id.equals(DefaultAssets.EMPTY_BLOCK_ID)) {
                return Optional.empty();
            }
            return TimelessAPI.getClientBlockIndex(id);
        }
        return Optional.empty();
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void extractRenderState(GunSmithTableBlockEntity blockEntity, BlockEntityRenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, overlay);
        ((BlockEntityRenderStateEntity) state).tacz$setBlockEntity(blockEntity);
    }

    @Override
    public void submit(BlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        GunSmithTableBlockEntity blockEntity = (GunSmithTableBlockEntity) ((BlockEntityRenderStateEntity) state).tacz$getBlockEntity();
        if (blockEntity == null) {
            return;
        }
        int combinedLightIn = state.lightCoords;
        getIndex(blockEntity).ifPresent(index -> {
            BedrockModel model = index.getModel();
            Identifier texture = index.getTexture();
            if (model == null) {
                return;
            }
            BlockState blockState = blockEntity.getBlockState();
            if (blockState.getBlock() instanceof AbstractGunSmithTableBlock block) {
                if (!block.isRoot(blockState)) {
                    return;
                }
                Direction facing = blockState.getValue(AbstractGunSmithTableBlock.FACING);
                poseStack.pushPose();
                poseStack.translate(0.5, 1.5, 0.5);
                poseStack.mulPose(Axis.ZN.rotationDegrees(180));
                poseStack.mulPose(Axis.YN.rotationDegrees(block.parseRotation(facing)));
                RenderType renderType = RenderConfig.BLOCK_ENTITY_TRANSLUCENT.get() ?
                        RenderTypes.entityTranslucent(texture) :
                        RenderTypes.entityCutout(texture);
                model.render(poseStack, ItemDisplayContext.NONE, collector, renderType, combinedLightIn, 0);
                poseStack.popPose();
            }
        });
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
