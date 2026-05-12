package net.gnomecraft.basaltcrusher.grizzly;

import com.google.common.base.Functions;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.gnomecraft.basaltcrusher.crusher.BasaltCrusherEntity;
import net.gnomecraft.basaltcrusher.utils.BasaltCrusherInventory;
import net.gnomecraft.basaltcrusher.utils.TerrestriaIntegration;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@NullMarked
public class GrizzlyEntity extends BlockEntity implements MenuProvider {
    private final EnumMap<Direction, Storage<ItemVariant>> storageCache;

    private int processingTimeTotal;
    private int processingTime;

    private ItemStack lastInput;

    private final HashMap<Item, Double> stockpile;

    private static final Codec<Map<Item, Double>> STOCKPILE_CODEC = Codec.unboundedMap(Item.CODEC.xmap(Holder::value, Functions.compose(Objects::requireNonNull, BuiltInRegistries.ITEM::wrapAsHolder)), Codec.DOUBLE);

    public GrizzlyEntity(BlockPos pos, BlockState state) {
        super(BasaltCrusher.GRIZZLY_ENTITY, pos, state);

        this.storageCache = new EnumMap<>(Direction.class);

        this.processingTimeTotal = 16;
        this.processingTime = 0;

        lastInput = Items.COARSE_DIRT.getDefaultInstance();

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
        public int[] getSlotsForFace(Direction direction) {
            Direction facing = GrizzlyEntity.this.getBlockState().getValue(GrizzlyBlock.FACING);

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
                    // gravel is fetched, not inputted
                    retVal = stack.is(Items.COARSE_DIRT);
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
        public void setChanged() {
            GrizzlyEntity.this.setChanged();
        }
    };

    public Storage<ItemVariant> getSidedStorage(Direction direction) {
        if (this.storageCache.get(direction) == null) {
            this.storageCache.put(direction, InventoryStorage.of(this.inventory, direction));
        }

        return this.storageCache.get(direction);
    }

    // Provide the stockpile levels to the menu.
    private final ContainerData propertyDelegate = new ContainerData() {
        @Override
        public int get(int index) {
            if (TerrestriaIntegration.ENABLED) {
                return switch (index) {
                    case 0 -> GrizzlyEntity.this.lastInput.is(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM) ? 1 : 0;
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
        public int getCount() {
            return 6;
        }
    };

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        inventory.storeAsItemList(view.list("Inventory", ItemStack.OPTIONAL_CODEC));

        view.putInt("ProcessingTimeTotal", this.processingTimeTotal);
        view.putInt("ProcessingTime", this.processingTime);

        view.store("LastInput", ItemStack.OPTIONAL_CODEC, this.lastInput);

        view.store("stockpile", STOCKPILE_CODEC, this.stockpile);

        super.saveAdditional(view);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);

        inventory.fromItemList(view.listOrEmpty("Inventory", ItemStack.OPTIONAL_CODEC));

        processingTimeTotal = view.getIntOr("ProcessingTimeTotal", 0);
        processingTime = view.getIntOr("ProcessingTime", 0);

        ItemStack stack = view.read("LastInput", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        if (stack.isEmpty()) {
            lastInput = Items.COARSE_DIRT.getDefaultInstance();
        } else {
            stack.setCount(1);
            lastInput = stack;
        }

        stockpile.putAll(view.read("stockpile", STOCKPILE_CODEC).orElse(Map.of()));
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new GrizzlyScreenHandler(syncId, playerInventory, this.inventory, this.propertyDelegate);
    }

    @SuppressWarnings("unused")
    public static void tick(Level world, BlockPos pos, BlockState state, GrizzlyEntity entity) {
        if (!world.isClientSide()) {
            entity.tickGrizzly(world, pos);
        }
    }

    private void tickGrizzly(Level world, BlockPos pos) {
        if (this.processingTime > 0) {
            --this.processingTime;
            this.setChanged();
            return;
        }

        ItemStack input  = this.inventory.getItem(0);
        ItemStack coarse = this.inventory.getItem(1);
        ItemStack fine   = this.inventory.getItem(2);

        // If we can insert gravel to our input slot we will try to get some from a crusher above.
        if (input.getCount() < input.getMaxStackSize()) {
            if (world.getBlockEntity(pos.relative(Direction.UP)) instanceof BasaltCrusherEntity companion) {
                Storage<ItemVariant> source = companion.getSidedStorage(Direction.DOWN);

                try (Transaction transaction = Transaction.openOuter()) {
                    if ((input.isEmpty() || input.is(Items.GRAVEL)) && source.extract(ItemVariant.of(Items.GRAVEL), 1, transaction) > 0) {
                        input = new ItemStack(Items.GRAVEL, input.getCount() + 1);
                        this.inventory.setItem(0, input);
                        transaction.commit();

                        // Input at Item Hopper speed.
                        this.processingTime = 8;
                        this.setChanged();
                    } else if (TerrestriaIntegration.ENABLED &&
                            (input.isEmpty() || input.is(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM))
                            && source.extract(ItemVariant.of(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM), 1, transaction) > 0) {
                        input = new ItemStack(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM, input.getCount() + 1);
                        this.inventory.setItem(0, input);
                        transaction.commit();

                        // Input at Item Hopper speed.
                        this.processingTime = 8;
                        this.setChanged();
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
        if (coarse.isEmpty() || (coarse.is(Items.GRAVEL) && coarse.getCount() < coarse.getMaxStackSize())) {
            if (input.is(Items.GRAVEL) && (fine.isEmpty() || (fine.is(Items.SAND) && fine.getCount() < fine.getMaxStackSize()))) {
                // RECIPE: 4 gravel yields 3 gravel and 1 sand
                input.shrink(1);
                this.inventory.setItem(0, input);

                // Increment the gravel fraction; maybe move some to the coarse output.
                double gravel = this.stockpile.get(Items.GRAVEL) + 0.75d;
                if (gravel >= 1.0d) {
                    gravel -= 1.0d;
                    if (coarse.isEmpty()) {
                        coarse = new ItemStack(Items.GRAVEL, 1);
                    } else {
                        coarse.grow(1);
                    }
                    this.inventory.setItem(1, coarse);
                }
                this.stockpile.put(Items.GRAVEL, gravel);

                // Increment the sand fraction; maybe move some to the fine output.
                double sand = this.stockpile.get(Items.SAND) + 0.25d;
                if (sand >= 1.0d) {
                    sand -= 1.0d;
                    if (fine.isEmpty()) {
                        fine = new ItemStack(Items.SAND, 1);
                    } else {
                        fine.grow(1);
                    }
                    this.inventory.setItem(2, fine);
                }
                this.stockpile.put(Items.SAND, sand);

                this.processingTime = this.processingTimeTotal;
                this.setChanged();
            } else if (input.is(Items.COARSE_DIRT) && (fine.isEmpty() || (fine.is(Items.DIRT) && fine.getCount() < fine.getMaxStackSize()))) {
                // RECIPE: 2 coarse dirt yields 1 gravel and 1 dirt
                input.shrink(1);
                this.inventory.setItem(0, input);

                // Increment the gravel fraction; maybe move some to the coarse output.
                double gravel = this.stockpile.get(Items.GRAVEL) + 0.5d;
                if (gravel >= 1.0d) {
                    gravel -= 1.0d;
                    if (coarse.isEmpty()) {
                        coarse = new ItemStack(Items.GRAVEL, 1);
                    } else {
                        coarse.grow(1);
                    }
                    this.inventory.setItem(1, coarse);
                }
                this.stockpile.put(Items.GRAVEL, gravel);

                // Increment the dirt fraction; maybe move some to the fine output.
                double dirt = this.stockpile.get(Items.DIRT) + 0.5d;
                if (dirt >= 1.0d) {
                    dirt -= 1.0d;
                    if (fine.isEmpty()) {
                        fine = new ItemStack(Items.DIRT, 1);
                    } else {
                        fine.grow(1);
                    }
                    this.inventory.setItem(2, fine);
                }
                this.stockpile.put(Items.DIRT, dirt);

                this.processingTime = this.processingTimeTotal;
                this.setChanged();
            }
        }

        // Second pass for Terrestria integration
        if (TerrestriaIntegration.ENABLED && (coarse.isEmpty() || (coarse.is(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM) && coarse.getCount() < coarse.getMaxStackSize()))) {
            if (input.is(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM) && (fine.isEmpty() || (fine.is(TerrestriaIntegration.VOLCANIC_SAND_ITEM) && fine.getCount() < fine.getMaxStackSize()))) {
                // RECIPE: 4 gravel yields 3 gravel and 1 sand
                input.shrink(1);
                this.inventory.setItem(0, input);

                // Increment the gravel fraction; maybe move some to the coarse output.
                double gravel = this.stockpile.get(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM) + 0.75d;
                if (gravel >= 1.0d) {
                    gravel -= 1.0d;
                    if (coarse.isEmpty()) {
                        coarse = new ItemStack(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM, 1);
                    } else {
                        coarse.grow(1);
                    }
                    this.inventory.setItem(1, coarse);
                }
                this.stockpile.put(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM, gravel);

                // Increment the sand fraction; maybe move some to the fine output.
                double sand = this.stockpile.get(TerrestriaIntegration.VOLCANIC_SAND_ITEM) + 0.25d;
                if (sand >= 1.0d) {
                    sand -= 1.0d;
                    if (fine.isEmpty()) {
                        fine = new ItemStack(TerrestriaIntegration.VOLCANIC_SAND_ITEM, 1);
                    } else {
                        fine.grow(1);
                    }
                    this.inventory.setItem(2, fine);
                }
                this.stockpile.put(TerrestriaIntegration.VOLCANIC_SAND_ITEM, sand);

                this.processingTime = this.processingTimeTotal;
                this.setChanged();
            }
        }
    }

    public void scatterInventory(Level world, BlockPos pos) {
        Containers.dropContents(world, pos, this.inventory);
    }

    public int calculateComparatorOutput() {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(this.inventory);
    }
}