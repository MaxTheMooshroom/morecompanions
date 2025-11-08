package net.qiyanamark.companionpouch.menu.container.client.screens;

import net.minecraft.world.entity.player.Inventory;

import net.qiyanamark.companionpouch.menu.container.client.ClientMenuPouchBase;
import net.qiyanamark.companionpouch.ui.ScreenBase;
import net.qiyanamark.companionpouch.util.CompositeTexture;

public abstract class ScreenPouchBase<Screen extends ScreenPouchBase<Screen, Menu>, Menu extends ClientMenuPouchBase<Menu, ?>> extends ScreenBase<Screen, Menu> {
    public ScreenPouchBase(CompositeTexture.ComponentTexture chrome, Menu menu, Inventory playerInventory) {
        super(chrome, menu, playerInventory);
    }
}
