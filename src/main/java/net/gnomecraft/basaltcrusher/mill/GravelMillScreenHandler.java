package net.gnomecraft.basaltcrusher.mill;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GravelMillScreenHandler extends AbstractContainerMenu {
    private final Container inventory;
    ContainerData propertyDelegate;

    public GravelMillScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(3), new SimpleContainerData(2));
    }

    public GravelMillScreenHandler(int syncId, Inventory playerInventory, Container inventory, ContainerData propertyDelegate) {
        super(BasaltCrusher.GRAVEL_MILL_SCREEN_HANDLER, syncId);

        checkContainerSize(inventory, 3);
        this.inventory = inventory;

        checkContainerDataCount(propertyDelegate, 2);
        this.propertyDelegate = propertyDelegate;

        this.inventory.startOpen(playerInventory.player);
        this.addDataSlots(propertyDelegate);

        // GravelMill inventory slots
        this.addSlot(new Slot(inventory, 0, 17,  35));  // input
        this.addSlot(new Slot(inventory, 1, 68,  35));  // rod charge
        this.addSlot(new Slot(inventory, 2, 136, 35));  // output

        // Player inventory slots
        for (int m = 0; m < 3; ++m) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }

        // Player hotbar slots
        for (int m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public void clicked(int slotNumber, int button, ContainerInput input, Player player) {
        ItemStack newStack = this.getCarried();

        // Filter swaps on the Mill inventory for acceptable items in.
        if ((input == ContainerInput.PICKUP || input == ContainerInput.PICKUP_ALL || input == ContainerInput.QUICK_CRAFT) && !newStack.isEmpty() && slotNumber >= 0 && slotNumber < this.slots.size()) {
            switch (slotNumber) {
                case 0:
                    // input slot
                    if (newStack.is(Items.GRAVEL) || newStack.is(Items.SAND)) {
                        super.clicked(slotNumber, button, input, player);
                    }
                    break;
                case 1:
                    // rod charge slot
                    if (newStack.is(BasaltCrusher.MILL_ROD_CHARGE_ITEM)) {
                        super.clicked(slotNumber, button, input, player);
                    }
                    break;
                case 2:
                    // output slot
                    // (nothing is acceptable)
                    break;
                default:
                    super.clicked(slotNumber, button, input, player);
                    break;
            }
        } else {
            super.clicked(slotNumber, button, input, player);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        // Reimplement to filter transfers to the Mill inventory for acceptable items & counts in.
        if (slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();

            if (invSlot < this.inventory.getContainerSize()) {
                // From the Mill inventory to the Player.
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From the Player inventory to the Mill.
                if (originalStack.is(BasaltCrusher.MILL_ROD_CHARGE_ITEM)) {
                    // Try to place a rod charge into the rod charge slot.
                    ItemStack targetStack = this.inventory.getItem(1).copy();
                    if (targetStack.isEmpty()) {
                        this.inventory.setItem(1, originalStack.split(1));
                        this.slots.get(1).setChanged();
                    }

                    // If the process above did not move any items.
                    if (ItemStack.matches(originalStack, newStack)) {
                        return ItemStack.EMPTY;
                    }
                } else if (originalStack.is(Items.GRAVEL) || originalStack.is(Items.SAND)) {
                    // Then try to place acceptable inputs into the input slot.
                    if (!this.moveItemStackTo(originalStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    // If we don't even try to do anything, we have to return EMPTY or the game locks up...
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return newStack;
    }

    // Milling progress as a fraction of 24 (the size of the arrow image).
    public int crushProgress24() {
        int millTime = propertyDelegate.get(0);
        int millTimeTotal = propertyDelegate.get(1);

        if (millTimeTotal <= 0) {
            millTimeTotal = 200;
        }

        return (millTime * 24) / millTimeTotal;
    }
}