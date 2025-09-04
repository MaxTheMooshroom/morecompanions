package net.qiyanamark.companionpouch.menu;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.qiyanamark.companionpouch.catalog.CatalogMenu;

public class MenuPouchCompanion extends AbstractContainerMenu {
    public static final String MENU_ID = "container_pouch_companion";

    private final ItemStack pouchStack;
    private final IItemHandler handler;

    // Server-side ctor
    public MenuPouchCompanion(int id, Inventory inv, ItemStack pouchStack) {
        super(CatalogMenu.COMPANION_POUCH, id);
        this.pouchStack = pouchStack;
        this.handler = pouchStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            .orElseThrow(IllegalStateException::new);
        layout(inv);
    }

    // Client-side ctor from network
    public static MenuPouchCompanion fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        boolean main = buf.readBoolean();
        ItemStack pouch = main ? inv.player.getMainHandItem() : inv.player.getOffhandItem();
        return new MenuPouchCompanion(id, inv, pouch);
    }

    private void layout(Inventory playerInv) {
        this.addSlot(new SlotItemHandler(handler, 0, 62, 20));
        this.addSlot(new SlotItemHandler(handler, 0, 80, 20));
        this.addSlot(new SlotItemHandler(handler, 0, 98, 20));

        for (int col = 0; col < 9; col++) {
            // Hotbar
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 109));

            // Player inventory 3x9
            for (int row = 0; row < 3; row++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem() == pouchStack || player.getOffhandItem() == pouchStack;
    }
}