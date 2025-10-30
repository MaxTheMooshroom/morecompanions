package net.qiyanamark.companionpouch.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import net.qiyanamark.companionpouch.catalog.CatalogTextures;
import net.qiyanamark.companionpouch.menu.container.MenuInventoryPouchCompanion;

public class ScreenInventoryPouch extends ScreenBase<MenuInventoryPouchCompanion> {
    public ScreenInventoryPouch(MenuInventoryPouchCompanion menuInventoryPouchCompanion, Inventory playerInventory, Component title) {
        super(CatalogTextures.SCREEN_INVENTORY_CHROME, menuInventoryPouchCompanion, playerInventory, title);
        this.background = CatalogTextures.SCREEN_INVENTORY_CHROME;
    }
}
