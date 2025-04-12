package net.gnomecraft.basaltcrusher.utils;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.Direction;

public abstract class BasaltCrusherInventory extends SimpleInventory implements SidedInventory {

    public BasaltCrusherInventory(int size) {
        super(size);
    }

    @Override
    public abstract boolean canInsert(int slot, ItemStack stack, Direction direction);

    @Override
    public abstract boolean canExtract(int slot, ItemStack stack, Direction direction);

    // Override so we can use OPTIONAL_CODEC which tolerates empty stacks.
    @Override
    public void readNbtList(NbtList nbtList, RegistryWrapper.WrapperLookup registryLookup) {
        RegistryOps<NbtElement> registryOps = registryLookup.getOps(NbtOps.INSTANCE);

        for (int slot = 0; slot < nbtList.size(); ++slot) {
            ItemStack stack = ItemStack.OPTIONAL_CODEC
                    .parse(registryOps, nbtList.getCompoundOrEmpty(slot))
                    .resultOrPartial(error -> BasaltCrusher.LOGGER.error("Tried to load invalid item: '{}'", error))
                    .orElse(ItemStack.EMPTY);
            this.setStack(slot, stack);
        }
    }

    // Override so we can use OPTIONAL_CODEC which tolerates empty stacks.
    @Override
    public NbtList toNbtList(RegistryWrapper.WrapperLookup registryLookup) {
        RegistryOps<NbtElement> registryOps = registryLookup.getOps(NbtOps.INSTANCE);
        NbtList nbtList = new NbtList();

        for (int slot = 0; slot < this.size(); ++slot) {
            ItemStack stack = this.getStack(slot);
            nbtList.add(ItemStack.OPTIONAL_CODEC.encodeStart(registryOps, stack).getOrThrow());
        }

        return nbtList;
    }
}