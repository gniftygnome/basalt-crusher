package net.gnomecraft.basaltcrusher;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.*;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Iterator;

import static net.gnomecraft.basaltcrusher.BasaltCrusherBlock.CRUSHING_STATE;

public class BasaltCrusherEntity extends LockableContainerBlockEntity implements RecipeInputProvider, RecipeUnlocker, SidedInventory {
    private DefaultedList<ItemStack> inventory;
    private Identifier lastRecipe;
    private final DefaultedList<Recipe<?>> recipesUsed;
    private final RecipeType<BasaltCrusherRecipe> recipeType;

    private static final int[] TOP_SLOTS = new int[] {0};
    private static final int[] SIDE_SLOTS = new int[] {1};
    private static final int[] BOTTOM_SLOTS = new int[] {2};
    // Crushing slot cannot be targeted: {3}

    private BasaltCrusherBlock.CrushingState crushingState;

    private int crushTimeTotal;
    private int crushTime;

    private float expPerCrush;
    private float expAccumulated;

    public BasaltCrusherEntity(BlockPos pos, BlockState state) {
        super(BasaltCrusher.BASALT_CRUSHER_ENTITY, pos, state);

        this.inventory = DefaultedList.ofSize(4, ItemStack.EMPTY);
        this.recipesUsed = DefaultedList.of();
        this.recipeType = BasaltCrusherRecipe.Type.INSTANCE;

        this.crushingState = BasaltCrusherBlock.CrushingState.EMPTY;

        // Our mod is a simple mod.
        this.crushTimeTotal = 200;
        this.crushTime = 0;
        this.expPerCrush = 0.1F;
        this.expAccumulated = 0.0F;
    }

    @Override
    public ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new BasaltCrusherScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public Text getContainerName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        Inventories.writeNbt(tag, this.inventory);

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

        inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readNbt(tag, this.inventory);

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
        ItemStack liners = entity.getStack(1);
        ItemStack crushing = entity.getStack(3);

        // Make sure there is a jaw liner in the active slot if we have one available.
        if (crushing.isEmpty()) {
            if (liners.isEmpty()) {
                // Update the display because we have no jaw liners available.
                entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.EMPTY);
            } else {
                // Move a jaw liner into the jaws slot.
                entity.setStack(3, entity.removeStack(1, 1));
                entity.markDirty();
            }
        }
    }

    private void tickCrusher(World world, BlockPos pos, BlockState state, BasaltCrusherEntity entity) {
        ItemStack input = entity.getStack(0);
        ItemStack output = entity.getStack(2);
        ItemStack crushing = entity.getStack(3);

        // We can't crush if our output is full.  Short circuit.
        if (output.getCount() == output.getMaxCount()) {
            entity.setCrushingState(state, BasaltCrusherBlock.CrushingState.IDLE);

            return;
        }

        if (!crushing.isEmpty()) {
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
                entity.setStack(2, new ItemStack(Blocks.GRAVEL, 1));
            } else {
                output.increment(1);
            }
            // Damage the jaw liner (if possible).
            if (crushing.isDamageable()) {
                if ((1.0d / (1.0d + (double)EnchantmentHelper.getLevel(Enchantments.UNBREAKING, crushing))) > world.random.nextDouble()) {
                    crushing.setDamage(crushing.getDamage() + 1);
                }
                if (crushing.getDamage() >= crushing.getMaxDamage()) {
                    crushing.decrement(1);
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
    public boolean canInsert(int index, ItemStack stack, Direction direction) {
        return this.isValid(index, stack);
    }

    @Override
    public boolean canExtract(int index, ItemStack stack, Direction direction) {
        // Allow extracting anything from any slot that matches the direction.
        return true;
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        Iterator<ItemStack> invIterator = this.inventory.iterator();

        ItemStack stack;
        do {
            if (!invIterator.hasNext()) {
                return true;
            }

            stack = (ItemStack) invIterator.next();
        } while (stack.isEmpty());

        return false;
    }

    @Override
    public ItemStack getStack(int index) {
        return this.inventory.get(index);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.inventory, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        ItemStack target = this.inventory.get(index);
        boolean sameItem = !stack.isEmpty() && stack.isItemEqualIgnoreDamage(target) && ItemStack.areNbtEqual(stack, target);

        this.inventory.set(index, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }

        if (index == 0 && !sameItem) {
            this.crushTime = 0;
            this.markDirty();
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (this.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return player.squaredDistanceTo((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public boolean isValid(int index, ItemStack stack) {
        Item newItem = stack.isEmpty() ? Items.AIR : stack.getItem();
        boolean retVal = false;

        switch (index) {
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
                // crushing slot (active jaw liner)
                // TODO: use the recipe
                retVal = (stack.isIn(BasaltCrusher.JAW_LINERS) && stack.getCount() == 1 && !stack.isItemEqual(this.inventory.get(3)));
                break;
        }

        return retVal;
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    @Override
    public void setLastRecipe(Recipe<?> recipe) {
        if (recipe != null) {
            lastRecipe = recipe.getId();
        }
    }

    @Override
    public Recipe<?> getLastRecipe() {
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
        for (ItemStack stack : this.inventory) {
            finder.addInput(stack);
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
