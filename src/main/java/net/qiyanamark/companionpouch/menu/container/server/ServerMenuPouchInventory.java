package net.qiyanamark.companionpouch.menu.container.server;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.menu.container.common.MenuSlotDataIndexed;
import net.qiyanamark.companionpouch.util.Structs.LocationPouch;

import static net.qiyanamark.companionpouch.catalog.CatalogMenu.COMPANION_POUCH_INVENTORY_CLIENT;

public class ServerMenuPouchInventory extends ServerMenuPouchBase<ServerMenuPouchInventory> {
    public static final ResourceLocation REL = ModCompanionPouch.rel("menu.container.server.inventory");
    public static final Component SCREEN_I18N = new TranslatableComponent("screen.container.companionpouch.inventory");

    private MenuSlotDataIndexed<Integer> curseStacks;

    protected ServerMenuPouchInventory(int id, Inventory inv, LocationPouch location) {
        super(COMPANION_POUCH_INVENTORY_CLIENT, id, inv, location);
    }

    public static SimpleMenuProvider fromLocation(LocationPouch location) {
        return new SimpleMenuProvider(
            (id, inv, player) -> new ServerMenuPouchInventory(id, inv, location),
            SCREEN_I18N
        );
    }
}
