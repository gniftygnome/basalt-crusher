package net.gnomecraft.basaltcrusher.crusher;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class BasaltCrusherBlock extends BlockWithEntity {
    enum CrushingState implements StringIdentifiable {
        EMPTY, IDLE, OPEN, OPENISH, CLOSEDISH, CLOSED;

        public String asString() {
            return this.toString().toLowerCase();
        }
    }
    static final EnumProperty<CrushingState> CRUSHING_STATE = EnumProperty.of("crushing_state", CrushingState.class);
    static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public BasaltCrusherBlock(Settings settings) {
        super(settings);

        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(CRUSHING_STATE, CrushingState.EMPTY));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BasaltCrusherEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, BasaltCrusher.BASALT_CRUSHER_ENTITY, BasaltCrusherEntity::tick);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            this.openContainer(world, pos, player);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof BasaltCrusherEntity) {
            ((BasaltCrusherEntity) blockEntity).dropExperience(player);
        }

        return super.onBreak(world, pos, state, player);
    }

    private void openContainer(World world, BlockPos blockPos, PlayerEntity playerEntity) {
        BlockEntity blockEntity = world.getBlockEntity(blockPos);

        if (blockEntity instanceof BasaltCrusherEntity) {
            playerEntity.openHandledScreen((NamedScreenHandlerFactory) blockEntity);
            // TODO: playerEntity.increaseStat(Stats.INTERACT_WITH_CRUSHER, 1);
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(CRUSHING_STATE) != CrushingState.EMPTY && state.get(CRUSHING_STATE) != CrushingState.IDLE) {
            double x = (double) pos.getX() + 0.5D;
            double y = (double) pos.getY();
            double z = (double) pos.getZ() + 0.5D;

            if (random.nextDouble() < 0.015D) {
                // If the crusher is running, play its sound about once every minute.
                world.playSound(x, y, z, BasaltCrusher.BASALT_CRUSHER_SOUND_EVENT, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
            }
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);

            if (blockEntity instanceof BasaltCrusherEntity) {
                ((BasaltCrusherEntity) blockEntity).scatterInventory(world, pos);
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

        if (entity instanceof BasaltCrusherEntity) {
            return ((BasaltCrusherEntity) entity).calculateComparatorOutput();
        }

        return 0;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, CRUSHING_STATE);
    }
}