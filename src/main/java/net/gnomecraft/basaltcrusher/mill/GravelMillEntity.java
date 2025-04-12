package net.gnomecraft.basaltcrusher.mill;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.gnomecraft.basaltcrusher.utils.BasaltCrusherInventory;
import net.gnomecraft.basaltcrusher.utils.IOTypeMatchers;
import net.gnomecraft.basaltcrusher.utils.TerrestriaIntegration;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.EnumMap;

import static net.gnomecraft.basaltcrusher.mill.GravelMillBlock.MILL_STATE;

public class GravelMillEntity extends BlockEntity implements NamedScreenHandlerFactory {
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
        this.millState = state.get(MILL_STATE);
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
        public int[] getAvailableSlots(Direction direction) {
            Direction facing = GravelMillEntity.this.getCachedState().get(GravelMillBlock.FACING);

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
        public boolean canInsert(int slot, ItemStack stack, Direction direction) {
            // All slots filter insertion.
            return this.isValid(slot, stack);
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction direction) {
            // Allow extracting anything from any slot that matches the direction.
            return true;
        }

        @Override
        public boolean isValid(int slot, ItemStack stack) {
            boolean retVal = false;

            switch (slot) {
                case 0:
                    // input slot
                    retVal = stack.isOf(Items.GRAVEL) || stack.isOf(Items.SAND) || TerrestriaIntegration.ENABLED &&
                            (stack.isOf(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM) || stack.isOf(TerrestriaIntegration.VOLCANIC_SAND_ITEM));
                    break;
                case 1:
                    // rod charge slot
                    retVal = stack.isOf(BasaltCrusher.MILL_ROD_CHARGE_ITEM);
                    break;
                case 2:
                    // output slot
                    break;
            }

            return retVal;
        }

        @Override
        public void markDirty() {
            GravelMillEntity.this.markDirty();
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            ItemStack target = this.getStack(slot);
            boolean sameItem = !stack.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, target);

            super.setStack(slot, stack);

            if (slot == 0 && !sameItem) {
                GravelMillEntity.this.millTime = 0;
            }

            GravelMillEntity.this.markDirty();
        }
    };

    public Storage<ItemVariant> getSidedStorage(Direction direction) {
        if (direction == null) {
            return null;
        }

        if (this.storageCache.get(direction) == null) {
            this.storageCache.put(direction, InventoryStorage.of(this.inventory, direction));
        }

        return this.storageCache.get(direction);
    }

    // Provide the milling progress to the menu.
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
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
        public int size() {
            return 2;
        }
    };

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new GravelMillScreenHandler(syncId, playerInventory, this.inventory, this.propertyDelegate);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.put("Inventory", this.inventory.toNbtList(registryLookup));

        tag.putShort("MillTimeTotal", (short) this.millTimeTotal);
        tag.putShort("MillTime", (short) this.millTime);
        tag.putFloat("ExpPerMilling", expPerMilling);
        tag.putFloat("ExpAccumulated", expAccumulated);
        tag.putShort("TransferCooldown", (short) this.transferCooldown);

        super.writeNbt(tag, registryLookup);
    }

    @Override
    public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(tag, registryLookup);

        inventory.readNbtList(tag.getListOrEmpty("Inventory"), registryLookup);

        millTimeTotal = tag.getShort("MillTimeTotal", (short) 0);
        millTime = tag.getShort("MillTime", (short) 0);
        expPerMilling = tag.getFloat("ExpPerMilling", 0f);
        expAccumulated = tag.getFloat("ExpAccumulated", 0f);
        transferCooldown = tag.getShort("TransferCooldown", (short) 0);
    }

    public static void tick(World world, BlockPos pos, BlockState state, GravelMillEntity entity) {
        if (entity != null && world != null && !world.isClient()) {
            entity.tickMill(world, pos, state, entity);
            entity.tickTransfer(world, pos, state, entity);
        }
    }

    private void tickMill(World world, BlockPos pos, BlockState state, GravelMillEntity entity) {
        ItemStack input = entity.inventory.getStack(0);
        ItemStack output = entity.inventory.getStack(2);
        ItemStack rodCharge = entity.inventory.getStack(1);

        // The mill is shut down without a rod charge.  Short circuit.
        if (rodCharge.isEmpty()) {
            entity.setMillState(state, 21);

            return;
        }

        // We can't mill if our output is full.  Short circuit.
        if (output.getCount() == output.getMaxCount()) {
            entity.setMillState(state, 20);

            return;
        }

        if (input.isEmpty() || !IOTypeMatchers.matchGravelSand(input, output)) {
            // We can't be milling so ensure milling is reset.
            // Conditions: [empty input] OR [mismatched input and output]
            entity.setMillState(state, 20);
            if (entity.millTime != 0) {
                entity.millTime = 0;
                entity.markDirty();
            }
        } else if (input.isOf(Items.SAND)) {
            // Bypass sand input for user convenience.
            // Typically in a real implementation it would be pre-screened to save mill wear.
            // However the mill could be fed a sandy mix (and just have the mill rate adjusted).
            input.decrement(1);
            if (output.isEmpty() || output.getCount() < 1) {
                entity.inventory.setStack(2, new ItemStack(Items.SAND, 1));
            } else {
                output.increment(1);
            }
            // Try to damage the rod charge (if possible), but less than with gravel.
            if (rodCharge.isDamageable()) {
                if (0.25d > world.random.nextDouble()) {
                    rodCharge.setDamage(rodCharge.getDamage() + 1);
                }
                if (rodCharge.getDamage() >= rodCharge.getMaxDamage()) {
                    rodCharge.decrement(1);
                }
            }
            entity.markDirty();
        } else if (TerrestriaIntegration.ENABLED && input.isOf(TerrestriaIntegration.VOLCANIC_SAND_ITEM)) {
            // Bypass sand input for user convenience.
            // Typically in a real implementation it would be pre-screened to save mill wear.
            // However the mill could be fed a sandy mix (and just have the mill rate adjusted).
            input.decrement(1);
            if (output.isEmpty() || output.getCount() < 1) {
                entity.inventory.setStack(2, new ItemStack(TerrestriaIntegration.VOLCANIC_SAND_ITEM, 1));
            } else {
                output.increment(1);
            }
            // Try to damage the rod charge (if possible), but less than with gravel.
            if (rodCharge.isDamageable()) {
                if (0.25d > world.random.nextDouble()) {
                    rodCharge.setDamage(rodCharge.getDamage() + 1);
                }
                if (rodCharge.getDamage() >= rodCharge.getMaxDamage()) {
                    rodCharge.decrement(1);
                }
            }
            entity.markDirty();
        } else {
            // Start or continue milling.
            // Rod mills should travel about 280 to 480 ft/min inside the cylinder.
            // We can achieve right around 280 by rotating our ~1m mill every 2s.
            // A 20 tick cycle represents half a rotation.
            entity.setMillState(state, millTime % 20);

            // Rate of milling in the rod mill is constant.
            // (In real rod mills it depends on things like input mix, rotation speed, and rod size.)
            ++entity.millTime;
            entity.markDirty();
        }

        if (entity.millTime >= entity.millTimeTotal) {
            // Successful milling.
            if (output.isEmpty()) {
                if (TerrestriaIntegration.ENABLED && input.isOf(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM)) {
                    entity.inventory.setStack(2, new ItemStack(TerrestriaIntegration.VOLCANIC_SAND_ITEM, 1));
                } else {
                    entity.inventory.setStack(2, new ItemStack(Items.SAND, 1));
                }
            } else {
                output.increment(1);
            }
            input.decrement(1);
            // Try to damage the rod charge (if possible).
            if (rodCharge.isDamageable()) {
                rodCharge.setDamage(rodCharge.getDamage() + 1);
                if (rodCharge.getDamage() >= rodCharge.getMaxDamage()) {
                    rodCharge.decrement(1);
                }
            }
            // Add XP.
            entity.expAccumulated += entity.expPerMilling;
            // Reset milling timer.
            entity.millTime = 0;
            entity.markDirty();
        }
    }

    private void tickTransfer(World world, BlockPos pos, BlockState state, GravelMillEntity entity) {
        ItemStack output = entity.inventory.getStack(2);

        // Implement transfer cooldown.
        if (entity.transferCooldown > 0) {
            --transferCooldown;
            entity.markDirty();
        }

        if (entity.transferCooldown <= 0 && !output.isEmpty()) {
            // Try to push an item into adjacent storage.
            Direction vent = state.get(GravelMillBlock.FACING).getOpposite();
            Storage<ItemVariant> sourceStorage = entity.getSidedStorage(vent);
            Storage<ItemVariant> targetStorage = ItemStorage.SIDED.find(world, pos.offset(vent), vent.getOpposite());

            if (sourceStorage != null && targetStorage != null) {
                if (StorageUtil.move(sourceStorage, targetStorage, variant -> true, 1, null) > 0) {
                    entity.transferCooldown = 8;
                    entity.markDirty();
                }
            }
        }
    }

    public void scatterInventory(World world, BlockPos pos) {
        ItemScatterer.spawn(world, pos, this.inventory);
    }

    public int calculateComparatorOutput() {
        return ScreenHandler.calculateComparatorOutput(this.inventory);
    }

    public void dropExperience(PlayerEntity player) {
        int expOrb;

        if (player == null) return;

        while (expAccumulated >= 1.0F) {
            expOrb = ExperienceOrbEntity.roundToOrbSize((int) expAccumulated);
            expAccumulated -= expOrb;
            player.getWorld().spawnEntity(new ExperienceOrbEntity(player.getWorld(), player.getX(), player.getY() + 0.5D, player.getZ() + 0.5D, expOrb));
        }

        this.markDirty();
    }

    // Local cache in the BE so we only update the BS when the state changes.
    // This way I can set the state whenever I feel like it without any penalty.
    private boolean setMillState(BlockState state, int newState) {
        assert (newState >= 0 && newState <= 21);

        if (newState == this.millState || this.world == null) {
            return false;
        } else {
            this.world.setBlockState(pos, state.with(MILL_STATE, newState));
            this.millState = newState;
            return true;
        }
    }
}