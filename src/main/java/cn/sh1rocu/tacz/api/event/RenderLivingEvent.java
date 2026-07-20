package cn.sh1rocu.tacz.api.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.LivingEntity;

// LivingEntityRenderer virou <T,S,M> (S = LivingEntityRenderState) e render(...) virou
// createRenderState/extractRenderState/submit - esse evento não usava o renderer nem o model em
// nenhum lugar de verdade (só RenderHeadShotAABB, que só quer entity/poseStack/luz), então
// simplificado pra não genérico, trocando MultiBufferSource por SubmitNodeCollector.
@Environment(EnvType.CLIENT)
public abstract class RenderLivingEvent extends BaseEvent {
    private final LivingEntity entity;
    private final float partialTick;
    private final PoseStack poseStack;
    private final SubmitNodeCollector collector;
    private final int packedLight;

    public static final Event<PostCallback> POST = EventFactory.createArrayBacked(PostCallback.class, callbacks -> event -> {
        for (PostCallback callback : callbacks) {
            callback.post(event);
        }
    });

    public interface PostCallback {
        void post(Post event);
    }

    protected RenderLivingEvent(LivingEntity entity, float partialTick, PoseStack poseStack,
                                SubmitNodeCollector collector, int packedLight) {
        this.entity = entity;
        this.partialTick = partialTick;
        this.poseStack = poseStack;
        this.collector = collector;
        this.packedLight = packedLight;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public float getPartialTick() {
        return partialTick;
    }

    public PoseStack getPoseStack() {
        return poseStack;
    }

    public SubmitNodeCollector getCollector() {
        return collector;
    }

    public int getPackedLight() {
        return packedLight;
    }

    public static class Post extends RenderLivingEvent {
        public Post(LivingEntity entity, float partialTick, PoseStack poseStack, SubmitNodeCollector collector, int packedLight) {
            super(entity, partialTick, poseStack, collector, packedLight);
        }
    }
}
