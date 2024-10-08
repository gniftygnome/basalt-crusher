package net.gnomecraft.basaltcrusher.crusher;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class BasaltCrusherScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    PropertyDelegate propertyDelegate;

    public BasaltCrusherScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(5), new ArrayPropertyDelegate(2));
    }

    public BasaltCrusherScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate propertyDelegate) {
        super(BasaltCrusher.BASALT_CRUSHER_SCREEN_HANDLER, syncId);

        checkSize(inventory, 5);
        this.inventory = inventory;

        checkDataCount(propertyDelegate, 2);
        this.propertyDelegate = propertyDelegate;

        this.inventory.onOpen(playerInventory.player);
        this.addProperties(propertyDelegate);

        // BasaltCrusher inventory slots
        this.addSlot(new Slot(inventory, 0, 88,  35));  // input
        this.addSlot(new Slot(inventory, 1, 17,  35) {  // jaw liners
            @Override
            public int getMaxItemCount(ItemStack stack) {
                return 16;
            }
        });
        this.addSlot(new Slot(inventory, 2, 136, 35));  // output
        this.addSlot(new Slot(inventory, 3, 59,  23));  // active top jaw liner
        this.addSlot(new Slot(inventory, 4, 59,  48));  // active bottom jaw liner

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

        // Filter swaps on the Crusher inventory for acceptable items in.
        if ((action == SlotActionType.PICKUP || action == SlotActionType.PICKUP_ALL || action == SlotActionType.QUICK_CRAFT) && !newStack.isEmpty() && slotNumber >= 0 && slotNumber < this.slots.size()) {
            switch (slotNumber) {
                case 0:
                    // input slot
                    if (newStack.isIn(BasaltCrusher.BASALTS)) {
                        super.onSlotClick(slotNumber, button, action, player);
                    }
                    break;
                case 1:
                    // jaw liner slot
                    if (newStack.isIn(BasaltCrusher.JAW_LINERS)) {
                        super.onSlotClick(slotNumber, button, action, player);
                    }
                    break;
                case 2:
                    // output slot
                    // (nothing is acceptable)
                    break;
                case 3:
                    // top crushing slot
                    if (newStack.isIn(BasaltCrusher.JAW_LINERS) && newStack.getCount() == 1 && !ItemStack.areItemsAndComponentsEqual(newStack, this.inventory.getStack(3))) {
                        super.onSlotClick(slotNumber, button, action, player);
                    }
                    break;
                case 4:
                    // bottom crushing slot
                    if (newStack.isIn(BasaltCrusher.JAW_LINERS) && newStack.getCount() == 1 && !ItemStack.areItemsAndComponentsEqual(newStack, this.inventory.getStack(4))) {
                        super.onSlotClick(slotNumber, button, action, player);
                    }
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

        // Reimplement to filter transfers to the Crusher inventory for acceptable items & counts in.
        if (slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (invSlot < this.inventory.size()) {
                // From the Crusher inventory to the Player.
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From the Player inventory to the Crusher.
                if (originalStack.isIn(BasaltCrusher.JAW_LINERS)) {
                    // First try to place one jaw liner in the top crushing slot.
                    if (this.inventory.getStack(3).isEmpty()) {
                        this.inventory.setStack(3, originalStack.split(1));
                        this.slots.get(3).markDirty();
                    }

                    // Next try to place one jaw liner in the bottom crushing slot.
                    if (this.inventory.getStack(4).isEmpty()) {
                        this.inventory.setStack(4, originalStack.split(1));
                        this.slots.get(4).markDirty();
                    }

                    // Finally, try to place up to a stack of jaw liners into the jaw liner slot.
                    ItemStack targetStack = this.inventory.getStack(1).copy();
                    if (targetStack.isEmpty()) {
                        this.inventory.setStack(1, originalStack.split(originalStack.getCount()));
                        this.slots.get(1).markDirty();
                    } else if (ItemStack.areItemsAndComponentsEqual(originalStack, targetStack)) {
                        int insertable = Math.min(originalStack.getCount(), 16 - targetStack.getCount());
                        if (insertable > 0) {
                            originalStack.decrement(insertable);
                            targetStack.increment(insertable);
                            this.inventory.setStack(1, targetStack);
                            this.slots.get(1).markDirty();
                        }
                    }

                    // If neither process above moved any items.
                    if (ItemStack.areEqual(originalStack, newStack)) {
                        return ItemStack.EMPTY;
                    }
                } else if (originalStack.isIn(BasaltCrusher.BASALTS)) {
                    // Then try to place anything basalt into the input slot.
                    if (!this.insertItem(originalStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    // If we don't even try to do anything, we have to return EMPTY or the game locks up...
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStackNoCallbacks(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    // Crushing progress as a fraction of 24 (the size of the arrow image).
    public int crushProgress24() {
        int crushTime = propertyDelegate.get(0);
        int crushTimeTotal = propertyDelegate.get(1);

        if (crushTimeTotal <= 0) {
            crushTimeTotal = 420;
        }

        return (crushTime * 24) / crushTimeTotal;
    }
}