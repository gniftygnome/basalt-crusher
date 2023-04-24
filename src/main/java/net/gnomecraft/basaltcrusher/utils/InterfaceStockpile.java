package net.gnomecraft.basaltcrusher.utils;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;

public interface InterfaceStockpile {
    /**
     * BasaltCrusher mod method to draw stockpile inventories, which contain fractional stacks (f.e. 1.5 Gravel).
     *
     * @param textRenderer TextRenderer - The text renderer to use when rendering the stack count
     * @param stack ItemStack - An item stack of the item to render
     * @param x int - Screen X coordinate to render the stack at
     * @param y int - Screen Y coordinate to render the stack at
     * @param quantity float - The fractional quantity in the stockpile
     */
    default void basaltCrusher$drawStockpile(TextRenderer textRenderer, ItemStack stack, int x, int y, float quantity) {}
}
