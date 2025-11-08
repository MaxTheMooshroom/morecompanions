package net.qiyanamark.companionpouch.menu.container.client.screens;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import net.qiyanamark.companionpouch.catalog.CatalogTextures;
import net.qiyanamark.companionpouch.menu.container.client.ClientMenuPouchInterface;
import net.qiyanamark.companionpouch.ui.WidgetBasic;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;

import static net.qiyanamark.companionpouch.catalog.CatalogTextures.TEXTURE_ATLAS_MENUS_POUCH;

public class ScreenPouchInterface extends ScreenPouchBase<ScreenPouchInterface, ClientMenuPouchInterface> {
    public static final ComponentTexture SCREEN_INTERFACE_CHROME = CatalogTextures.createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "bg.interface", new Vec2i(0, 166), new Vec2i(128, 64));
    public static final String GROUP_HOTKEY_TOGGLERS = "hotkey_togglers";

    public ScreenPouchInterface(ClientMenuPouchInterface menu, Inventory playerInventory, Component __) {
        super(SCREEN_INTERFACE_CHROME, menu, playerInventory);
        List<WidgetBasic<ScreenPouchInterface>> togglers = this.widgetGroups.computeIfAbsent(GROUP_HOTKEY_TOGGLERS, ___ -> new ArrayList<>());
    }
}
