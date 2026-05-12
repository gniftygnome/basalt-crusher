package net.gnomecraft.basaltcrusher.crusher;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.MenuProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.InteractionResult;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class BasaltCrusherBlock extends BaseEntityBlock {
    enum CrushingState implements StringRepresentable {
        EMPTY, IDLE, OPEN, OPENISH, CLOSEDISH, CLOSED;

        public String getSerializedName() {
            return this.toString().toLowerCase();
        }
    }
    static final EnumProperty<CrushingState> CRUSHING_STATE = EnumProperty.create("crushing_state", CrushingState.class);
    static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public BasaltCrusherBlock(Properties settings) {
        super(settings);

        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(CRUSHING_STATE, CrushingState.EMPTY));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        //noinspection ConstantConditions
        return null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BasaltCrusherEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BasaltCrusher.BASALT_CRUSHER_ENTITY, BasaltCrusherEntity::tick);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide()) {
            this.openContainer(world, pos, player);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof BasaltCrusherEntity basaltCrusherEntity) {
            basaltCrusherEntity.dropExperience(world, player);
        }

        return super.playerWillDestroy(world, pos, state, player);
    }

    private void openContainer(Level world, BlockPos blockPos, Player playerEntity) {
        BlockEntity blockEntity = world.getBlockEntity(blockPos);

        if (blockEntity instanceof BasaltCrusherEntity) {
            playerEntity.openMenu((MenuProvider) blockEntity);
            // TODO: playerEntity.increaseStat(Stats.INTERACT_WITH_CRUSHER, 1);
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
        if (state.getValue(CRUSHING_STATE) != CrushingState.EMPTY && state.getValue(CRUSHING_STATE) != CrushingState.IDLE) {
            double x = (double) pos.getX() + 0.5D;
            double y = (double) pos.getY();
            double z = (double) pos.getZ() + 0.5D;

            if (random.nextDouble() < 0.015D) {
                // If the crusher is running, play its sound about once every minute.
                world.playLocalSound(x, y, z, BasaltCrusher.BASALT_CRUSHER_SOUND_EVENT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof BasaltCrusherEntity basaltCrusherEntity) {
            basaltCrusherEntity.scatterInventory(world, pos);
            world.updateNeighbourForOutputSignal(pos, this);
        }

        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, Direction direction) {
        BlockEntity entity = world.getBlockEntity(pos);

        if (entity instanceof BasaltCrusherEntity basaltCrusherEntity) {
            return basaltCrusherEntity.calculateComparatorOutput();
        }

        return 0;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CRUSHING_STATE);
    }
}