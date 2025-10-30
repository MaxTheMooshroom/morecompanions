package net.qiyanamark.companionpouch.menu.container;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import net.qiyanamark.companionpouch.catalog.CatalogMenu;
import net.qiyanamark.companionpouch.catalog.CatalogTextures;
import net.qiyanamark.companionpouch.util.Structs;
import net.qiyanamark.companionpouch.util.annotations.Extends;

public class MenuInterfacePouchCompanion extends MenuPouchBase {
    public static final String MENU_ID = "container_interface_pouch_companion";
    public static final String SCREEN_I18N = "screen.companionpouch.interface_pouch_companion";

    // Server-side ctor
    public MenuInterfacePouchCompanion(int id, Inventory inv, Structs.LocationPouch loc) {
        super(id, inv, loc);
    }

    // Client-side ctor from network
    public static MenuInterfacePouchCompanion fromNetwork(int id, Inventory inv, FriendlyByteBuf buf) {
        return new MenuInterfacePouchCompanion(id, inv, buf.readEnum(Structs.LocationPouch.class));
    }

    public static SimpleMenuProvider getProviderFromEquippedCurio() {
        return new SimpleMenuProvider(
            (id, inv, player) -> new MenuInterfacePouchCompanion(id, inv, Structs.LocationPouch.CURIO),
            new TranslatableComponent(SCREEN_I18N)
        );
    }

    @Override
    @Extends(AbstractContainerMenu.class)
    public boolean stillValid(@NotNull Player player) {
        return Structs.LocationPouch.CURIO.getFromEntity(player).isPresent();
    }

    protected void defineLayout(Inventory inv) {
        int yPos = CatalogMenu.MENU_INTERFACE_SLOT_PADDING_LEFT_TOP.y();
        int slotW = CatalogTextures.MENU_SLOT.getSize().x();
        int chromeW = CatalogTextures.SCREEN_INTERFACE_CHROME.getSize().x();
        int paddingX = CatalogMenu.MENU_INTERFACE_SLOT_PADDING_LEFT_TOP.x();

        int slotCount = this.pouchContainer.getSlots();

        switch (slotCount) {
        case 0:
            return;
        case 1: {
            int xPos = chromeW / 2 - slotW / 2;
            this.addSlot(0, xPos, yPos);
            return;
        }
        case 2: {
            int xRight = chromeW - paddingX - slotW;
            this.addSlot(0, paddingX, yPos);
            this.addSlot(1, xRight, yPos);
            return;
        }
        default: {
            int available = chromeW - 2 * paddingX - slotW;
            int spacing = available / (slotCount - 1);

            for (int i = 0; i < slotCount; i++) {
                int center = paddingX + slotW / 2 + spacing * i;
                int xPos = center - slotW / 2;
                this.addSlot(i, xPos, yPos);
            }
        }
        }
    }
}