package com.tacz.guns.client.renderer.entity;

import cn.sh1rocu.tacz.api.mixin.EntityRenderStateEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.model.BedrockAmmoModel;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.InternalAssetLoader;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Optional;

// render(entity, yaw, partialTicks, poseStack, buffer, light) virou createRenderState()/
// extractRenderState()/submit() na 26.2 (rework extract/submit). Em vez de reextrair cada campo
// individualmente pro novo RenderState, guarda a bullet e o partialTick direto nele (via
// EntityRenderStateEntity, ver EntityRendererMixin) e reusa a lógica de desenho quase inalterada
// dentro de submit() - mais simples que espalhar toda a interpolação em extractRenderState.
public class EntityBulletRenderer extends EntityRenderer<EntityKineticBullet, EntityBulletRenderer.BulletRenderState> {
    public EntityBulletRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    public static Optional<BedrockModel> getModel() {
        return InternalAssetLoader.getBedrockModel(InternalAssetLoader.DEFAULT_BULLET_MODEL);
    }

    @Override
    public BulletRenderState createRenderState() {
        return new BulletRenderState();
    }

    @Override
    public void extractRenderState(EntityKineticBullet bullet, BulletRenderState state, float partialTick) {
        super.extractRenderState(bullet, state, partialTick);
        state.partialTick = partialTick;
    }

    @Override
    public void submit(BulletRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        EntityKineticBullet bullet = (EntityKineticBullet) ((EntityRenderStateEntity) state).tacz$getEntity();
        if (bullet == null) {
            return;
        }
        float partialTicks = state.partialTick;
        int packedLight = state.lightCoords;
        Identifier gunId = bullet.getGunId();
        Identifier gunDisplayId = bullet.getGunDisplayId();
        Optional<GunDisplayInstance> display = TimelessAPI.getGunDisplay(gunDisplayId, gunId);
        if (display.isEmpty()) {
            return;
        }
        float @Nullable [] tracerColor = bullet.getTracerColorOverride().orElse(display.get().getTracerColor());
        Identifier ammoId = bullet.getAmmoId();
        TimelessAPI.getClientAmmoIndex(ammoId).ifPresent(ammoIndex -> {
            BedrockAmmoModel ammoEntityModel = ammoIndex.getAmmoEntityModel();
            Identifier textureLocation = ammoIndex.getAmmoEntityTextureLocation();
            if (ammoEntityModel != null && textureLocation != null) {
                poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, bullet.yRotO, bullet.getYRot()) - 180.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, bullet.xRotO, bullet.getXRot())));
                poseStack.pushPose();
                poseStack.translate(0, 1.5, 0);
                poseStack.scale(-1, -1, 1);
                ammoEntityModel.render(poseStack, ItemDisplayContext.GROUND, collector, RenderTypes.entityTranslucent(textureLocation, true), packedLight, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }

            // 曳光弹发光
            if (bullet.isTracerAmmo()) {
                float[] actualTracerColor = Objects.requireNonNullElse(tracerColor, ammoIndex.getTracerColor());
                renderTracerAmmo(bullet, actualTracerColor, partialTicks, poseStack, collector, packedLight);
            }
        });
    }

    public void renderTracerAmmo(EntityKineticBullet bullet, float[] tracerColor, float partialTicks, PoseStack poseStack, SubmitNodeCollector collector, int packedLight) {
        getModel().ifPresent(model -> {
            Entity shooter = bullet.getOwner();
            if (shooter == null) {
                return;
            }
            boolean isFirstPerson = this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && shooter instanceof LocalPlayer;
            if (isFirstPerson && !RenderConfig.FIRST_PERSON_BULLET_TRACER_ENABLE.get()) {
                return;
            }
            poseStack.pushPose();
            {
                float width = 0.005f;
                Vec3 bulletPosition = bullet.getPosition(partialTicks);
                double trailLength = 0.85 * bullet.getDeltaMovement().length();
                double disToEye = bulletPosition.distanceTo(shooter.getEyePosition(partialTicks));
                trailLength = Math.min(trailLength, disToEye * 0.8);

                if (isFirstPerson) {
                    // 第一人称渲染自己的曳光弹的时候需要应用偏移
                    Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
                    Vector3f offset = bullet.getFirstPersonRenderOffset();
                    if (offset == null) {
                        // GunItemRendererWrapper (simplebedrockmodel-fabric, sem build 26.2) não
                        // existe mais - sem o offset real do cano, usa a origem da câmera
                        offset = new Vector3f(0, 0, 0);
                        bullet.setCameraXRot(camera.xRot());
                        bullet.setCameraYRot(camera.yRot());
                        bullet.setFirstPersonRenderOffset(offset);
                    }
                    // 按照生存时间减少曳光弹的偏移，避免渲染位置距离落点太远
                    double offsetReducer = Math.max(0, (50 - disToEye)) / 50;
                    // 摄像机旋转
                    poseStack.mulPose(Axis.YN.rotationDegrees(bullet.getCameraYRot() + 180f));
                    poseStack.mulPose(Axis.XN.rotationDegrees(bullet.getCameraXRot()));
                    // 应用偏移
                    poseStack.translate(offset.x * offsetReducer, offset.y * offsetReducer, offset.z * offsetReducer);
                    // 逆转摄像机旋转
                    poseStack.mulPose(Axis.XP.rotationDegrees(bullet.getCameraXRot()));
                    poseStack.mulPose(Axis.YP.rotationDegrees(bullet.getCameraYRot() + 180f));
                }
                // 说是 override 其实默认值是 1
                // 所以这里直接乘也没关系
                width *= bullet.getTracerSizeOverride();
                width *= (float) Math.max(1.0, disToEye / 3.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, bullet.yRotO, bullet.getYRot()) - 180.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, bullet.xRotO, bullet.getXRot())));
                poseStack.translate(0, isFirstPerson ? 0 : -0.2, trailLength / 2.0);
                poseStack.scale(width, width, (float) trailLength);
                // 距离两格外才渲染，只在前 5 tick 判定
                double bulletDistance = bulletPosition.distanceTo(shooter.getEyePosition());
                if (bullet.tickCount >= 5 || bulletDistance > 2) {
                    RenderType type = RenderTypes.energySwirl(InternalAssetLoader.DEFAULT_BULLET_TEXTURE, 15, 15);
                    model.render(poseStack, ItemDisplayContext.NONE, collector, type, packedLight, OverlayTexture.NO_OVERLAY,
                            tracerColor[0], tracerColor[1], tracerColor[2], 1);
                }
            }
            poseStack.popPose();
        });
    }

    @Override
    protected int getBlockLightLevel(@NotNull EntityKineticBullet entityBullet, @NotNull BlockPos blockPos) {
        return 15;
    }

    @Override
    public boolean shouldRender(EntityKineticBullet bullet, Frustum camera, double pCamX, double pCamY, double pCamZ) {
        AABB aabb = bullet.getBoundingBox().inflate(0.5);
        if (aabb.hasNaN() || aabb.getSize() == 0) {
            aabb = new AABB(bullet.getX() - 2.0, bullet.getY() - 2.0, bullet.getZ() - 2.0, bullet.getX() + 2.0, bullet.getY() + 2.0, bullet.getZ() + 2.0);
        }
        return camera.isVisible(aabb);
    }

    // getTextureLocation(T) removido: não existe mais em EntityRenderer na 26.2

    public static class BulletRenderState extends EntityRenderState {
        public float partialTick;
    }
}
