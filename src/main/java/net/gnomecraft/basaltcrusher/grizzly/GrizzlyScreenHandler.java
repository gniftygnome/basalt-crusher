package net.gnomecraft.basaltcrusher.grizzly;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.gnomecraft.basaltcrusher.utils.TerrestriaIntegration;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class GrizzlyScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    PropertyDelegate propertyDelegate;

    public GrizzlyScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(3), new ArrayPropertyDelegate(6));
    }

    public GrizzlyScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(BasaltCrusher.GRIZZLY_SCREEN_HANDLER, syncId);

        checkSize(inventory, 3);
        this.inventory = inventory;

        checkDataCount(propertyDelegate, 6);
        this.propertyDelegate = propertyDelegate;

        this.inventory.onOpen(playerInventory.player);
        this.addProperties(propertyDelegate);

        // Grizzly inventory slots
        this.addSlot(new Slot(inventory, 0, 80,  10));  // input
        this.addSlot(new Slot(inventory, 1, 26,  51));  // coarse output
        this.addSlot(new Slot(inventory, 2, 134, 51));  // fine output

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

        // Filter swaps on the Grizzly inventory for acceptable items in.
        if ((action == SlotActionType.PICKUP || action == SlotActionType.PICKUP_ALL || action == SlotActionType.QUICK_CRAFT) && !newStack.isEmpty() && slotNumber >= 0 && slotNumber < this.slots.size()) {
            switch (slotNumber) {
                case 0:
                    // input slot
                    if (newStack.isOf(Items.COARSE_DIRT)) {
                        super.onSlotClick(slotNumber, button, action, player);
                    }
                    break;
                case 1:
                    // coarse output slot
                    // (nothing is acceptable)
                    break;
                case 2:
                    // fine output slot
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
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        // Reimplement to filter transfers to the Grizzly inventory for acceptable items in.
        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (invSlot < this.inventory.size()) {
                // From the Grizzly inventory to the Player.
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From the Player inventory to the Grizzly.
                if (originalStack.isOf(Items.COARSE_DIRT)) {
                    // Try to place up to a stack of any acceptable item into the input slot.
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

    // Stockpile level as a fraction of one of the given block.
    // This is a rounded version of the real thing AND it can briefly meet or exceed 1.0f.
    public float stockpileOf(Item item) {
        if (item == Items.AIR) {
            return propertyDelegate.get(0);
        } else if (item == Items.DIRT) {
            return propertyDelegate.get(1) / 100.0f;
        } else if (item == Items.GRAVEL) {
            return propertyDelegate.get(2) / 100.0f;
        } else if (item == Items.SAND) {
            return propertyDelegate.get(3) / 100.0f;
        } else if (TerrestriaIntegration.ENABLED && item == TerrestriaIntegration.BLACK_GRAVEL_ITEM) {
            return propertyDelegate.get(4) / 100.0f;
        } else if (TerrestriaIntegration.ENABLED && item == TerrestriaIntegration.BLACK_SAND_ITEM) {
            return propertyDelegate.get(5) / 100.0f;
        } else {
            return 0.0f;
        }
    }
}