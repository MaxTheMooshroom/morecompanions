package net.qiyanamark.companionpouch.container;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.qiyanamark.companionpouch.catalog.CatalogContainer;
import net.qiyanamark.companionpouch.helpers.HandlerSlotContainerPouchCompanion;

public class ContainerPouchCompanion extends AbstractContainerMenu {
    public static final String MENU_ID = "container_pouch_companion";

    private final ItemStack pouchStack;
    private final IItemHandler handler;

    // Server-side ctor
    public ContainerPouchCompanion(int id, Inventory inv, ItemStack pouchStack) {
        super(CatalogContainer.COMPANION_POUCH, id);
        this.pouchStack = pouchStack;
        this.handler = pouchStack
            .getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            .orElseThrow(IllegalStateException::new);

        defineLayout(inv);
    }

    // Client-side ctor from network
    public static ContainerPouchCompanion fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        boolean main = buf.readBoolean();
        ItemStack pouch = main ? inv.player.getMainHandItem() : inv.player.getOffhandItem();
        return new ContainerPouchCompanion(id, inv, pouch);
    }

    private void defineLayout(Inventory inv) {
        this.addSlot(new HandlerSlotContainerPouchCompanion(handler, 0, 62, 20));
        this.addSlot(new HandlerSlotContainerPouchCompanion(handler, 0, 80, 20));
        this.addSlot(new HandlerSlotContainerPouchCompanion(handler, 0, 98, 20));

        for (int col = 0; col < 9; col++) {
            // Hotbar 9x1
            this.addSlot(new Slot(inv, col, 8 + col * 18, 109));

            // Player inventory 9x3
            for (int row = 0; row < 3; row++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem() == pouchStack || player.getOffhandItem() == pouchStack;
    }
}