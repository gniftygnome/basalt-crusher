package net.gnomecraft.basaltcrusher.grizzly;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.gnomecraft.basaltcrusher.utils.TerrestriaIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GrizzlyScreenHandler extends AbstractContainerMenu {
    private final Container inventory;
    ContainerData propertyDelegate;

    public GrizzlyScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(3), new SimpleContainerData(6));
    }

    public GrizzlyScreenHandler(int syncId, Inventory playerInventory, Container inventory, ContainerData propertyDelegate) {
        super(BasaltCrusher.GRIZZLY_SCREEN_HANDLER, syncId);

        checkContainerSize(inventory, 3);
        this.inventory = inventory;

        checkContainerDataCount(propertyDelegate, 6);
        this.propertyDelegate = propertyDelegate;

        this.inventory.startOpen(playerInventory.player);
        this.addDataSlots(propertyDelegate);

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
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public void clicked(int slotNumber, int button, ClickType action, Player player) {
        ItemStack newStack = this.getCarried();

        // Filter swaps on the Grizzly inventory for acceptable items in.
        if ((action == ClickType.PICKUP || action == ClickType.PICKUP_ALL || action == ClickType.QUICK_CRAFT) && !newStack.isEmpty() && slotNumber >= 0 && slotNumber < this.slots.size()) {
            switch (slotNumber) {
                case 0:
                    // input slot
                    if (newStack.is(Items.COARSE_DIRT)) {
                        super.clicked(slotNumber, button, action, player);
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

        // Reimplement to filter transfers to the Grizzly inventory for acceptable items in.
        if (slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();

            if (invSlot < this.inventory.getContainerSize()) {
                // From the Grizzly inventory to the Player.
                if (!this.moveItemStackTo(originalStack, this.inventory.getContainerSize(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From the Player inventory to the Grizzly.
                if (originalStack.is(Items.COARSE_DIRT)) {
                    // Try to place up to a stack of any acceptable item into the input slot.
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
        } else if (TerrestriaIntegration.ENABLED && item == TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM) {
            return propertyDelegate.get(4) / 100.0f;
        } else if (TerrestriaIntegration.ENABLED && item == TerrestriaIntegration.VOLCANIC_SAND_ITEM) {
            return propertyDelegate.get(5) / 100.0f;
        } else {
            return 0.0f;
        }
    }
}