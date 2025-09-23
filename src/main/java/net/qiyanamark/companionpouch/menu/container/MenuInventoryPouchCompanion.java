package net.qiyanamark.companionpouch.menu.container;

import iskallia.vault.item.CompanionItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
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
import net.qiyanamark.companionpouch.screen.ScreenInventoryPouchCompanion;
import net.qiyanamark.companionpouch.util.annotations.Extends;

public class MenuInventoryPouchCompanion extends AbstractContainerMenu {
    public static final String MENU_ID = "container_inventory_pouch_companion";

    private final ItemStack pouchStack;
    private final IItemHandler handler;
    private final int slotCount;

    // Server-side ctor
    public MenuInventoryPouchCompanion(int id, Inventory inv, ItemStack pouchStack, int slotCount) {
        super(CatalogMenu.COMPANION_POUCH_INVENTORY, id);
        // this.slotCount = CapabilitiesPouchCompanion.getSize(pouchStack.getOrCreateTag()).orElse(ItemPouchCompanion.DEFAULT_SLOT_COUNT);
        this.slotCount = slotCount;
        this.pouchStack = pouchStack;
        this.handler = pouchStack
                .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElseThrow(IllegalStateException::new);

        defineLayout(inv);
    }

    // Client-side ctor from network
    public static MenuInventoryPouchCompanion fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        boolean main = buf.readBoolean();
        ItemStack pouchStack = main ? inv.player.getMainHandItem() : inv.player.getOffhandItem();
        int slotCount = buf.readByte();
        return new MenuInventoryPouchCompanion(id, inv, pouchStack, slotCount);
    }

    public static SimpleMenuProvider getProvider(InteractionHand hand, String containerI18n) {
        return new SimpleMenuProvider(
                (id, inv, player) -> {
                    ItemStack pouchStack = player.getItemInHand(hand);
                    byte slotCount = CapabilitiesPouchCompanion.getSizeOrDefault(pouchStack.getOrCreateTag());
                    return new MenuInventoryPouchCompanion(id, inv, pouchStack, slotCount);
                },
                new TranslatableComponent(containerI18n)
            );
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
        int slotSpacing = (ScreenInventoryPouchCompanion.getSize().x() - (HelperInventory.PLAYER_INV_LEFT * 2)) / this.slotCount;
        for (int i = 0; i < this.slotCount; i++) {
            this.addSlot(new SlotContainerPouch(this.handler, i, HelperInventory.PLAYER_INV_LEFT + slotSpacing * i, 32));
        }

        HelperInventory.playerInventory(inv).stream().forEach(slot -> this.addSlot(slot));
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