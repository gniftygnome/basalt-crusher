package net.gnomecraft.basaltcrusher.mill;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class GravelMillScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    PropertyDelegate propertyDelegate;

    public GravelMillScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(3), new ArrayPropertyDelegate(2));
    }

    public GravelMillScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(BasaltCrusher.GRAVEL_MILL_SCREEN_HANDLER, syncId);

        checkSize(inventory, 3);
        this.inventory = inventory;

        checkDataCount(propertyDelegate, 2);
        this.propertyDelegate = propertyDelegate;

        this.inventory.onOpen(playerInventory.player);
        this.addProperties(propertyDelegate);

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
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public void onSlotClick(int slotNumber, int button, SlotActionType action, PlayerEntity player) {
        ItemStack newStack = this.getCursorStack();

        // Filter swaps on the Mill inventory for acceptable items in.
        if ((action == SlotActionType.PICKUP || action == SlotActionType.PICKUP_ALL || action == SlotActionType.QUICK_CRAFT) && !newStack.isEmpty() && slotNumber >= 0 && slotNumber < this.slots.size()) {
            switch (slotNumber) {
                case 0:
                    // input slot
                    if (newStack.isOf(Items.GRAVEL) || newStack.isOf(Items.SAND)) {
                        super.onSlotClick(slotNumber, button, action, player);
                    }
                    break;
                case 1:
                    // rod charge slot
                    if (newStack.isOf(BasaltCrusher.MILL_ROD_CHARGE_ITEM)) {
                        super.onSlotClick(slotNumber, button, action, player);
                    }
                    break;
                case 2:
                    // output slot
                    // (nothing is acceptable)
                    break;
                default:
                    super.onSlotClick(slotNumber, button, action, player);
                    break;
            }
        } else {
            super.onSlotClick(slotNumber, button, action, player);
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        // Reimplement to filter transfers to the Mill inventory for acceptable items & counts in.
        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (invSlot < this.inventory.size()) {
                // From the Mill inventory to the Player.
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From the Player inventory to the Mill.
                if (originalStack.isOf(BasaltCrusher.MILL_ROD_CHARGE_ITEM)) {
                    // Try to place a rod charge into the rod charge slot.
                    ItemStack targetStack = this.inventory.getStack(1).copy();
                    if (targetStack.isEmpty()) {
                        this.inventory.setStack(1, originalStack.split(1));
                        this.slots.get(1).markDirty();
                    }

                    // If the process above did not move any items.
                    if (ItemStack.areEqual(originalStack, newStack)) {
                        return ItemStack.EMPTY;
                    }
                } else if (originalStack.isOf(Items.GRAVEL) || originalStack.isOf(Items.SAND)) {
                    // Then try to place acceptable inputs into the input slot.
                    if (!this.insertItem(originalStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    // If we don't even try to do anything, we have to return EMPTY or the game locks up...
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
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