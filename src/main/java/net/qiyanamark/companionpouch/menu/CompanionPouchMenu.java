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


import net.qiyanamark.companionpouch.init.ModMenus;
import net.qiyanamark.companionpouch.item.CompanionPouchItem;


public class CompanionPouchMenu extends AbstractContainerMenu {
    private final ItemStack pouchStack;
    private final IItemHandler handler;


    // Server-side ctor
    public CompanionPouchMenu(int id, Inventory inv, ItemStack pouchStack) {
        super(ModMenus.COMPANION_POUCH.get(), id);
        this.pouchStack = pouchStack;
        this.handler = pouchStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow();
        layout(inv);
    }


    // Client-side ctor from network
    public static CompanionPouchMenu fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        boolean main = buf.readBoolean();
        ItemStack pouch = main ? inv.player.getMainHandItem() : inv.player.getOffhandItem();
        return new CompanionPouchMenu(id, inv, pouch);
    }


    private void layout(Inventory playerInv) {
// 3 pouch slots (x: 62, 80, 98; y: 20)
        for (int i = 0; i < CompanionPouchItem.SLOT_COUNT; i++) {
            this.addSlot(new SlotItemHandler(handler, i, 62 + i * 18, 20));
        }


// Player inventory 3x9
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
// Hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 109));
        }
    }


    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem() == pouchStack || player.getOffhandItem() == pouchStack;
    }


}