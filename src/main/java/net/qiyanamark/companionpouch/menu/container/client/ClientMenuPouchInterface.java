package net.qiyanamark.companionpouch.menu.container.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import net.qiyanamark.companionpouch.menu.container.common.MenuSlotData;
import net.qiyanamark.companionpouch.menu.container.common.MenuSlotDataIndexed;
import net.qiyanamark.companionpouch.menu.container.server.ServerMenuPouchInterface;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;

import static net.qiyanamark.companionpouch.catalog.CatalogMenu.COMPANION_POUCH_INTERFACE_CLIENT;

public class ClientMenuPouchInterface extends ClientMenuPouchBase<ClientMenuPouchInterface, ServerMenuPouchInterface> {
    public static final ResourceLocation REL = ModCompanionPouch.rel("menu.container.client.interface");

    public static final Vec2i MENU_INTERFACE_SLOT_PADDING_LEFT_TOP = new Vec2i(18, 7);

    @SuppressWarnings("unchecked")
    private final MenuSlotDataIndexed<Boolean>[] temporalAvailable = (MenuSlotDataIndexed<Boolean>[]) new MenuSlotDataIndexed<?>[ItemPouchCompanion.MAX_SLOT_COUNT];
    private final MenuSlotData<Byte> activationIndex = new MenuSlotData<>();

    public static ClientMenuPouchInterface serverOpen(int id, Inventory __) {
        return new ClientMenuPouchInterface(id);
    }

    protected ClientMenuPouchInterface(int id) {
        super(COMPANION_POUCH_INTERFACE_CLIENT, id);
        this.policyMayPickup = false;
        this.policyMayPlace = false;
    }
}
