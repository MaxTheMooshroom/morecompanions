package net.qiyanamark.companionpouch.menu.container;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import net.qiyanamark.companionpouch.catalog.CatalogTextures;
import net.qiyanamark.companionpouch.helper.HelperInventory;
import net.qiyanamark.companionpouch.util.Structs;
import net.qiyanamark.companionpouch.util.annotations.Extends;

import static net.minecraft.Util.NIL_UUID;

public class MenuInventoryPouchCompanion extends MenuPouchBase {
    public static final String MENU_ID = "container_inventory_pouch_companion";
    public static final String SCREEN_I18N = "screen.companionpouch.interface_pouch_companion";

    public MenuInventoryPouchCompanion(int id, Inventory inv, Structs.LocationPouch loc) {
        super(id, inv, loc);
        this.policyMayPickup = true;
        this.policyMayPlace = true;
    }

    // Client-side ctor from network
    public static MenuInventoryPouchCompanion fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        Structs.LocationPouch loc = buf.readEnum(Structs.LocationPouch.class);
        return new MenuInventoryPouchCompanion(id, inv, loc);
    }

    public static SimpleMenuProvider getProviderFromLocation(Structs.LocationPouch loc) {
        return new SimpleMenuProvider(
            (id, inv, player) -> new MenuInventoryPouchCompanion(id, inv, loc),
            new TranslatableComponent(SCREEN_I18N)
        );
    }

    public static SimpleMenuProvider getProviderFromEquippedCurio() {
        return getProviderFromLocation(Structs.LocationPouch.CURIO);
    }

    @Override
    @Extends(AbstractContainerMenu.class)
    public boolean stillValid(@NotNull Player player) {
        if (player instanceof ServerPlayer) {
            if (this.pouchContainer.getPouchLocation().getFromEntity(player).filter(cap -> cap == this.pouchContainer.getPouchStack()).isPresent()) {
                return true;
            }
        }
        player.sendMessage(new TextComponent("closing menu"), NIL_UUID);
        return false;
    }

    @Override
    @Extends(MenuPouchBase.class)
    protected void defineLayout(Inventory inv) {
        int slotWidth = CatalogTextures.MENU_SLOT.getSize().x();
        int totalWidth = slotWidth * this.pouchContainer.getSlots();
        int remaining = CatalogTextures.SCREEN_INVENTORY_CHROME.getSize().x() - totalWidth;
        int leftPadding = remaining / 2;
        
        for (int i = 0; i < this.pouchContainer.getSlots(); i++) {
            int posX = leftPadding + i * slotWidth;
            this.addSlot(new SlotContainerPouch(this.pouchContainer, i, posX, 32, this.policyMayPlace, this.policyMayPickup));
        }

        HelperInventory.playerInventory(inv).forEach(this::addSlot);
    }
}