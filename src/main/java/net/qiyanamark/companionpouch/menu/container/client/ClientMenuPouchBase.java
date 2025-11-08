package net.qiyanamark.companionpouch.menu.container.client;

import net.minecraft.world.inventory.MenuType;

import net.qiyanamark.companionpouch.helper.HelperInventory;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import net.qiyanamark.companionpouch.menu.container.common.MenuSlotsItem;
import net.qiyanamark.companionpouch.menu.container.server.ServerMenuPouchBase;

public abstract class ClientMenuPouchBase<Child extends ClientMenuPouchBase<Child, Server>, Server extends ServerMenuPouchBase<Server>> extends ClientMenuBase<Child, ServerMenuPouchBase<Server>> {
    protected boolean policyMayPlace;
    protected boolean policyMayPickup;

    private final MenuSlotsItem pouchStacks = new MenuSlotsItem(ItemPouchCompanion.MAX_SLOT_COUNT);

    protected ClientMenuPouchBase(MenuType<Child> menuType, int id) {
        super(menuType, id);
    }

    protected final void addPlayerInventory() {
        HelperInventory.playerInventory(lPlayer.getInventory()).forEach(this::addSlot);
    }
}
