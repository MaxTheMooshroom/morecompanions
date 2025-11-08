package net.qiyanamark.companionpouch.menu.container.client.screens;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import net.qiyanamark.companionpouch.catalog.CatalogTextures;
import net.qiyanamark.companionpouch.menu.container.client.ClientMenuPouchInventory;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;

import static net.qiyanamark.companionpouch.catalog.CatalogTextures.TEXTURE_ATLAS_MENUS_POUCH;

public class ScreenPouchInventory extends ScreenPouchBase<ScreenPouchInventory, ClientMenuPouchInventory> {
    public static final ComponentTexture SCREEN_INVENTORY_CHROME = CatalogTextures.createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "bg.inventory", new Vec2i(0, 0), new Vec2i(174, 163));
    public static final String GROUP_HOTKEY_TOGGLERS = "hotkey_togglers";
    public static final String GROUP_CURSE_INDICATOR = "curse_indicator";

    public ScreenPouchInventory(ClientMenuPouchInventory menu, Inventory inv) {
        super(SCREEN_INVENTORY_CHROME, menu, inv);
        this.group(GROUP_HOTKEY_TOGGLERS);
        this.group(GROUP_CURSE_INDICATOR);
    }

    public static ScreenPouchInventory fromMenu(ClientMenuPouchInventory menu, Inventory inv, Component __) {
        return new ScreenPouchInventory(menu, inv);
    }
}
