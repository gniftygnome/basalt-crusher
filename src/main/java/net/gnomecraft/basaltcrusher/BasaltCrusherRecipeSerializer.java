package net.gnomecraft.basaltcrusher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BasaltCrusherRecipeSerializer implements RecipeSerializer<BasaltCrusherRecipe> {
    // This will be the "type" field in the JSON.
    public static final Identifier ID = new Identifier("basalt-crusher:basalt_crusher_recipe");

    private BasaltCrusherRecipeSerializer() {}
    public static final BasaltCrusherRecipeSerializer INSTANCE = new BasaltCrusherRecipeSerializer();

    // Utility class for GSON to convert JSON into Recipe
    public static class BasaltCrusherRecipeJsonFormat {
        JsonObject inputA;
        JsonObject inputB;
        String outputItem;
        int outputAmount;
    }

    // Turns JSON into Recipe.
    @Override
    public BasaltCrusherRecipe read(Identifier id, JsonObject json) {

        BasaltCrusherRecipeJsonFormat recipeJson = new Gson().fromJson(json, BasaltCrusherRecipeJsonFormat.class);
        if (recipeJson.inputA == null || recipeJson.inputB == null || recipeJson.outputItem == null) {
            throw new JsonSyntaxException("A required Basalt Crusher recipe attribute is missing!");
        }
        if (recipeJson.outputAmount == 0) recipeJson.outputAmount = 1;

        Ingredient inputA = Ingredient.fromJson(recipeJson.inputA);
        Ingredient inputB = Ingredient.fromJson(recipeJson.inputB);

        // The JSON will specify the item ID.
        // We can get the Item instance based off of that from the Item registry.
        Item outputItem = Registry.ITEM.getOrEmpty(new Identifier(recipeJson.outputItem))
                // Validate the inputted item actually exists
                .orElseThrow(() -> new JsonSyntaxException("Basalt Crusher recipe: no such item '" + recipeJson.outputItem + "'"));
        ItemStack outputStack = new ItemStack(outputItem, recipeJson.outputAmount);

        return new BasaltCrusherRecipe(id, outputStack, inputA, inputB);
    }

    // Turns Recipe into PacketByteBuf.
    @Override
    public void write(PacketByteBuf packetData, BasaltCrusherRecipe recipe) {

        recipe.getInputA().write(packetData);
        recipe.getInputB().write(packetData);
        packetData.writeItemStack(recipe.getOutput());
    }

    // Turns PacketByteBuf into Recipe.
    @Override
    public BasaltCrusherRecipe read(Identifier id, PacketByteBuf packetData) {

        // Be certain to read in the same order you have written!
        Ingredient inputA = Ingredient.fromPacket(packetData);
        Ingredient inputB = Ingredient.fromPacket(packetData);
        ItemStack output = packetData.readItemStack();

        return new BasaltCrusherRecipe(id, output, inputA, inputB);
    }
}