package cn.sh1rocu.tacz.api.mixin;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityRenderStateEntity {
    BlockEntity tacz$getBlockEntity();

    void tacz$setBlockEntity(BlockEntity blockEntity);
}
