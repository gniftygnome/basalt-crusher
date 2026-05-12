package net.gnomecraft.basaltcrusher.utils;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.Direction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract class BasaltCrusherInventory extends SimpleContainer implements WorldlyContainer {

    public BasaltCrusherInventory(int size) {
        super(size);
    }

    @Override
    public abstract boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction);

    @Override
    public abstract boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction);

    // Override SimpleInventory's readDataList and toDataList because we use OPTIONAL_CODEC to serialize empty stacks.

    @Override
    public void fromItemList(ValueInput.TypedInputList<ItemStack> list) {
        java.util.Iterator<ItemStack> iterator = list.iterator();

        if (list.isEmpty()) {
            // Uninitialized BE (I am not sure why this happens, but it does at placement).
            this.clearContent();
        } else {
            for (int slot = 0; slot < this.getContainerSize(); ++slot) {
                if (iterator.hasNext()) {
                    this.setItem(slot, iterator.next());
                } else {
                    // Should never happen, but this function must not throw.
                    BasaltCrusher.LOGGER.warn("Deserializing null for slot {}; previous value: {}", slot, this.getItem(slot));
                    this.setItem(slot, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public void storeAsItemList(ValueOutput.TypedOutputList<ItemStack> list) {
        for (int slot = 0; slot < this.getContainerSize(); ++slot) {
            list.add(this.getItem(slot));
        }
    }
}