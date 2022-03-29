package net.gnomecraft.basaltcrusher;

import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class BasaltCrusherRecipe implements Recipe<SidedInventory> {

    private final Identifier id;
    private final Ingredient inputA;
    private final Ingredient inputB;
    private final ItemStack outputStack;

    public BasaltCrusherRecipe(Identifier id, ItemStack outputStack, Ingredient inputA, Ingredient inputB) {
        this.id = id;
        this.inputA = inputA;
        this.inputB = inputB;
        this.outputStack = outputStack;
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    public Ingredient getInputA() {
        return this.inputA;
    }

    public Ingredient getInputB() {
        return this.inputB;
    }

    @Override
    public ItemStack getOutput() {
        return this.outputStack;
    }

    @Override
    public boolean matches(SidedInventory inventory, World world) {
        if (inventory.size() < 2) return false;
        return inputA.test(inventory.getStack(0)) && inputB.test(inventory.getStack(1));
    }

    @Override
    public ItemStack craft(SidedInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return BasaltCrusherRecipeSerializer.INSTANCE;
    }

    public static class Type implements RecipeType<BasaltCrusherRecipe> {
		private Type() {}
        public static final Type INSTANCE = new Type();
        public static final String ID = "basalt_crusher_recipe";
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }
}