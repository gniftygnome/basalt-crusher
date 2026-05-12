package net.gnomecraft.basaltcrusher.mill;

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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class GravelMillBlock extends BaseEntityBlock {
    static final IntegerProperty MILL_STATE = IntegerProperty.create("mill_state",0, 21);
    static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public GravelMillBlock(Properties settings) {
        super(settings);

        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(MILL_STATE, 21));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        //noinspection ConstantConditions
        return null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GravelMillEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BasaltCrusher.GRAVEL_MILL_ENTITY, GravelMillEntity::tick);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            this.openContainer(level, pos, player);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof GravelMillEntity gravelMillEntity) {
            gravelMillEntity.dropExperience(level, player);
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    private void openContainer(Level level, BlockPos blockPos, Player playerEntity) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);

        if (blockEntity instanceof GravelMillEntity) {
            playerEntity.openMenu((MenuProvider) blockEntity);
            // TODO: playerEntity.increaseStat(Stats.INTERACT_WITH_MILL, 1);
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(MILL_STATE) < 20) {
            double x = (double) pos.getX() + 0.5D;
            double y = (double) pos.getY();
            double z = (double) pos.getZ() + 0.5D;

            if (random.nextDouble() < 0.015D) {
                // If the mill is running, play its sound about once every minute.
                level.playLocalSound(x, y, z, BasaltCrusher.GRAVEL_MILL_SOUND_EVENT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof GravelMillEntity gravelMillEntity) {
            gravelMillEntity.scatterInventory(level, pos);
            level.updateNeighbourForOutputSignal(pos, this);
        }

        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        BlockEntity entity = level.getBlockEntity(pos);

        if (entity instanceof GravelMillEntity gravelMillEntity) {
            return gravelMillEntity.calculateComparatorOutput();
        }

        return 0;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MILL_STATE);
    }
}