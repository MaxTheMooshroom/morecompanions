package net.qiyanamark.companionpouch.menu.container;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.qiyanamark.companionpouch.catalog.CatalogMenu;
import net.qiyanamark.companionpouch.container.ContainerItemCurioPouch;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.util.Structs;
import net.qiyanamark.companionpouch.util.annotations.Extends;

public abstract class MenuPouchBase extends AbstractContainerMenu {
    public final ContainerItemCurioPouch pouchContainer;

    private final int[] dataSlotActivationIndex;
    private final int[] dataSlotsTemporalUsed;
    private final int[] dataSlotCurseStacks;

    protected boolean policyMayPlace;
    protected boolean policyMayPickup;

    // Server-side ctor
    public MenuPouchBase(int id, Inventory inv, Structs.LocationPouch loc) {
        super(CatalogMenu.COMPANION_POUCH_INTERFACE, id);

        this.pouchContainer = new ContainerItemCurioPouch(inv.player, loc);
        this.dataSlotActivationIndex = new int[]{ this.pouchContainer.getActivationIndex() };
        this.dataSlotsTemporalUsed = new int[this.pouchContainer.getSlots()];
        this.dataSlotCurseStacks = new int[] { this.pouchContainer.getCurseStacks() };

        // temporal selection
        this.addDataSlot(DataSlot.shared(this.dataSlotActivationIndex, 0));

        // curse stack calculation
        this.addDataSlot(DataSlot.shared(this.dataSlotCurseStacks, 0));

        // companion status indicators
        Optional<Vault> vaultMaybe = VaultUtils.getVault(inv.player.getLevel());
        for (int i = 0; i < this.pouchContainer.getSlots(); i++) {
            this.addDataSlot(DataSlot.shared(this.dataSlotsTemporalUsed, i));

            ItemStack companion = this.pouchContainer.getStackInSlot(i);
            boolean temporalUsed = !HelperCompanions.companionCanUseTemporalInVault(companion, vaultMaybe);
            this.dataSlotsTemporalUsed[i] = temporalUsed ? 1 : 0;
        }

        this.defineLayout(inv);
    }

    public byte getActivationIndex() {
        return (byte) this.dataSlotActivationIndex[0];
    }

    public boolean getCompanionTemporalUsed(int slotId) {
        return this.dataSlotsTemporalUsed[slotId] == 1;
    }

    public void setActivationIndex(byte index) {
        this.pouchContainer.setActivationIndex(index);
        this.dataSlotActivationIndex[0] = index;
        this.broadcastChanges();
    }

    public void setCompanionTemporalUsed(byte index, boolean value) {
        this.dataSlotsTemporalUsed[index + 1] = value ? 1 : 0;
        this.broadcastChanges();
    }

    public ContainerItemCurioPouch getPouchContainer() {
        return this.pouchContainer;
    }

    @Override
    @Extends(AbstractContainerMenu.class)
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();

        int playerSlots = this.slots.size();
        int slotCount = 0;
        if (index < slotCount) {
            // pouch -> player
            if (!this.policyMayPickup || !this.moveItemStackTo(stackInSlot, slotCount, playerSlots, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // player -> pouch
            if (!this.policyMayPlace || !this.moveItemStackTo(stackInSlot, 0, slotCount, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return stackInSlot.copy();
    }

    protected abstract void defineLayout(Inventory inv);

    protected void addSlot(int index, int xPosition, int yPosition) {
        this.addSlot(new SlotContainerPouch(this.pouchContainer, index, xPosition, yPosition, this.policyMayPlace, this.policyMayPickup));
    }

    protected static class SlotContainerPouch extends SlotItemHandler {
        private final boolean policyMayPlace;
        private final boolean policyMayPickup;

        public SlotContainerPouch(IItemHandler itemHandler, int index, int xPosition, int yPosition, boolean mayPlace, boolean mayPickup) {
            super(itemHandler, index, xPosition, yPosition);
            this.policyMayPlace = mayPlace;
            this.policyMayPickup = mayPickup;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return this.policyMayPlace;
        }

        @Override
        public boolean mayPickup(Player playerIn) {
            return this.policyMayPickup;
        }
    }
}
