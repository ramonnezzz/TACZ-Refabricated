package com.tacz.guns.client.event;

import cn.sh1rocu.tacz.api.event.RenderLivingEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.config.util.HeadShotAABBConfigRead;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

@Environment(EnvType.CLIENT)
public class RenderHeadShotAABB {
    public static void onRenderEntity(RenderLivingEvent.Post event) {
        // EntityRenderDispatcher.shouldRenderHitBoxes() sumiu sem substituto óbvio - fica só
        // com o próprio toggle de debug do mod como gate
        if (!RenderConfig.HEAD_SHOT_DEBUG_HITBOX.get()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        AABB aabb = HeadShotAABBConfigRead.getAABB(entityId);
        if (aabb == null) {
            float width = entity.getBbWidth();
            float eyeHeight = entity.getEyeHeight();
            // 扩张 0.01，避免和原版显示重合
            aabb = new AABB(-width / 2, eyeHeight - 0.25, -width / 2, width / 2, eyeHeight + 0.25, width / 2).inflate(0.01);
        }
        AABB box = aabb;
        // LevelRenderer.renderLineBox sumiu - desenha as 12 arestas manualmente via addVertex,
        // já que a API de VertexConsumer mudou pra addVertex/setColor (ver rework de rendering)
        event.getCollector().submitCustomGeometry(event.getPoseStack(), RenderTypes.lines(), (pose, consumer) ->
                renderLineBox(pose, consumer, box, ARGB.colorFromFloat(1.0F, 1.0F, 1.0F, 0.0F)));
    }

    private static void renderLineBox(PoseStack.Pose pose, VertexConsumer consumer, AABB box, int color) {
        float minX = (float) box.minX, minY = (float) box.minY, minZ = (float) box.minZ;
        float maxX = (float) box.maxX, maxY = (float) box.maxY, maxZ = (float) box.maxZ;
        float[][] edges = {
                {minX, minY, minZ, maxX, minY, minZ}, {minX, minY, minZ, minX, maxY, minZ}, {minX, minY, minZ, minX, minY, maxZ},
                {maxX, maxY, maxZ, minX, maxY, maxZ}, {maxX, maxY, maxZ, maxX, minY, maxZ}, {maxX, maxY, maxZ, maxX, maxY, minZ},
                {minX, maxY, minZ, maxX, maxY, minZ}, {minX, maxY, minZ, minX, maxY, maxZ},
                {maxX, minY, minZ, maxX, maxY, minZ}, {maxX, minY, minZ, maxX, minY, maxZ},
                {minX, minY, maxZ, maxX, minY, maxZ}, {minX, minY, maxZ, minX, maxY, maxZ}
        };
        for (float[] e : edges) {
            consumer.addVertex(pose, e[0], e[1], e[2]).setColor(color);
            consumer.addVertex(pose, e[3], e[4], e[5]).setColor(color);
        }
    }
}
