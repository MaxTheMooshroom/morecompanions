package net.qiyanamark.companionpouch.menu.container;

import iskallia.vault.item.CompanionItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.qiyanamark.companionpouch.capabilities.CapabilitiesPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogMenu;
import net.qiyanamark.companionpouch.helper.HelperInventory;
import net.qiyanamark.companionpouch.helper.annotations.Extends;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;

public class MenuContainerPouchCompanion extends AbstractContainerMenu {
    public static final String MENU_ID = "container_pouch_companion";

    private final ItemStack pouchStack;
    private final IItemHandler handler;
    private final int slotCount;

    // Server-side ctor
    public MenuContainerPouchCompanion(int id, Inventory inv, ItemStack pouchStack, int slotCount) {
        super(CatalogMenu.COMPANION_POUCH, id);
        // this.slotCount = CapabilitiesPouchCompanion.getSize(pouchStack.getOrCreateTag()).orElse(ItemPouchCompanion.DEFAULT_SLOT_COUNT);
        this.slotCount = slotCount;
        this.pouchStack = pouchStack;
        this.handler = pouchStack
                .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElseThrow(IllegalStateException::new);

        defineLayout(inv);
    }

    // Client-side ctor from network
    public static MenuContainerPouchCompanion fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        boolean main = buf.readBoolean();
        ItemStack pouchStack = main ? inv.player.getMainHandItem() : inv.player.getOffhandItem();
        int slotCount = buf.readByte();
        return new MenuContainerPouchCompanion(id, inv, pouchStack, slotCount);
    }

    @Override
    @Extends(AbstractContainerMenu.class)
    public boolean stillValid(Player player) {
        return player.getMainHandItem() == pouchStack || player.getOffhandItem() == pouchStack;
    }

    @Override
    @Extends(AbstractContainerMenu.class)
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        
        ItemStack stackInSlot = slot.getItem();
        ItemStack result = stackInSlot.copy();

        int playerSlots = this.slots.size();
        if (index < this.slotCount) {
            // pouch -> player
            if (!this.moveItemStackTo(stackInSlot, this.slotCount, playerSlots, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // player -> pouch
            if (!this.moveItemStackTo(stackInSlot, 0, this.slotCount, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return result;
    }

    private void defineLayout(Inventory inv) {
        this.addSlot(new SlotContainerPouch(handler, 0, 62, 20));
        this.addSlot(new SlotContainerPouch(handler, 1, 80, 20));
        this.addSlot(new SlotContainerPouch(handler, 2, 98, 20));

        // TODO: Add slots procedurally

        HelperInventory.playerInventory(inv, 0).stream().forEach(slot -> this.addSlot(slot));
    }

    private class SlotContainerPouch extends SlotItemHandler {
        public SlotContainerPouch(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof CompanionItem;
        }
    }
}