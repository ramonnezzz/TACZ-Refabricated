package com.tacz.guns.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;

public interface IFunctionalRenderer {
    void render(PoseStack poseStack, SubmitNodeCollector collector, RenderType renderType, ItemDisplayContext transformType, int light, int overlay);
}
