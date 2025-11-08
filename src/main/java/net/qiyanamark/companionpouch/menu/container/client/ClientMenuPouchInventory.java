package net.qiyanamark.companionpouch.menu.container.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.menu.container.common.MenuSlotData;
import net.qiyanamark.companionpouch.menu.container.server.ServerMenuPouchInventory;

import static net.qiyanamark.companionpouch.catalog.CatalogMenu.COMPANION_POUCH_INVENTORY_CLIENT;

public class ClientMenuPouchInventory extends ClientMenuPouchBase<ClientMenuPouchInventory, ServerMenuPouchInventory> {
    public static final ResourceLocation REL = ModCompanionPouch.rel("menu.container.client.inventory");

    private final MenuSlotData<Byte> activationIndex = new MenuSlotData<>();
    private final MenuSlotData<Integer> curseStacks = new MenuSlotData<>();

    public ClientMenuPouchInventory(int id) {
        super(COMPANION_POUCH_INVENTORY_CLIENT, id);
        this.addPlayerInventory();
    }

    public static ClientMenuPouchInventory serverOpen(int id, Inventory __) {
        return new ClientMenuPouchInventory(id);
    }
}
