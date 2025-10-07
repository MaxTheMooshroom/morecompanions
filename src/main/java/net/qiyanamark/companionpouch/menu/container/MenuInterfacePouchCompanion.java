package net.qiyanamark.companionpouch.menu.container;

import com.mojang.datafixers.util.Pair;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import iskallia.vault.core.vault.VaultUtils;

import net.qiyanamark.companionpouch.catalog.CatalogCapability;
import org.jetbrains.annotations.NotNull;

import net.qiyanamark.companionpouch.capability.IDataPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogMenu;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import net.qiyanamark.companionpouch.util.Structs;
import net.qiyanamark.companionpouch.util.annotations.Extends;

public class MenuInterfacePouchCompanion extends AbstractContainerMenu {
    public static final String MENU_I18N = "container_interface_pouch_companion";
    public static final String SCREEN_I18N = "screen.companionpouch.interface_pouch_companion";

    private final ItemStack pouchStack;
    private final int slotCount;

    private LazyOptional<IDataPouchCompanion> pouchDataProvider;
    private IDataPouchCompanion pouchData;

    // Server-side ctor
    public MenuInterfacePouchCompanion(int id, ItemStack pouchStack, int slotCount) {
        super(CatalogMenu.COMPANION_POUCH_INTERFACE, id);

        this.slotCount = slotCount;
        this.pouchStack = pouchStack;

        this.refreshPouchDataCapability(null);
        defineLayout();
    }

    private void refreshPouchDataCapability(LazyOptional<IDataPouchCompanion> oldCap) {
        this.pouchDataProvider = this.pouchStack.getCapability(CatalogCapability.COMPANION_POUCH_CAPABILITY);
        this.pouchDataProvider.addListener(this::refreshPouchDataCapability);
        this.pouchData = this.pouchDataProvider.orElseThrow(IllegalStateException::new);
    }

    // Client-side ctor from network
    public static MenuInterfacePouchCompanion fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        int slotCount = buf.readByte();
        Player player = inv.player;

        return Structs.LocationPouch.findOnPlayer(player)
                .map(Pair::getSecond)
                .map(stack -> new MenuInterfacePouchCompanion(id, stack, slotCount))
                .orElse(null);
    }

    public static SimpleMenuProvider getProvider(ItemStack pouchStack) {
        return new SimpleMenuProvider(
                (id, inv, player) -> {
                    int slotCount = pouchStack.getCapability(CatalogCapability.COMPANION_POUCH_CAPABILITY)
                        .map(IDataPouchCompanion::getSlots)
                        .orElse(ItemPouchCompanion.DEFAULT_SLOT_COUNT);
                    return new MenuInterfacePouchCompanion(id, pouchStack, slotCount);
                },
                new TranslatableComponent(SCREEN_I18N)
            );
    }

    @Override
    @Extends(AbstractContainerMenu.class)
    public boolean stillValid(Player player) {
        return VaultUtils.getVault(player.level).isPresent()
                || player.getMainHandItem() == pouchStack
                || player.getOffhandItem() == pouchStack;
    }

    @Override
    @Extends(AbstractContainerMenu.class)
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
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

    public ItemStack getPouchStack() {
        return this.pouchStack;
    }

    private void defineLayout() {
        int yPos = CatalogMenu.MENU_INTERFACE_SLOT_PADDING_LEFT_TOP.y();
        int slotW = CatalogMenu.MENU_SLOT.getSize().x();
        int chromeW = CatalogMenu.SCREEN_INTERFACE_CHROME.getSize().x();
        int paddingX = CatalogMenu.MENU_INTERFACE_SLOT_PADDING_LEFT_TOP.x();

        switch (this.slotCount) {
        case 0:
            return;
        case 1: {
            int xPos = chromeW / 2 - slotW / 2;
            this.addSlot(new SlotContainerPouch(this.pouchData, 0, xPos, yPos));
            return;
        }
        case 2: {
            int xRight = chromeW - paddingX - slotW;
            this.addSlot(new SlotContainerPouch(this.pouchData, 0, paddingX, yPos));
            this.addSlot(new SlotContainerPouch(this.pouchData, 1, xRight, yPos));
            return;
        }
        default: {
            int available = chromeW - 2 * paddingX - slotW;
            int spacing = available / (this.slotCount - 1);

            for (int i = 0; i < this.slotCount; i++) {
                int center = paddingX + slotW / 2 + spacing * i;
                int xPos = center - slotW / 2;
                this.addSlot(new SlotContainerPouch(this.pouchData, i, xPos, yPos));
            }
        }
        }
    }

    private static class SlotContainerPouch extends SlotItemHandler {
        public SlotContainerPouch(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player playerIn) {
            return false;
        }
    }
}