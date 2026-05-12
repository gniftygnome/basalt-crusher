package net.gnomecraft.basaltcrusher.crusher;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BasaltCrusherScreenHandler extends AbstractContainerMenu {
    private final Container inventory;
    ContainerData propertyDelegate;

    public BasaltCrusherScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(5), new SimpleContainerData(2));
    }

    public BasaltCrusherScreenHandler(int syncId, Inventory playerInventory, Container inventory, ContainerData propertyDelegate) {
        super(BasaltCrusher.BASALT_CRUSHER_SCREEN_HANDLER, syncId);

        checkContainerSize(inventory, 5);
        this.inventory = inventory;

        checkContainerDataCount(propertyDelegate, 2);
        this.propertyDelegate = propertyDelegate;

        this.inventory.startOpen(playerInventory.player);
        this.addDataSlots(propertyDelegate);

        // BasaltCrusher inventory slots
        this.addSlot(new Slot(inventory, 0, 88,  35));  // input
        this.addSlot(new Slot(inventory, 1, 17,  35) {  // jaw liners
            @Override
            public int getMaxStackSize(ItemStack stack) {
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
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public void clicked(int slotNumber, int button, ClickType action, Player player) {
        ItemStack newStack = this.getCarried();

        // Filter swaps on the Crusher inventory for acceptable items in.
        if ((action == ClickType.PICKUP || action == ClickType.PICKUP_ALL || action == ClickType.QUICK_CRAFT) && !newStack.isEmpty() && slotNumber >= 0 && slotNumber < this.slots.size()) {
            switch (slotNumber) {
                case 0:
                    // input slot
                    if (newStack.is(BasaltCrusher.BASALTS)) {
                        super.clicked(slotNumber, button, action, player);
                    }
                    break;
                case 1:
                    // jaw liner slot
                    if (newStack.is(BasaltCrusher.JAW_LINERS)) {
                        super.clicked(slotNumber, button, action, player);
                    }
                    break;
                case 2:
                    // output slot
                    // (nothing is acceptable)
                    break;
                case 3:
                    // top crushing slot
                    if (newStack.is(BasaltCrusher.JAW_LINERS) && newStack.getCount() == 1 && !ItemStack.isSameItemSameComponents(newStack, this.inventory.getItem(3))) {
                        super.clicked(slotNumber, button, action, player);
                    }
                    break;
                case 4:
                    // bottom crushing slot
                    if (newStack.is(BasaltCrusher.JAW_LINERS) && newStack.getCount() == 1 && !ItemStack.isSameItemSameComponents(newStack, this.inventory.getItem(4))) {
                        super.clicked(slotNumber, button, action, player);
                    }
                    break;
                default:
                    super.clicked(slotNumber, button, action, player);
                    break;
            }
        } else {
            super.clicked(slotNumber, button, action, player);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        // Reimplement to filter transfers to the Crusher inventory for acceptable items & counts in.
        if (slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();

            if (invSlot < this.inventory.getContainerSize()) {
                // From the Crusher inventory to the Player.
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From the Player inventory to the Crusher.
                if (originalStack.is(BasaltCrusher.JAW_LINERS)) {
                    // First try to place one jaw liner in the top crushing slot.
                    if (this.inventory.getItem(3).isEmpty()) {
                        this.inventory.setItem(3, originalStack.split(1));
                        this.slots.get(3).setChanged();
                    }

                    // Next try to place one jaw liner in the bottom crushing slot.
                    if (this.inventory.getItem(4).isEmpty()) {
                        this.inventory.setItem(4, originalStack.split(1));
                        this.slots.get(4).setChanged();
                    }

                    // Finally, try to place up to a stack of jaw liners into the jaw liner slot.
                    ItemStack targetStack = this.inventory.getItem(1).copy();
                    if (targetStack.isEmpty()) {
                        this.inventory.setItem(1, originalStack.split(originalStack.getCount()));
                        this.slots.get(1).setChanged();
                    } else if (ItemStack.isSameItemSameComponents(originalStack, targetStack)) {
                        int insertable = Math.min(originalStack.getCount(), 16 - targetStack.getCount());
                        if (insertable > 0) {
                            originalStack.shrink(insertable);
                            targetStack.grow(insertable);
                            this.inventory.setItem(1, targetStack);
                            this.slots.get(1).setChanged();
                        }
                    }

                    // If neither process above moved any items.
                    if (ItemStack.matches(originalStack, newStack)) {
                        return ItemStack.EMPTY;
                    }
                } else if (originalStack.is(BasaltCrusher.BASALTS)) {
                    // Then try to place anything basalt into the input slot.
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