package com.tacz.guns.block;

import cn.sh1rocu.tacz.api.extension.IBlockExtension;
import com.mojang.serialization.MapCodec;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.block.entity.StatueBlockEntity;
import com.tacz.guns.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class StatueBlock extends BaseEntityBlock implements IBlockExtension {
    // BaseEntityBlock#codec() virou abstrato na 26.2 (registro de bloco por codec/datapack)
    private static final MapCodec<StatueBlock> CODEC = simpleCodec(StatueBlock::new);

    @Override
    protected MapCodec<? extends StatueBlock> codec() {
        return CODEC;
    }

    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    // DirectionProperty sumiu como classe própria - HORIZONTAL_FACING agora é só EnumProperty<Direction>
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public StatueBlock() {
        this(defaultProperties());
    }

    public StatueBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(FACING, Direction.NORTH)
        );
    }

    public static Properties defaultProperties() {
        return Properties.of().sound(SoundType.STONE).strength(2.0F, 3.0F).noOcclusion().pushReaction(PushReaction.DESTROY);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return state.getValue(HALF).equals(DoubleBlockHalf.LOWER) && level.isClientSide() ? createTickerHelper(blockEntityType, ModBlocks.STATUE_BE, StatueBlockEntity::clientTick) : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return pState.getValue(HALF) == DoubleBlockHalf.LOWER ? new StatueBlockEntity(pPos, pState) : null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState pState, Level level, BlockPos pos, Player player, InteractionHand pHand, BlockHitResult pHit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            if (pState.getValue(HALF) == DoubleBlockHalf.UPPER) {
                pos = pos.below();
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof StatueBlockEntity statueBlockEntity) {
                if (stack.getItem() instanceof IGun) {
                    statueBlockEntity.setGun(stack);
                    stack.shrink(1);
                    return InteractionResult.SUCCESS;
                }

                if (stack.isEmpty()) {
                    statueBlockEntity.dropItem();
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite();
        BlockPos clickedPos = context.getClickedPos();
        BlockPos above = clickedPos.above();
        Level level = context.getLevel();
        if (level.getBlockState(above).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(above)) {
            return this.defaultBlockState().setValue(FACING, direction);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(world, pos, state, placer, stack);
        if (!world.isClientSide()) {
            BlockPos above = pos.above();
            world.setBlock(above, state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
            world.updateNeighborsAt(pos, Blocks.AIR, null);
            state.updateNeighbourShapes(world, pos, Block.UPDATE_ALL);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos currentPos, Direction facing, BlockPos facingPos, BlockState facingState, RandomSource random) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (facing.getAxis() == Direction.Axis.Y) {
            if (half.equals(DoubleBlockHalf.LOWER) && facing == Direction.UP || half.equals(DoubleBlockHalf.UPPER) && facing == Direction.DOWN) {
                // 拆一半另外一半跟着没
                if (!facingState.is(this)) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
        }

        return state;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        // onRemove(oldState,Level,pos,newState,bool) virou affectNeighborsAfterRemoval - o
        // framework agora só chama isso quando o bloco de fato mudou de tipo, sem precisar
        // comparar contra o newState manualmente como antes.
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof StatueBlockEntity statueBlockEntity) {
            statueBlockEntity.dropItem();
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE; // ENTITYBLOCK_ANIMATED sumiu do enum (só sobrou INVISIBLE/MODEL) - o renderer de block entity roda de qualquer forma
    }

    @Override
    public void tacz$onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        IBlockExtension.super.tacz$onBlockExploded(state, level, pos, explosion);
    }
}
