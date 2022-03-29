package net.gnomecraft.basaltcrusher;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.*;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.EnumMap;

import static net.gnomecraft.basaltcrusher.BasaltCrusherBlock.CRUSHING_STATE;

public class BasaltCrusherEntity extends BlockEntity implements NamedScreenHandlerFactory, RecipeInputProvider, RecipeUnlocker {
    private BasaltCrusherBlock.CrushingState crushingState;
    private EnumMap<Direction, Storage<ItemVariant>> storageCache;

    private Identifier lastRecipe;
    private final DefaultedList<Recipe<?>> recipesUsed;
    private final RecipeType<BasaltCrusherRecipe> recipeType;

    private int crushTimeTotal;
    private int crushTime;

    private float expPerCrush;
    private float expAccumulated;

    public BasaltCrusherEntity(BlockPos pos, BlockState state) {
        super(BasaltCrusher.BASALT_CRUSHER_ENTITY, pos, state);

        // Initialize cached crushing state.
        this.crushingState = state.get(CRUSHING_STATE);
        this.storageCache = new EnumMap<>(Direction.class);

        // Recipe support (currently unused).
        this.recipesUsed = DefaultedList.of();
        this.recipeType = BasaltCrusherRecipe.Type.INSTANCE;

        // Our mod is a simple mod.
        this.crushTimeTotal = 200;
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
        public boolean isValid(int slot, ItemStack stack) {
            boolean retVal = false;

            switch (slot) {
                case 0:
                    // input slot
                    // TODO: use the recipe
                    retVal = stack.isIn(BasaltCrusher.BASALTS);
                    break;
                case 1:
                    // jaw liner slot
                    // TODO: use the recipe
                    retVal = stack.isIn(BasaltCrusher.JAW_LINERS);
                    break;
                case 2:
                    // output slot
                    break;
                case 3:
                    // top crushing slot (active jaw liner)
                    // TODO: use the recipe
                    retVal = (stack.isIn(BasaltCrusher.JAW_LINERS) && stack.getCount() == 1 && !stack.isItemEqual(this.getStack(3)));
                    break;
                case 4:
                    // bottom crushing slot (active jaw liner)
                    // TODO: use the recipe
                    retVal = (stack.isIn(BasaltCrusher.JAW_LINERS) && stack.getCount() == 1 && !stack.isItemEqual(this.getStack(4)));
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
            boolean sameItem = !stack.isEmpty() && stack.isItemEqualIgnoreDamage(target) && ItemStack.areNbtEqual(stack, target);

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

        @Override
        protected int getCapacity(ItemVariant itemVariant) {
            return 16;
        }
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
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        tag.put("Inventory", this.inventory.toNbtList());

        if (lastRecipe != null) {
            tag.putString("LastRecipeLocation", lastRecipe.toString());
        }

        tag.putShort("CrushTimeTotal", (short) this.crushTimeTotal);
        tag.putShort("CrushTime", (short) this.crushTime);
        tag.putFloat("ExpPerCrush", expPerCrush);
        tag.putFloat("ExpAccumulated", expAccumulated);

        super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        inventory.readNbtList(tag.getList("Inventory", NbtList.COMPOUND_TYPE));

        String lastRecipeLocation = tag.getString("LastRecipeLocation");
        if (lastRecipeLocation != null && !lastRecipeLocation.isEmpty()) {
            lastRecipe = new Identifier(lastRecipeLocation);
        }

        crushTimeTotal = tag.getShort("CrushTimeTotal");
        crushTime = tag.getShort("CrushTime");
        expPerCrush = tag.getFloat("ExpPerCrush");
        expAccumulated = tag.getFloat("ExpAccumulated");
    }

    public static void tick(World world, BlockPos pos, BlockState state, BasaltCrusherEntity entity) {
        entity.tickJawLiner(world, pos, state, entity);
        entity.tickCrusher(world, pos, state, entity);
    }

    private void tickJawLiner(World world, BlockPos pos, BlockState state, BasaltCrusherEntity entity) {
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
            if (input.isEmpty()) {
                // We can't be crushing so ensure crushing is reset.
                entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.IDLE);
                if (entity.crushTime != 0) {
                    entity.crushTime = 0;
                    entity.markDirty();
                }
            } else {
                // Start or continue crushing.
                // The jaws cycle 4 times per 200-tick crush, every 2.5 seconds.
                switch ((int) ((crushTime % 50) / 8)) {
                    case 0 -> entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.OPEN);
                    case 1 -> entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.OPENISH);
                    case 2 -> entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.CLOSEDISH);
                    case 3 -> entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.CLOSED);
                    case 4 -> entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.CLOSEDISH);
                    case 5 -> entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.OPENISH);
                    case 6 -> entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.OPEN);
                }
                ++entity.crushTime;
                entity.markDirty();
            }
        }

        if (entity.crushTime >= entity.crushTimeTotal) {
            // Successful crushing.
            // TODO: use the recipe (probably not; we only make gravel).
            input.decrement(1);
            if (output.isEmpty()) {
                entity.inventory.setStack(2, new ItemStack(Blocks.GRAVEL, 1));
            } else {
                output.increment(1);
            }
            // Try to damage the top jaw liner (if possible).
            if (upperJaw.isDamageable()) {
                if ((0.5d / (1.0d + (double) EnchantmentHelper.getLevel(Enchantments.UNBREAKING, upperJaw))) > world.random.nextDouble()) {
                    upperJaw.setDamage(upperJaw.getDamage() + 1);
                }
                if (upperJaw.getDamage() >= upperJaw.getMaxDamage()) {
                    upperJaw.decrement(1);
                }
            }
            // Try to damage the bottom jaw liner (if possible).
            if (lowerJaw.isDamageable()) {
                if ((0.5d / (1.0d + (double) EnchantmentHelper.getLevel(Enchantments.UNBREAKING, lowerJaw))) > world.random.nextDouble()) {
                    lowerJaw.setDamage(lowerJaw.getDamage() + 1);
                }
                if (lowerJaw.getDamage() >= lowerJaw.getMaxDamage()) {
                    lowerJaw.decrement(1);
                }
            }
            // Add recipe utilization and XP.
            if (entity.getLastRecipe() != null) {
                entity.recipesUsed.add(entity.getLastRecipe());
            }
            entity.expAccumulated += entity.expPerCrush;
            // Reset crush timer.
            entity.crushTime = 0;
            entity.markDirty();
        }
    }

    public void scatterInventory(World world, BlockPos pos) {
        ItemScatterer.spawn(world, pos, this.inventory);
    }

    @Override
    public void setLastRecipe(Recipe<?> recipe) {
        if (recipe != null) {
            lastRecipe = recipe.getId();
        }
    }

    @Override
    public Recipe<?> getLastRecipe() {
        if (this.world == null) {
            return null;
        }

        return this.world.getRecipeManager().get(lastRecipe).orElse(null);
    }

    @Override
    public void unlockLastRecipe(PlayerEntity player) {
        player.unlockRecipes(new Identifier[] { lastRecipe });
    }

    public void dropExperience(PlayerEntity player) {
        int expOrb;

        if (player == null) return;

        while (expAccumulated >= 1.0F) {
            expOrb = ExperienceOrbEntity.roundToOrbSize((int) expAccumulated);
            expAccumulated -= expOrb;
            player.world.spawnEntity(new ExperienceOrbEntity(player.world, player.getX(), player.getY() + 0.5D, player.getZ() + 0.5D, expOrb));
        }

        this.markDirty();
    }

    @Override
    public void provideRecipeInputs(RecipeMatcher finder) {
        for (int slot = 0; slot < this.inventory.size(); ++slot) {
            finder.addInput(this.inventory.getStack(slot));
        }
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