package net.gnomecraft.basaltcrusher.grizzly;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class GrizzlyBlock extends BlockWithEntity {
    static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    private static final VoxelShape GRIZZLY_SHAPE_NORTH = VoxelShapes.union(
            // base, front, left, right, back
            Block.createCuboidShape(0, 0, 0, 16, 4, 16),
            Block.createCuboidShape(0, 4, 0, 16, 8, 1),
            Block.createCuboidShape(0, 4, 1, 1, 16, 15),
            Block.createCuboidShape(15, 4, 1, 16, 16, 15),
            Block.createCuboidShape(0, 4, 15, 16, 16, 16),
            // Sloped component (slats) ... can't believe this is The Way but look at the Lectern...
            Block.createCuboidShape(1, 4,  4, 15, 6, 6),
            Block.createCuboidShape(1, 4,  6, 15, 8, 8),
            Block.createCuboidShape(1, 4,  8, 15, 10, 10),
            Block.createCuboidShape(1, 4, 10, 15, 12, 12),
            Block.createCuboidShape(1, 4, 12, 15, 14, 14),
            Block.createCuboidShape(1, 4, 14, 15, 16, 16)
    ).simplify();
    private static final VoxelShape GRIZZLY_SHAPE_EAST  = rotateShape(Direction.NORTH, Direction.EAST,  GRIZZLY_SHAPE_NORTH);
    private static final VoxelShape GRIZZLY_SHAPE_SOUTH = rotateShape(Direction.NORTH, Direction.SOUTH, GRIZZLY_SHAPE_NORTH);
    private static final VoxelShape GRIZZLY_SHAPE_WEST  = rotateShape(Direction.NORTH, Direction.WEST,  GRIZZLY_SHAPE_NORTH);

    public GrizzlyBlock(AbstractBlock.Settings settings) {
        super(settings);

        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GrizzlyEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, BasaltCrusher.GRIZZLY_ENTITY, GrizzlyEntity::tick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            this.openContainer(world, pos, player);
        }

        return ActionResult.SUCCESS;
    }

    private void openContainer(World world, BlockPos blockPos, PlayerEntity playerEntity) {
        BlockEntity blockEntity = world.getBlockEntity(blockPos);

        if (blockEntity instanceof GrizzlyEntity) {
            playerEntity.openHandledScreen((NamedScreenHandlerFactory) blockEntity);
            // TODO: playerEntity.increaseStat(Stats.INTERACT_WITH_GRIZZLY, 1);
        }
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState blockState, LootContext.Builder lootContext$Builder) {
        ArrayList<ItemStack> dropList = new ArrayList<>();
        dropList.add(new ItemStack(this));
        return dropList;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof GrizzlyEntity) {
                ((GrizzlyEntity) blockEntity).scatterInventory(world, pos);
                world.updateComparators(pos,this);
            }

            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        BlockEntity entity = world.getBlockEntity(pos);

        if (entity instanceof GrizzlyEntity) {
            return ((GrizzlyEntity) entity).calculateComparatorOutput();
        }

        return 0;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH -> GRIZZLY_SHAPE_NORTH;
            case EAST  -> GRIZZLY_SHAPE_EAST;
            case SOUTH -> GRIZZLY_SHAPE_SOUTH;
            case WEST  -> GRIZZLY_SHAPE_WEST;
            default    -> VoxelShapes.fullCube();
        };
    }

    public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, VoxelShapes.empty()};

        int times = (to.getHorizontal() - from.getHorizontal() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = VoxelShapes.union(buffer[1], VoxelShapes.cuboid(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = VoxelShapes.empty();
        }

        return buffer[0].simplify();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}