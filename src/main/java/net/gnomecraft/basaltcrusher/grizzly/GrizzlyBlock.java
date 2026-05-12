package net.gnomecraft.basaltcrusher.grizzly;

import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class GrizzlyBlock extends BaseEntityBlock {
    static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape GRIZZLY_SHAPE_NORTH = Shapes.or(
            // base, front, left, right, back
            Block.box(0, 0, 0, 16, 4, 16),
            Block.box(0, 4, 0, 16, 8, 1),
            Block.box(0, 4, 1, 1, 16, 15),
            Block.box(15, 4, 1, 16, 16, 15),
            Block.box(0, 4, 15, 16, 16, 16),
            // Sloped component (slats) ... can't believe this is The Way but look at the Lectern...
            Block.box(1, 4,  4, 15, 6, 6),
            Block.box(1, 4,  6, 15, 8, 8),
            Block.box(1, 4,  8, 15, 10, 10),
            Block.box(1, 4, 10, 15, 12, 12),
            Block.box(1, 4, 12, 15, 14, 14),
            Block.box(1, 4, 14, 15, 16, 16)
    ).optimize();
    private static final VoxelShape GRIZZLY_SHAPE_EAST  = rotateShape(Direction.NORTH, Direction.EAST,  GRIZZLY_SHAPE_NORTH);
    private static final VoxelShape GRIZZLY_SHAPE_SOUTH = rotateShape(Direction.NORTH, Direction.SOUTH, GRIZZLY_SHAPE_NORTH);
    private static final VoxelShape GRIZZLY_SHAPE_WEST  = rotateShape(Direction.NORTH, Direction.WEST,  GRIZZLY_SHAPE_NORTH);

    public GrizzlyBlock(BlockBehaviour.Properties settings) {
        super(settings);

        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        //noinspection ConstantConditions
        return null;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GrizzlyEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BasaltCrusher.GRIZZLY_ENTITY, GrizzlyEntity::tick);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            this.openContainer(level, pos, player);
        }

        return InteractionResult.SUCCESS;
    }

    private void openContainer(Level level, BlockPos blockPos, Player playerEntity) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);

        if (blockEntity instanceof GrizzlyEntity) {
            playerEntity.openMenu((MenuProvider) blockEntity);
            // TODO: playerEntity.increaseStat(Stats.INTERACT_WITH_GRIZZLY, 1);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof GrizzlyEntity grizzlyEntity) {
            grizzlyEntity.scatterInventory(level, pos);
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

        if (entity instanceof GrizzlyEntity grizzlyEntity) {
            return grizzlyEntity.calculateComparatorOutput();
        }

        return 0;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> GRIZZLY_SHAPE_NORTH;
            case EAST  -> GRIZZLY_SHAPE_EAST;
            case SOUTH -> GRIZZLY_SHAPE_SOUTH;
            case WEST  -> GRIZZLY_SHAPE_WEST;
            default    -> Shapes.block();
        };
    }

    public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};

        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }

        return buffer[0].optimize();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}