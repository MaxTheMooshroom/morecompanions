package net.qiyanamark.companionpouch.menu.container;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.capabilities.ProviderCapabilityPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogItem;
import net.qiyanamark.companionpouch.catalog.CatalogMenu;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.util.annotations.Extends;

public class MenuInterfacePouchCompanion extends AbstractContainerMenu {
    public static final String MENU_ID = "container_interface_pouch_companion";
    public static final String SCREEN_I18N = "screen.companionpouch.interface_pouch_companion";

    private final ItemStack pouchStack;
    private final IItemHandler handler;
    private final int slotCount;

    // Server-side ctor
    public MenuInterfacePouchCompanion(int id, Inventory inv, ItemStack pouchStack, int slotCount) {
        super(CatalogMenu.COMPANION_POUCH_INTERFACE, id);

        this.slotCount = slotCount;
        this.pouchStack = pouchStack;
        this.handler = pouchStack
                .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .orElseThrow(() -> new IllegalStateException("stack has no ITEM_HANDLER_CAPABILITY; stack registry name: " + pouchStack.getItem().getRegistryName()));

        defineLayout(inv);
    }

    // Client-side ctor from network
    public static MenuInterfacePouchCompanion fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        int slotCount = buf.readByte();
        LocalPlayer lPlayer = ModCompanionPouch.getClientPlayer();

        ModCompanionPouch.messageLocal("hit0");
        return HelperCompanions.getCompanionPouch(lPlayer)
            .map(stack -> new MenuInterfacePouchCompanion(id, inv, stack, slotCount))
            .orElseGet(() -> {
                ItemStack hand = lPlayer.getMainHandItem();
                if (!hand.isEmpty() && hand.getItem() == CatalogItem.COMPANION_POUCH.get()) {
                    ModCompanionPouch.messageLocal("hit1");
                    return new MenuInterfacePouchCompanion(id, inv, hand, slotCount);
                }

                hand = lPlayer.getOffhandItem();
                if (!hand.isEmpty() && hand.getItem() == CatalogItem.COMPANION_POUCH.get()) {
                    ModCompanionPouch.messageLocal("hit2");
                    return new MenuInterfacePouchCompanion(id, inv, hand, slotCount);
                }
                
                ModCompanionPouch.messageLocal("hit3");
                return null;
            });
            // .orElse(null);
    }

    public static SimpleMenuProvider getProvider(ItemStack pouchStack) {
        return new SimpleMenuProvider(
                (id, inv, player) -> {
                    byte slotCount = ProviderCapabilityPouchCompanion.getSizeOrDefault(pouchStack.getOrCreateTag());
                    return new MenuInterfacePouchCompanion(id, inv, pouchStack, slotCount);
                },
                new TranslatableComponent(SCREEN_I18N)
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

    public int getSlotCount() {
        return this.slotCount;
    }

    public ItemStack getPouchStack() {
        return this.pouchStack;
    }

    private void defineLayout(Inventory inv) {
        int yPos = CatalogMenu.MENU_INTERFACE_SLOT_PADDING_LEFT_TOP.y();
        int slotW = CatalogMenu.MENU_SLOT.getSize().x();
        int chromeW = CatalogMenu.SCREEN_INTERFACE_CHROME.getSize().x();
        int paddingX = CatalogMenu.MENU_INTERFACE_SLOT_PADDING_LEFT_TOP.x();

        switch (this.slotCount) {
        case 0:
            return;
        case 1: {
            int xPos = chromeW / 2 - slotW / 2;
            this.addSlot(new SlotContainerPouch(this.handler, 0, xPos, yPos));
            return;
        }
        case 2: {
            int xLeft = paddingX;
            int xRight = chromeW - paddingX - slotW;
            this.addSlot(new SlotContainerPouch(this.handler, 0, xLeft, yPos));
            this.addSlot(new SlotContainerPouch(this.handler, 1, xRight, yPos));
            return;
        }
        default: {
            int available = chromeW - 2 * paddingX - slotW;
            int spacing = available / (this.slotCount - 1);

            for (int i = 0; i < this.slotCount; i++) {
                int center = paddingX + slotW / 2 + spacing * i;
                int xPos = center - slotW / 2;
                this.addSlot(new SlotContainerPouch(this.handler, i, xPos, yPos));
            }
        }
        }
    }

    private class SlotContainerPouch extends SlotItemHandler {
        public SlotContainerPouch(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player playerIn) {
            return false;
        }

        @Override
        public boolean isActive() {
            return true;
        }
    }
}