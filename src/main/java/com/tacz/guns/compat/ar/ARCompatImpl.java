package com.tacz.guns.compat.ar;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

// AcceleratedRendering ainda não tem build pra 26.2 (ver build.gradle) - todo corpo aqui é
// inalcançável em runtime, já que ARCompat só chama esses métodos quando ARCompat.LOADED é
// true, e isso exige o mod instalado. Vira stub sem as classes reais da AR pra compilar.
public class ARCompatImpl {

	public static boolean shouldAccelerate() {
		return false;
	}

	public static boolean isAccelerated(VertexConsumer vertexConsumer) {
		return false;
	}

	public static void setRenderingLevel() {
	}

	public static void resetRenderingLevel() {
	}

	public static void setRenderLayer(int layer) {
	}

	public static void setRenderBeforeFunction(Runnable runnable) {
	}

	public static void setRenderAfterFunction(Runnable runnable) {
	}

	public static void resetRenderLayer() {
	}

	public static void resetRenderBeforeFunction() {
	}

	public static void resetRenderAfterFunction() {
	}

	public static void disableAcceleration() {
	}

	public static void resetAcceleration() {
	}

	public static void renderLaser(
			VertexConsumer vertexConsumer,
			float z,
			float width,
			boolean fadeOut,
			PoseStack poseStack,
			int color
	) {
	}
}
