package net.gnomecraft.basaltcrusher;

import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Direction;

public abstract class BasaltCrusherInventory extends SimpleInventory implements SidedInventory {

    public BasaltCrusherInventory(int size) {
        super(size);
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
    public void readNbtList(NbtList nbtList) {
        for (int slot = 0; slot < nbtList.size(); ++slot) {
            ItemStack stack = ItemStack.fromNbt(nbtList.getCompound(slot));
            this.setStack(slot, stack);
        }
    }

    @Override
    public NbtList toNbtList() {
        NbtList nbtList = new NbtList();
        for (int slot = 0; slot < this.size(); ++slot) {
            ItemStack stack = this.getStack(slot);
            nbtList.add(stack.writeNbt(new NbtCompound()));
        }
        return nbtList;
    }
}