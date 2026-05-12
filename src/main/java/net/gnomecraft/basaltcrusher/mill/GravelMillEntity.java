package net.gnomecraft.basaltcrusher.mill;

import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.gnomecraft.basaltcrusher.utils.BasaltCrusherInventory;
import net.gnomecraft.basaltcrusher.utils.IOTypeMatchers;
import net.gnomecraft.basaltcrusher.utils.TerrestriaIntegration;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;

import static net.gnomecraft.basaltcrusher.mill.GravelMillBlock.MILL_STATE;

@NullMarked
public class GravelMillEntity extends BlockEntity implements MenuProvider {
    private int millState;
    private final EnumMap<Direction, Storage<ItemVariant>> storageCache;

    private int millTimeTotal;
    private int millTime;

    private float expPerMilling;
    private float expAccumulated;

    private int transferCooldown;

    public GravelMillEntity(BlockPos pos, BlockState state) {
        super(BasaltCrusher.GRAVEL_MILL_ENTITY, pos, state);

        // Initialize cached milling state.
        this.millState = state.getValue(MILL_STATE);
        this.storageCache = new EnumMap<>(Direction.class);

        // Our mod is a simple mod.
        this.millTimeTotal = 200;
        this.millTime = 0;
        this.expPerMilling = 0.1F;
        this.expAccumulated = 0.0F;
        this.transferCooldown = 0;
    }

    // BasaltCrusherInventory is the backing store for our Storage implementations.
    private final BasaltCrusherInventory inventory = new BasaltCrusherInventory(3) {
        private static final int[] TOP_SLOTS = new int[]{1};    // consumable: replacement rods
        private static final int[] SIDE_SLOTS = new int[]{};    // no transfer
        private static final int[] BACK_SLOTS = new int[]{2};   // output: sand
        private static final int[] FRONT_SLOTS = new int[]{0};  // input: gravel
        // For convenience when using Item Hoppers, output can also be extracted down.
        private static final int[] BOTTOM_SLOTS = new int[]{2}; // output: sand

        @Override
        public int[] getSlotsForFace(Direction direction) {
            Direction facing = GravelMillEntity.this.getBlockState().getValue(GravelMillBlock.FACING);

            if (direction == Direction.UP) {
                return TOP_SLOTS;
            } else if (direction == Direction.DOWN) {
                return BOTTOM_SLOTS;
            } else if (direction == facing) {
                return FRONT_SLOTS;
            } else if (direction == facing.getOpposite()) {
                return BACK_SLOTS;
            } else {
                return SIDE_SLOTS;
            }
        }

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
            // All slots filter insertion.
            return this.canPlaceItem(slot, stack);
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
            // Allow extracting anything from any slot that matches the direction.
            return true;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            boolean retVal = false;

            switch (slot) {
                case 0:
                    // input slot
                    retVal = stack.is(Items.GRAVEL) || stack.is(Items.SAND) || TerrestriaIntegration.ENABLED &&
                            (stack.is(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM) || stack.is(TerrestriaIntegration.VOLCANIC_SAND_ITEM));
                    break;
                case 1:
                    // rod charge slot
                    retVal = stack.is(BasaltCrusher.MILL_ROD_CHARGE_ITEM);
                    break;
                case 2:
                    // output slot
                    break;
            }

            return retVal;
        }

        @Override
        public void setChanged() {
            GravelMillEntity.this.setChanged();
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            ItemStack target = this.getItem(slot);
            boolean sameItem = !stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, target);

            super.setItem(slot, stack);

            if (slot == 0 && !sameItem) {
                GravelMillEntity.this.millTime = 0;
            }

            GravelMillEntity.this.setChanged();
        }
    };

    public Storage<ItemVariant> getSidedStorage(Direction direction) {
        if (this.storageCache.get(direction) == null) {
            this.storageCache.put(direction, ContainerStorage.of(this.inventory, direction));
        }

        return this.storageCache.get(direction);
    }

    // Provide the milling progress to the menu.
    private final ContainerData propertyDelegate = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> GravelMillEntity.this.millTime;
                case 1 -> GravelMillEntity.this.millTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> GravelMillEntity.this.millTime = value;
                case 1 -> GravelMillEntity.this.millTimeTotal = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new GravelMillScreenHandler(syncId, playerInventory, this.inventory, this.propertyDelegate);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        inventory.storeAsItemList(view.list("Inventory", ItemStack.OPTIONAL_CODEC));

        view.putInt("MillTimeTotal", this.millTimeTotal);
        view.putInt("MillTime", this.millTime);
        view.putFloat("ExpPerMilling", expPerMilling);
        view.putFloat("ExpAccumulated", expAccumulated);
        view.putInt("TransferCooldown", this.transferCooldown);

        super.saveAdditional(view);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);

        inventory.fromItemList(view.listOrEmpty("Inventory", ItemStack.OPTIONAL_CODEC));

        millTimeTotal = view.getIntOr("MillTimeTotal", 0);
        millTime = view.getIntOr("MillTime", 0);
        expPerMilling = view.getFloatOr("ExpPerMilling", 0f);
        expAccumulated = view.getFloatOr("ExpAccumulated", 0f);
        transferCooldown = view.getIntOr("TransferCooldown", 0);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GravelMillEntity entity) {
        if (!level.isClientSide()) {
            entity.tickMill(level, pos, state, entity);
            entity.tickTransfer(level, pos, state, entity);
        }
    }

    private void tickMill(Level level, BlockPos pos, BlockState state, GravelMillEntity entity) {
        ItemStack input = entity.inventory.getItem(0);
        ItemStack output = entity.inventory.getItem(2);
        ItemStack rodCharge = entity.inventory.getItem(1);

        // The mill is shut down without a rod charge.  Short circuit.
        if (rodCharge.isEmpty()) {
            entity.setMillState(state, 21);

            return;
        }

        // We can't mill if our output is full.  Short circuit.
        if (output.getCount() == output.getMaxStackSize()) {
            entity.setMillState(state, 20);

            return;
        }

        if (input.isEmpty() || !IOTypeMatchers.matchGravelSand(input, output)) {
            // We can't be milling so ensure milling is reset.
            // Conditions: [empty input] OR [mismatched input and output]
            entity.setMillState(state, 20);
            if (entity.millTime != 0) {
                entity.millTime = 0;
                entity.setChanged();
            }
        } else if (input.is(Items.SAND)) {
            // Bypass sand input for user convenience.
            // Typically in a real implementation it would be pre-screened to save mill wear.
            // However the mill could be fed a sandy mix (and just have the mill rate adjusted).
            input.shrink(1);
            if (output.isEmpty() || output.getCount() < 1) {
                entity.inventory.setItem(2, new ItemStack(Items.SAND, 1));
            } else {
                output.grow(1);
            }
            // Try to damage the rod charge (if possible), but less than with gravel.
            if (rodCharge.isDamageableItem()) {
                if (0.25d > level.getRandom().nextDouble()) {
                    rodCharge.setDamageValue(rodCharge.getDamageValue() + 1);
                }
                if (rodCharge.getDamageValue() >= rodCharge.getMaxDamage()) {
                    rodCharge.shrink(1);
                }
            }
            entity.setChanged();
        } else if (TerrestriaIntegration.ENABLED && input.is(TerrestriaIntegration.VOLCANIC_SAND_ITEM)) {
            // Bypass sand input for user convenience.
            // Typically in a real implementation it would be pre-screened to save mill wear.
            // However the mill could be fed a sandy mix (and just have the mill rate adjusted).
            input.shrink(1);
            if (output.isEmpty() || output.getCount() < 1) {
                entity.inventory.setItem(2, new ItemStack(TerrestriaIntegration.VOLCANIC_SAND_ITEM, 1));
            } else {
                output.grow(1);
            }
            // Try to damage the rod charge (if possible), but less than with gravel.
            if (rodCharge.isDamageableItem()) {
                if (0.25d > level.getRandom().nextDouble()) {
                    rodCharge.setDamageValue(rodCharge.getDamageValue() + 1);
                }
                if (rodCharge.getDamageValue() >= rodCharge.getMaxDamage()) {
                    rodCharge.shrink(1);
                }
            }
            entity.setChanged();
        } else {
            // Start or continue milling.
            // Rod mills should travel about 280 to 480 ft/min inside the cylinder.
            // We can achieve right around 280 by rotating our ~1m mill every 2s.
            // A 20 tick cycle represents half a rotation.
            entity.setMillState(state, millTime % 20);

            // Rate of milling in the rod mill is constant.
            // (In real rod mills it depends on things like input mix, rotation speed, and rod size.)
            ++entity.millTime;
            entity.setChanged();
        }

        if (entity.millTime >= entity.millTimeTotal) {
            // Successful milling.
            if (output.isEmpty()) {
                if (TerrestriaIntegration.ENABLED && input.is(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM)) {
                    entity.inventory.setItem(2, new ItemStack(TerrestriaIntegration.VOLCANIC_SAND_ITEM, 1));
                } else {
                    entity.inventory.setItem(2, new ItemStack(Items.SAND, 1));
                }
            } else {
                output.grow(1);
            }
            input.shrink(1);
            // Try to damage the rod charge (if possible).
            if (rodCharge.isDamageableItem()) {
                rodCharge.setDamageValue(rodCharge.getDamageValue() + 1);
                if (rodCharge.getDamageValue() >= rodCharge.getMaxDamage()) {
                    rodCharge.shrink(1);
                }
            }
            // Add XP.
            entity.expAccumulated += entity.expPerMilling;
            // Reset milling timer.
            entity.millTime = 0;
            entity.setChanged();
        }
    }

    private void tickTransfer(Level level, BlockPos pos, BlockState state, GravelMillEntity entity) {
        ItemStack output = entity.inventory.getItem(2);

        // Implement transfer cooldown.
        if (entity.transferCooldown > 0) {
            --transferCooldown;
            entity.setChanged();
        }

        if (entity.transferCooldown <= 0 && !output.isEmpty()) {
            // Try to push an item into adjacent storage.
            Direction vent = state.getValue(GravelMillBlock.FACING).getOpposite();
            Storage<ItemVariant> sourceStorage = entity.getSidedStorage(vent);
            Storage<ItemVariant> targetStorage = ItemStorage.SIDED.find(level, pos.relative(vent), vent.getOpposite());

            if (targetStorage != null) {
                if (StorageUtil.move(sourceStorage, targetStorage, variant -> true, 1, null) > 0) {
                    entity.transferCooldown = 8;
                    entity.setChanged();
                }
            }
        }
    }

    public void scatterInventory(Level level, BlockPos pos) {
        Containers.dropContents(level, pos, this.inventory);
    }

    public int calculateComparatorOutput() {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(this.inventory);
    }

    public void dropExperience(Level level, Player player) {
        int expOrb;

        while (expAccumulated >= 1.0F) {
            expOrb = ExperienceOrb.getExperienceValue((int) expAccumulated);
            expAccumulated -= expOrb;
            level.addFreshEntity(new ExperienceOrb(level, player.getX(), player.getY() + 0.5D, player.getZ() + 0.5D, expOrb));
        }

        this.setChanged();
    }

    // Local cache in the BE so we only update the BS when the state changes.
    // This way I can set the state whenever I feel like it without any penalty.
    private boolean setMillState(BlockState state, int newState) {
        assert (newState >= 0 && newState <= 21);

        if (newState == this.millState || this.level == null) {
            return false;
        } else {
            this.level.setBlockAndUpdate(worldPosition, state.setValue(MILL_STATE, newState));
            this.millState = newState;
            return true;
        }
    }
}