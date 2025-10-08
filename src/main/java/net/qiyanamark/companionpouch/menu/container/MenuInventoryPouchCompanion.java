package net.qiyanamark.companionpouch.menu.container;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.SlotItemHandler;

import iskallia.vault.item.CompanionItem;
import net.qiyanamark.companionpouch.capability.IDataPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogCapability;
import net.qiyanamark.companionpouch.catalog.CatalogMenu;
import net.qiyanamark.companionpouch.helper.HelperInventory;
import net.qiyanamark.companionpouch.util.IByteBufEnum;
import net.qiyanamark.companionpouch.util.Structs;
import net.qiyanamark.companionpouch.util.annotations.Extends;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MenuInventoryPouchCompanion extends AbstractContainerMenu {
    public static final String MENU_ID = "container_inventory_pouch_companion";
    public static final String SCREEN_I18N = "screen.companionpouch.interface_pouch_companion";

    private final ItemStack pouchStack;
    private final int slotCount;

    private LazyOptional<IDataPouchCompanion> pouchDataProvider;
    private IDataPouchCompanion pouchData;

    // Server-side ctor
    public MenuInventoryPouchCompanion(int id, Inventory inv, ItemStack pouchStack, int slotCount) {
        super(CatalogMenu.COMPANION_POUCH_INVENTORY, id);
        this.pouchStack = pouchStack;
        this.slotCount = slotCount;

        this.refreshPouchDataCapability(null);
        defineLayout(inv);
    }

    private void refreshPouchDataCapability(LazyOptional<IDataPouchCompanion> oldCap) {
        this.pouchDataProvider = this.pouchStack.getCapability(CatalogCapability.COMPANION_POUCH_CAPABILITY);
        this.pouchDataProvider.addListener(this::refreshPouchDataCapability);
        this.pouchData = this.pouchDataProvider.orElseThrow(IllegalStateException::new);
    }

    // Client-side ctor from network
    public static @Nullable MenuInventoryPouchCompanion fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        Structs.LocationPouch location = IByteBufEnum.readByte(Structs.LocationPouch.class, buf);
        int slotCount = buf.readByte();

        Optional<ItemStack> pouchStack = location.getFromPlayer(inv.player);
        return pouchStack
                .map(itemStack -> new MenuInventoryPouchCompanion(id, inv, itemStack, slotCount))
                .orElse(null);
    }

    public static SimpleMenuProvider getProvider(ItemStack pouchStack, int slotCount) {
        return new SimpleMenuProvider(
                (id, inv, player) -> new MenuInventoryPouchCompanion(id, inv, pouchStack, slotCount),
                new TranslatableComponent(SCREEN_I18N)
        );
    }

    @Override
    @Extends(AbstractContainerMenu.class)
    public boolean stillValid(@NotNull Player player) {
        return Structs.LocationPouch.findOnPlayer(player).isPresent();
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

    private void defineLayout(Inventory inv) {
        int slotWidth = CatalogMenu.MENU_SLOT.getSize().x();
        int totalWidth = slotWidth * this.slotCount;
        int remaining = CatalogMenu.SCREEN_INVENTORY_CHROME.getSize().x() - totalWidth;
        int leftPadding = remaining / 2;
        
        for (int i = 0; i < this.slotCount; i++) {
            int posX = leftPadding + i * slotWidth;
            this.addSlot(new SlotContainerPouch(this.pouchData, i, posX, 32));
        }

        HelperInventory.playerInventory(inv).forEach(this::addSlot);
    }

    private static class SlotContainerPouch extends SlotItemHandler {
        public SlotContainerPouch(IDataPouchCompanion itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof CompanionItem;
        }
    }
}