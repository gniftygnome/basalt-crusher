package net.gnomecraft.basaltcrusher.utils;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Direction;

public abstract class BasaltCrusherInventory extends SimpleInventory implements SidedInventory {

    public BasaltCrusherInventory(int size) {
        super(size);
    }

    @Override
    public abstract boolean canInsert(int slot, ItemStack stack, Direction direction);

    @Override
    public abstract boolean canExtract(int slot, ItemStack stack, Direction direction);

    // Override SimpleInventory's readDataList and toDataList because we use OPTIONAL_CODEC to serialize empty stacks.

    @Override
    public void readDataList(ReadView.TypedListReadView<ItemStack> list) {
        java.util.Iterator<ItemStack> iterator = list.iterator();

        if (list.isEmpty()) {
            // Uninitialized BE (I am not sure why this happens, but it does at placement).
            this.clear();
        } else {
            for (int slot = 0; slot < this.size(); ++slot) {
                if (iterator.hasNext()) {
                    this.setStack(slot, iterator.next());
                } else {
                    // Should never happen, but this function must not throw.
                    BasaltCrusher.LOGGER.warn("Deserializing null for slot {}; previous value: {}", slot, this.getStack(slot));
                    this.setStack(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public void toDataList(WriteView.ListAppender<ItemStack> list) {
        for (int slot = 0; slot < this.size(); ++slot) {
            list.add(this.getStack(slot));
        }
    }
}