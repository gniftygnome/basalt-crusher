package net.gnomecraft.basaltcrusher.crusher;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.gnomecraft.basaltcrusher.utils.BasaltCrusherInventory;
import net.gnomecraft.basaltcrusher.utils.IOTypeMatchers;
import net.gnomecraft.basaltcrusher.utils.TerrestriaIntegration;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.EnumMap;
import java.util.HashMap;

import static net.gnomecraft.basaltcrusher.crusher.BasaltCrusherBlock.CRUSHING_STATE;

public class BasaltCrusherEntity extends BlockEntity implements NamedScreenHandlerFactory {
    private BasaltCrusherBlock.CrushingState crushingState;
    private final EnumMap<Direction, Storage<ItemVariant>> storageCache;
    private final HashMap<RegistryKey<Enchantment>, RegistryEntry<Enchantment>> enchantmentEntries = new HashMap<>(8);

    private int crushTimeTotal;
    private int crushTime;

    private float expPerCrush;
    private float expAccumulated;

    public BasaltCrusherEntity(BlockPos pos, BlockState state) {
        super(BasaltCrusher.BASALT_CRUSHER_ENTITY, pos, state);

        // Initialize cached crushing state.
        this.crushingState = state.get(CRUSHING_STATE);
        this.storageCache = new EnumMap<>(Direction.class);

        // Our mod is a simple mod.
        this.crushTimeTotal = 420;
        this.crushTime = 0;
        this.expPerCrush = 0.1F;
        this.expAccumulated = 0.0F;
    }

    // BasaltCrusherInventory is the backing store for our Storage implementations.
    private final BasaltCrusherInventory inventory = new BasaltCrusherInventory(5) {
        private static final int[] TOP_SLOTS = new int[] {0};
        private static final int[] SIDE_SLOTS = new int[] {1};
        private static final int[] BOTTOM_SLOTS = new int[] {2};
        // Crushing slots cannot be targeted: {3,4}

        @Override
        public int[] getAvailableSlots(Direction direction) {
            if (direction == Direction.UP) {
                return TOP_SLOTS;
            } else if (direction == Direction.DOWN) {
                return BOTTOM_SLOTS;
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
                    // TODO: use recipes
                    retVal = stack.isIn(BasaltCrusher.BASALTS);
                    break;
                case 1:
                    // jaw liner slot
                    // TODO: use recipes
                    retVal = stack.isIn(BasaltCrusher.JAW_LINERS);
                    break;
                case 2:
                    // output slot
                    break;
                case 3:
                    // top crushing slot (active jaw liner)
                    // TODO: use recipes
                    retVal = (stack.isIn(BasaltCrusher.JAW_LINERS) && stack.getCount() == 1 && !ItemStack.areItemsAndComponentsEqual(stack, this.getStack(3)));
                    break;
                case 4:
                    // bottom crushing slot (active jaw liner)
                    // TODO: use recipes
                    retVal = (stack.isIn(BasaltCrusher.JAW_LINERS) && stack.getCount() == 1 && !ItemStack.areItemsAndComponentsEqual(stack, this.getStack(4)));
                    break;
            }

            return retVal;
        }

        @Override
        public void markDirty() {
            BasaltCrusherEntity.this.markDirty();
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            ItemStack target = this.getStack(slot);
            boolean sameItem = !stack.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, target);

            super.setStack(slot, stack);

            if (slot == 0 && !sameItem) {
                BasaltCrusherEntity.this.crushTime = 0;
            }

            BasaltCrusherEntity.this.markDirty();
        }
    };

    // The jawStorage is the transfer access to the Jaw Liner slot (1).
    private final SingleStackStorage jawStorage = new SingleStackStorage() {
        @Override
        protected ItemStack getStack() {
            return BasaltCrusherEntity.this.inventory.getStack(1);
        }

        @Override
        protected void setStack(ItemStack stack) {
            BasaltCrusherEntity.this.inventory.setStack(1, stack);
        }

        @Override
        protected boolean canInsert(ItemVariant itemVariant) {
            return itemVariant.toStack().isIn(BasaltCrusher.JAW_LINERS);
        }

        /* TODO: review whether we can restore the 16-stack behavior somehow
        @Override
        protected int getCapacity(ItemVariant itemVariant) {
            return 16;
        }
        */
    };

    public Storage<ItemVariant> getSidedStorage(Direction direction) {
        if (direction == null) {
            return null;
        }

        if (storageCache.get(direction) == null) {
            if (direction == Direction.DOWN || direction == Direction.UP) {
                // slots 0 and 2 via InventoryStorage
                storageCache.put(direction, InventoryStorage.of(inventory, direction));
            } else {
                // slot 1 via SingleStackStorage
                storageCache.put(direction, jawStorage);
            }
        }

        return storageCache.get(direction);
    }

    // Provide the crushing progress to the menu.
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> BasaltCrusherEntity.this.crushTime;
                case 1 -> BasaltCrusherEntity.this.crushTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> BasaltCrusherEntity.this.crushTime = value;
                case 1 -> BasaltCrusherEntity.this.crushTimeTotal = value;
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new BasaltCrusherScreenHandler(syncId, playerInventory, this.inventory, this.propertyDelegate);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    protected void writeData(WriteView view) {
        inventory.toDataList(view.getListAppender("Inventory", ItemStack.OPTIONAL_CODEC));

        view.putInt("CrushTimeTotal", this.crushTimeTotal);
        view.putInt("CrushTime", this.crushTime);
        view.putFloat("ExpPerCrush", expPerCrush);
        view.putFloat("ExpAccumulated", expAccumulated);

        super.writeData(view);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);

        inventory.readDataList(view.getTypedListView("Inventory", ItemStack.OPTIONAL_CODEC));

        crushTimeTotal = view.getInt("CrushTimeTotal", 0);
        crushTime = view.getInt("CrushTime", 0);
        expPerCrush = view.getFloat("ExpPerCrush", 0f);
        expAccumulated = view.getFloat("ExpAccumulated", 0f);
    }

    @SuppressWarnings("unused")
    public static void tick(World world, BlockPos pos, BlockState state, BasaltCrusherEntity entity) {
        if (entity != null && world != null && !world.isClient()) {
            entity.tickJawLiners(world, pos, state, entity);
            entity.tickCrusher(world, pos, state, entity);
        }
    }

    private void tickJawLiners(World world, BlockPos pos, BlockState state, BasaltCrusherEntity entity) {
        ItemStack liners = entity.inventory.getStack(1);
        ItemStack upperJaw = entity.inventory.getStack(3);
        ItemStack lowerJaw = entity.inventory.getStack(4);

        // Make sure there is a jaw liner in the top slot if we have one available.
        if (upperJaw.isEmpty()) {
            if (liners.isEmpty()) {
                // Update the display because we have no jaw liners available.
                entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.EMPTY);
            } else {
                // Move a jaw liner into the top jaw slot.
                entity.inventory.setStack(3, entity.inventory.removeStack(1, 1));
                entity.markDirty();
            }
        }

        // Make sure there is a jaw liner in the bottom slot if we have one available.
        if (lowerJaw.isEmpty()) {
            if (liners.isEmpty()) {
                // Update the display because we have no jaw liners available.
                entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.EMPTY);
            } else {
                // Move a jaw liner into the bottom jaw slot.
                entity.inventory.setStack(4, entity.inventory.removeStack(1, 1));
                entity.markDirty();
            }
        }
    }

    private void tickCrusher(World world, BlockPos pos, BlockState state, BasaltCrusherEntity entity) {
        ItemStack input = entity.inventory.getStack(0);
        ItemStack output = entity.inventory.getStack(2);
        ItemStack upperJaw = entity.inventory.getStack(3);
        ItemStack lowerJaw = entity.inventory.getStack(4);

        // We can't crush if our output is full.  Short circuit.
        if (output.getCount() == output.getMaxCount()) {
            entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.IDLE);

            return;
        }

        if (!upperJaw.isEmpty() && !lowerJaw.isEmpty()) {
            if (input.isEmpty() || !IOTypeMatchers.matchStoneGravel(input, output)) {
                // We can't be crushing so ensure crushing is reset.
                // Conditions: [empty input] OR [mismatched input and output]
                entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.IDLE);
                if (entity.crushTime != 0) {
                    entity.crushTime = 0;
                    entity.markDirty();
                }
            } else {
                // Start or continue crushing.
                // The jaws cycle 3 times per 210-tick crush, every 3.5 seconds.
                // Ideally the math produces numbers evenly distributed between 0 and 6 except
                // that the last (highest) result should be as close to 6 as possible, over or under.
                switch ((crushTime % 140) / 23) {
                    case 0 -> entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.OPEN);
                    case 1 -> entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.OPENISH);
                    case 2 -> entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.CLOSEDISH);
                    case 3 -> entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.CLOSED);
                    case 4 -> entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.CLOSEDISH);
                    case 5 -> entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.OPENISH);
                    case 6 -> entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.OPEN);
                }

                // Rate of crushing varies from 2 to 7 depending on use of Efficiency enchants.
                // Total time can vary from 210 (no Efficiency) to 60 (two Efficiency V) ticks in duration.
                entity.crushTime += 2 + (getEnchantmentLevel(world, Enchantments.EFFICIENCY, upperJaw) + getEnchantmentLevel(world, Enchantments.EFFICIENCY, lowerJaw)) / 2;
                entity.markDirty();
            }
        }

        if (entity.crushTime >= entity.crushTimeTotal) {
            // Successful crushing.
            // TODO: use recipes.
            input.decrement(1);
            if (output.isEmpty()) {
                if (TerrestriaIntegration.ENABLED && input.isIn(TerrestriaIntegration.TERRESTRIA_BASALTS)) {
                    entity.inventory.setStack(2, new ItemStack(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM, 1));
                } else {
                    entity.inventory.setStack(2, new ItemStack(Blocks.GRAVEL, 1));
                }
            } else {
                output.increment(1);
            }
            // Try to damage the top jaw liner (if possible).
            if (upperJaw.isDamageable()) {
                if ((0.5d / (1.0d + (double) getEnchantmentLevel(world, Enchantments.UNBREAKING, upperJaw))) > world.random.nextDouble()) {
                    upperJaw.setDamage(upperJaw.getDamage() + 1);
                }
                if (upperJaw.getDamage() >= upperJaw.getMaxDamage()) {
                    upperJaw.decrement(1);
                }
            }
            // Try to damage the bottom jaw liner (if possible).
            if (lowerJaw.isDamageable()) {
                if ((0.5d / (1.0d + (double) getEnchantmentLevel(world, Enchantments.UNBREAKING, lowerJaw))) > world.random.nextDouble()) {
                    lowerJaw.setDamage(lowerJaw.getDamage() + 1);
                }
                if (lowerJaw.getDamage() >= lowerJaw.getMaxDamage()) {
                    lowerJaw.decrement(1);
                }
            }
            // Add XP.
            entity.expAccumulated += entity.expPerCrush;

            // Draw down stored XP to mend jaw liners.
            if (entity.expAccumulated >= 1.0f) {
                if (0.5d > world.random.nextDouble()) {
                    if (getEnchantmentLevel(world, Enchantments.MENDING, upperJaw) > 0 && upperJaw.isDamaged()) {
                        upperJaw.setDamage(upperJaw.getDamage() - 1);
                        entity.expAccumulated -= 1.0f;
                    }
                } else {
                    if (getEnchantmentLevel(world, Enchantments.MENDING, lowerJaw) > 0 && lowerJaw.isDamaged()) {
                        lowerJaw.setDamage(lowerJaw.getDamage() - 1);
                        entity.expAccumulated -= 1.0f;
                    }
                }
            }

            // Reset crush timer.
            entity.crushTime = 0;
            entity.markDirty();
        }
    }

    private int getEnchantmentLevel(World world, RegistryKey<Enchantment> enchantment, ItemStack stack) {
        return EnchantmentHelper.getLevel(
                enchantmentEntries.computeIfAbsent(
                        enchantment,
                        key -> world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(key)),
                stack);
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
    private boolean setCrushingState(BlockState state, BasaltCrusherBlock.CrushingState newState) {
        if (newState == this.crushingState || this.world == null) {
            return false;
        } else {
            this.world.setBlockState(pos, state.with(CRUSHING_STATE, newState));
            this.crushingState = newState;
            return true;
        }
    }
}