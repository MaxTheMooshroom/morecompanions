package net.qiyanamark.companionpouch.menu.container;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
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
import net.qiyanamark.companionpouch.capabilities.CapabilitiesPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogMenu;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.util.annotations.Extends;

public class MenuInterfacePouchCompanion extends AbstractContainerMenu {
    public static final String MENU_ID = "container_interface_pouch_companion";

    private final ItemStack pouchStack;
    private final IItemHandler handler;
    private final int slotCount;

    // Server-side ctor
    public MenuInterfacePouchCompanion(int id, Inventory inv, ItemStack pouchStack, int slotCount) {
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
    public static MenuInterfacePouchCompanion fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        int slotCount = buf.readByte();
        
        UUID uuid = buf.readUUID();
        ServerPlayer sPlayer = ModCompanionPouch.getServer().getPlayerList().getPlayer(uuid);
        Optional<ItemStack> pouchStack = HelperCompanions.getCompanionPouch(sPlayer);

        if (pouchStack.isEmpty()) {
            return null;
        }

        return new MenuInterfacePouchCompanion(id, inv, pouchStack.get(), slotCount);
    }

    public static SimpleMenuProvider getProvider(ItemStack pouchStack, String containerI18n) {
        return new SimpleMenuProvider(
                (id, inv, player) -> {
                    byte slotCount = CapabilitiesPouchCompanion.getSizeOrDefault(pouchStack.getOrCreateTag());
                    return new MenuInterfacePouchCompanion(id, inv, pouchStack, slotCount);
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

    public int getSlotCount() {
        return this.slotCount;
    }

    public ItemStack getPouchStack() {
        return this.pouchStack;
    }

    private void defineLayout(Inventory inv) {
        for (int i = 0; i < this.slotCount; i++) {
            this.addSlot(new SlotContainerPouch(
                this.handler, i,
                CatalogMenu.MENU_INTERFACE_SLOT_PADDING_LEFT_TOP.x() + (CatalogMenu.MENU_INTERFACE_SLOT_SPACING * i),
                CatalogMenu.MENU_INTERFACE_SLOT_PADDING_LEFT_TOP.y()
            ));
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
    }
}