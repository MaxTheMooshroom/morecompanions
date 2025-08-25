package com.qiyanamark.companionpouch.container;


import com.qiyanamark.companionpouch.init.ModContainers;
import com.qiyanamark.companionpouch.item.CompanionPouchItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CompanionPouchContainer extends AbstractContainerMenu {
    private final ItemStack pouchStack;
    private final InteractionHand hand;

    public CompanionPouchContainer(int windowId, Inventory playerInventory, ItemStack pouchStack, InteractionHand hand) {
        super(ModContainers.COMPANION_POUCH.get(), windowId);
        this.pouchStack = pouchStack;
        this.hand = hand;

        // Add companion slots (3 slots in a row)
        for (int i = 0; i < 3; i++) {
            this.addSlot(new CompanionSlot(i, 62 + i * 18, 35));
        }

        // Add player inventory slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Add player hotbar slots
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    public CompanionPouchContainer(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        this(windowId, playerInventory, data.readItem(), data.readEnum(InteractionHand.class));
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 3) {
                // Moving from companion slots to player inventory
                if (!this.moveItemStackTo(itemstack1, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to companion slots
                if (CompanionPouchItem.isValidCompanion(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 0, 3, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void clicked(int slotId, int dragType, @NotNull ClickType clickType, @NotNull Player player) {
        // Prevent moving the pouch itself
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot.getItem() == this.pouchStack) {
                return;
            }
        }

        super.clicked(slotId, dragType, clickType, player);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        ItemStack currentPouch = player.getItemInHand(hand);
        return ItemStack.isSame(currentPouch, pouchStack) &&
                CompanionPouchItem.isOwner(currentPouch, player);
    }

    private class CompanionSlot extends Slot {
        private final int companionSlot;

        public CompanionSlot(int companionSlot, int x, int y) {
            super(null, companionSlot, x, y);
            this.companionSlot = companionSlot;
        }

        @Override
        public @NotNull ItemStack getItem() {
            return CompanionPouchItem.getCompanionInSlot(pouchStack, companionSlot);
        }

        @Override
        public void set(@NotNull ItemStack stack) {
            CompanionPouchItem.setCompanionInSlot(pouchStack, companionSlot, stack);
            this.setChanged();
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return CompanionPouchItem.isValidCompanion(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public @NotNull ItemStack remove(int amount) {
            ItemStack current = getItem();
            if (!current.isEmpty()) {
                CompanionPouchItem.setCompanionInSlot(pouchStack, companionSlot, ItemStack.EMPTY);
                this.setChanged();
                return current;
            }
            return ItemStack.EMPTY;
        }
    }
}
