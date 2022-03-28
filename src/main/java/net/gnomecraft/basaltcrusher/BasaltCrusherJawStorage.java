package net.gnomecraft.basaltcrusher;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.item.ItemStack;

import java.util.Objects;

// Specialty Storage to hold up to 16 of any Jaw Liner
public class BasaltCrusherJawStorage extends SingleStackStorage {
    protected ItemStack inventory = ItemStack.EMPTY;

    @Override
    protected ItemStack getStack() {
        return inventory;
    }

    @Override
    protected void setStack(ItemStack stack) {
        inventory = Objects.requireNonNullElse(stack, ItemStack.EMPTY);
    }

    @Override
    protected int getCapacity(ItemVariant itemVariant) {
        return 16;
    }
}
