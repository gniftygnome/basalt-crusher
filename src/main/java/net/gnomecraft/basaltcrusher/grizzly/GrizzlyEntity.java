package net.gnomecraft.basaltcrusher.grizzly;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.gnomecraft.basaltcrusher.crusher.BasaltCrusherEntity;
import net.gnomecraft.basaltcrusher.utils.BasaltCrusherInventory;
import net.gnomecraft.basaltcrusher.utils.TerrestriaIntegration;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
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
import java.util.HashMap;
import java.util.Map;

public class GrizzlyEntity extends BlockEntity implements NamedScreenHandlerFactory {
    private final EnumMap<Direction, Storage<ItemVariant>> storageCache;

    private int processingTimeTotal;
    private int processingTime;

    private ItemStack lastInput;

    private final HashMap<Item, Double> stockpile;

    public GrizzlyEntity(BlockPos pos, BlockState state) {
        super(BasaltCrusher.GRIZZLY_ENTITY, pos, state);

        this.storageCache = new EnumMap<>(Direction.class);

        this.processingTimeTotal = 16;
        this.processingTime = 0;

        lastInput = Items.COARSE_DIRT.getDefaultStack();

        if (TerrestriaIntegration.ENABLED) {
            this.stockpile = new HashMap<>(Map.of(Items.DIRT, 0.0d,
                    Items.GRAVEL, 0.0d, TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM, 0.0d,
                    Items.SAND, 0.0d, TerrestriaIntegration.VOLCANIC_SAND_ITEM, 0.0d));
        } else {
            this.stockpile = new HashMap<>(Map.of(Items.DIRT, 0.0d, Items.GRAVEL, 0.0d, Items.SAND, 0.0d));
        }
    }

    // BasaltCrusherInventory is the backing store for our Storage implementations.
    private final BasaltCrusherInventory inventory = new BasaltCrusherInventory(3) {
        private static final int[] TOP_SLOTS = new int[]{0};
        private static final int[] SIDE_SLOTS = new int[]{};
        private static final int[] BACK_SLOTS = new int[]{2};
        // For modded extractors (f.e. Ductwork Collectors), can extract or input via the front.
        private static final int[] FRONT_SLOTS = new int[]{0,1};
        // For convenience when using Item Hoppers, both outputs can be extracted down.
        private static final int[] BOTTOM_SLOTS = new int[]{1,2};

        @Override
        public int[] getAvailableSlots(Direction direction) {
            Direction facing = GrizzlyEntity.this.getCachedState().get(GrizzlyBlock.FACING);

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
                    // gravel is fetched, not inputted
                    retVal = stack.isOf(Items.COARSE_DIRT);
                    break;
                case 1:
                    // gravel output slot
                    break;
                case 2:
                    // sand/dirt output slot
                    break;
            }

            return retVal;
        }

        @Override
        public void markDirty() {
            GrizzlyEntity.this.markDirty();
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

    // Provide the stockpile levels to the menu.
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            if (TerrestriaIntegration.ENABLED) {
                return switch (index) {
                    case 0 -> GrizzlyEntity.this.lastInput.isOf(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM) ? 1 : 0;
                    case 1 -> (int) (100 * GrizzlyEntity.this.stockpile.get(Items.DIRT));
                    case 2 -> (int) (100 * GrizzlyEntity.this.stockpile.get(Items.GRAVEL));
                    case 3 -> (int) (100 * GrizzlyEntity.this.stockpile.get(Items.SAND));
                    case 4 -> (int) (100 * GrizzlyEntity.this.stockpile.get(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM));
                    case 5 -> (int) (100 * GrizzlyEntity.this.stockpile.get(TerrestriaIntegration.VOLCANIC_SAND_ITEM));
                    default -> 0;
                };
            } else {
                return switch (index) {
                    case 1 -> (int) (100 * GrizzlyEntity.this.stockpile.get(Items.DIRT));
                    case 2 -> (int) (100 * GrizzlyEntity.this.stockpile.get(Items.GRAVEL));
                    case 3 -> (int) (100 * GrizzlyEntity.this.stockpile.get(Items.SAND));
                    default -> 0;
                };
            }
        }

        @Override
        public void set(int index, int value) {
            if (TerrestriaIntegration.ENABLED) {
                switch (index) {
                    case 1 -> GrizzlyEntity.this.stockpile.put(Items.DIRT, (double) (value / 100));
                    case 2 -> GrizzlyEntity.this.stockpile.put(Items.GRAVEL, (double) (value / 100));
                    case 3 -> GrizzlyEntity.this.stockpile.put(Items.SAND, (double) (value / 100));
                    case 4 -> GrizzlyEntity.this.stockpile.put(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM, (double) (value / 100));
                    case 5 -> GrizzlyEntity.this.stockpile.put(TerrestriaIntegration.VOLCANIC_SAND_ITEM, (double) (value / 100));
                    default -> {}
                }
            } else {
                switch (index) {
                    case 1 -> GrizzlyEntity.this.stockpile.put(Items.DIRT, (double) (value / 100));
                    case 2 -> GrizzlyEntity.this.stockpile.put(Items.GRAVEL, (double) (value / 100));
                    case 3 -> GrizzlyEntity.this.stockpile.put(Items.SAND, (double) (value / 100));
                    default -> {}
                }
            }
        }

        @Override
        public int size() {
            return 6;
        }
    };

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        tag.put("Inventory", this.inventory.toNbtList(registryLookup));

        tag.putShort("ProcessingTimeTotal", (short) this.processingTimeTotal);
        tag.putShort("ProcessingTime", (short) this.processingTime);

        tag.put("LastInput", this.lastInput.toNbt(registryLookup));

        NbtCompound outer = new NbtCompound();
        this.stockpile.forEach((item, amount) -> {
            NbtCompound inner = new NbtCompound();
            inner.put("item",  item.getDefaultStack().toNbt(registryLookup));
            inner.put("amount", NbtDouble.of(amount));
            outer.put(item.toString(), inner);
        });
        tag.put("stockpile", outer);

        super.writeNbt(tag, registryLookup);
    }

    @Override
    public void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(tag, registryLookup);

        inventory.readNbtList(tag.getListOrEmpty("Inventory"), registryLookup);

        processingTimeTotal = tag.getShort("ProcessingTimeTotal", (short) 0);
        processingTime = tag.getShort("ProcessingTime", (short) 0);

        ItemStack stack = ItemStack.fromNbt(registryLookup, tag.getCompoundOrEmpty("LastInput")).orElse(ItemStack.EMPTY);
        if (stack.isEmpty()) {
            lastInput = Items.COARSE_DIRT.getDefaultStack();
        } else {
            stack.setCount(1);
            lastInput = stack;
        }

        stockpile.clear();
        tag.getCompound("stockpile").ifPresent(outer -> {
            for (String key : outer.getKeys()) {
                outer.getCompound(key).ifPresent(inner ->
                        stockpile.put(
                                ItemStack.fromNbt(registryLookup, inner.getCompoundOrEmpty("item")).orElse(ItemStack.EMPTY).getItem(),
                                inner.getDouble("amount").orElse(0d)
                        )
                );
            }
        });
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new GrizzlyScreenHandler(syncId, playerInventory, this.inventory, this.propertyDelegate);
    }

    public static void tick(World world, BlockPos pos, BlockState state, GrizzlyEntity entity) {
        if (entity != null && world != null && !world.isClient()) {
            entity.tickGrizzly(world, pos);
        }
    }

    private void tickGrizzly(World world, BlockPos pos) {
        if (this.processingTime > 0) {
            --this.processingTime;
            this.markDirty();
            return;
        }

        ItemStack input  = this.inventory.getStack(0);
        ItemStack coarse = this.inventory.getStack(1);
        ItemStack fine   = this.inventory.getStack(2);
        if (input == null || coarse == null || fine == null) {
            return;
        }

        // If we can insert gravel to our input slot we will try to get some from a crusher above.
        if (input.getCount() < input.getMaxCount()) {
            if (world.getBlockEntity(pos.offset(Direction.UP)) instanceof BasaltCrusherEntity companion) {
                Storage<ItemVariant> source = companion.getSidedStorage(Direction.DOWN);

                try (Transaction transaction = Transaction.openOuter()) {
                    if ((input.isEmpty() || input.isOf(Items.GRAVEL)) && source.extract(ItemVariant.of(Items.GRAVEL), 1, transaction) > 0) {
                        input = new ItemStack(Items.GRAVEL, input.getCount() + 1);
                        this.inventory.setStack(0, input);
                        transaction.commit();

                        // Input at Item Hopper speed.
                        this.processingTime = 8;
                        this.markDirty();
                    } else if (TerrestriaIntegration.ENABLED &&
                            (input.isEmpty() || input.isOf(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM))
                            && source.extract(ItemVariant.of(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM), 1, transaction) > 0) {
                        input = new ItemStack(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM, input.getCount() + 1);
                        this.inventory.setStack(0, input);
                        transaction.commit();

                        // Input at Item Hopper speed.
                        this.processingTime = 8;
                        this.markDirty();
                    } else {
                        transaction.abort();
                    }
                }
            }
        }
        if (input.isEmpty()) {
            // There is nothing to do.
            return;
        } else {
            this.lastInput = input.copy();
        }

        // Sloppy mess wherein I reinvent the wheel to implement funky recipes entirely in code.
        if (coarse.isEmpty() || (coarse.isOf(Items.GRAVEL) && coarse.getCount() < coarse.getMaxCount())) {
            if (input.isOf(Items.GRAVEL) && (fine.isEmpty() || (fine.isOf(Items.SAND) && fine.getCount() < fine.getMaxCount()))) {
                // RECIPE: 4 gravel yields 3 gravel and 1 sand
                input.decrement(1);
                this.inventory.setStack(0, input);

                // Increment the gravel fraction; maybe move some to the coarse output.
                double gravel = this.stockpile.get(Items.GRAVEL) + 0.75d;
                if (gravel >= 1.0d) {
                    gravel -= 1.0d;
                    if (coarse.isEmpty()) {
                        coarse = new ItemStack(Items.GRAVEL, 1);
                    } else {
                        coarse.increment(1);
                    }
                    this.inventory.setStack(1, coarse);
                }
                this.stockpile.put(Items.GRAVEL, gravel);

                // Increment the sand fraction; maybe move some to the fine output.
                double sand = this.stockpile.get(Items.SAND) + 0.25d;
                if (sand >= 1.0d) {
                    sand -= 1.0d;
                    if (fine.isEmpty()) {
                        fine = new ItemStack(Items.SAND, 1);
                    } else {
                        fine.increment(1);
                    }
                    this.inventory.setStack(2, fine);
                }
                this.stockpile.put(Items.SAND, sand);

                this.processingTime = this.processingTimeTotal;
                this.markDirty();
            } else if (input.isOf(Items.COARSE_DIRT) && (fine.isEmpty() || (fine.isOf(Items.DIRT) && fine.getCount() < fine.getMaxCount()))) {
                // RECIPE: 2 coarse dirt yields 1 gravel and 1 dirt
                input.decrement(1);
                this.inventory.setStack(0, input);

                // Increment the gravel fraction; maybe move some to the coarse output.
                double gravel = this.stockpile.get(Items.GRAVEL) + 0.5d;
                if (gravel >= 1.0d) {
                    gravel -= 1.0d;
                    if (coarse.isEmpty()) {
                        coarse = new ItemStack(Items.GRAVEL, 1);
                    } else {
                        coarse.increment(1);
                    }
                    this.inventory.setStack(1, coarse);
                }
                this.stockpile.put(Items.GRAVEL, gravel);

                // Increment the dirt fraction; maybe move some to the fine output.
                double dirt = this.stockpile.get(Items.DIRT) + 0.5d;
                if (dirt >= 1.0d) {
                    dirt -= 1.0d;
                    if (fine.isEmpty()) {
                        fine = new ItemStack(Items.DIRT, 1);
                    } else {
                        fine.increment(1);
                    }
                    this.inventory.setStack(2, fine);
                }
                this.stockpile.put(Items.DIRT, dirt);

                this.processingTime = this.processingTimeTotal;
                this.markDirty();
            }
        }

        // Second pass for Terrestria integration
        if (TerrestriaIntegration.ENABLED && (coarse.isEmpty() || (coarse.isOf(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM) && coarse.getCount() < coarse.getMaxCount()))) {
            if (input.isOf(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM) && (fine.isEmpty() || (fine.isOf(TerrestriaIntegration.VOLCANIC_SAND_ITEM) && fine.getCount() < fine.getMaxCount()))) {
                // RECIPE: 4 gravel yields 3 gravel and 1 sand
                input.decrement(1);
                this.inventory.setStack(0, input);

                // Increment the gravel fraction; maybe move some to the coarse output.
                double gravel = this.stockpile.get(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM) + 0.75d;
                if (gravel >= 1.0d) {
                    gravel -= 1.0d;
                    if (coarse.isEmpty()) {
                        coarse = new ItemStack(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM, 1);
                    } else {
                        coarse.increment(1);
                    }
                    this.inventory.setStack(1, coarse);
                }
                this.stockpile.put(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM, gravel);

                // Increment the sand fraction; maybe move some to the fine output.
                double sand = this.stockpile.get(TerrestriaIntegration.VOLCANIC_SAND_ITEM) + 0.25d;
                if (sand >= 1.0d) {
                    sand -= 1.0d;
                    if (fine.isEmpty()) {
                        fine = new ItemStack(TerrestriaIntegration.VOLCANIC_SAND_ITEM, 1);
                    } else {
                        fine.increment(1);
                    }
                    this.inventory.setStack(2, fine);
                }
                this.stockpile.put(TerrestriaIntegration.VOLCANIC_SAND_ITEM, sand);

                this.processingTime = this.processingTimeTotal;
                this.markDirty();
            }
        }
    }

    public void scatterInventory(World world, BlockPos pos) {
        ItemScatterer.spawn(world, pos, this.inventory);
    }

    public int calculateComparatorOutput() {
        return ScreenHandler.calculateComparatorOutput(this.inventory);
    }
}