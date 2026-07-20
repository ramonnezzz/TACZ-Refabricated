package com.tacz.guns.client.renderer.entity;

import cn.sh1rocu.tacz.api.mixin.EntityRenderStateEntity;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.resource.InternalAssetLoader;
import com.tacz.guns.entity.TargetMinecart;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.Optional;

// MinecartRenderer (a classe concreta da vanilla) tem tipo fixo <AbstractMinecart,
// MinecartRenderState> agora - pra um minecart customizado, estende AbstractMinecartRenderer
// direto (que continua genérico), igual TntMinecartRenderer faz na própria vanilla.
@Environment(EnvType.CLIENT)
public class TargetMinecartRenderer extends AbstractMinecartRenderer<TargetMinecart, MinecartRenderState> {
    private static final String HEAD_NAME = "head";
    private static final String HEAD_2_NAME = "head2";

    public TargetMinecartRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, ModelLayers.TNT_MINECART);
        this.shadowRadius = 0.25F;
    }

    public static Optional<BedrockModel> getModel() {
        return InternalAssetLoader.getBedrockModel(InternalAssetLoader.TARGET_MINECART_MODEL_LOCATION);
    }

    // getTextureLocation(T) removido: não existe mais em EntityRenderer na 26.2

    @Override
    public MinecartRenderState createRenderState() {
        return new MinecartRenderState();
    }

    @Override
    protected void submitMinecartContents(MinecartRenderState state, BlockModelRenderState blockModelRenderState, PoseStack stack, SubmitNodeCollector collector, int packedLight) {
        TargetMinecart targetMinecart = (TargetMinecart) ((EntityRenderStateEntity) state).tacz$getEntity();
        if (targetMinecart == null) {
            return;
        }
        getModel().ifPresent(model -> {
            BedrockPart headModel = model.getNode(HEAD_NAME);
            BedrockPart head2Model = model.getNode(HEAD_2_NAME);
            headModel.visible = false;
            head2Model.visible = false;

            stack.pushPose();
            stack.translate(0.5, 1.875, 0.5);
            stack.scale(1.5f, 1.5f, 1.5f);
            stack.mulPose(Axis.ZN.rotationDegrees(180));
            stack.mulPose(Axis.YN.rotationDegrees(90));
            model.render(stack, ItemDisplayContext.NONE, collector, RenderTypes.entityTranslucent(InternalAssetLoader.TARGET_MINECART_TEXTURE_LOCATION, true), packedLight, OverlayTexture.NO_OVERLAY);
            GameProfile gameProfile = targetMinecart.getGameProfile();
            if (gameProfile != null) {
                stack.translate(0, 1, -4.5 / 16d);
                Minecraft minecraft = Minecraft.getInstance();
                // SkinManager.getInsecureSkinInformation/registerTexture sumiram - a busca de
                // skin agora é assíncrona (createLookup retorna um Supplier com fallback)
                PlayerSkin playerSkin = minecraft.getSkinManager().createLookup(gameProfile, false).get();
                Identifier skin = playerSkin.body().texturePath();

                headModel.visible = true;
                headModel.render(stack, ItemDisplayContext.NONE, collector, RenderTypes.entityTranslucent(skin, true), packedLight, OverlayTexture.NO_OVERLAY);

                head2Model.visible = true;
                stack.translate(0, 0, 0.01);
                head2Model.render(stack, ItemDisplayContext.NONE, collector, RenderTypes.entityTranslucent(skin, true), packedLight, OverlayTexture.NO_OVERLAY);
            }
            stack.popPose();
        });
    }
}
