package net.qiyanamark.companionpouch.menu.container.server;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import net.qiyanamark.companionpouch.container.ContainerItemCurioPouch;
import net.qiyanamark.companionpouch.menu.container.common.MenuSlotData;
import net.qiyanamark.companionpouch.util.Structs;

import static net.minecraft.Util.NIL_UUID;

public abstract class ServerMenuPouchBase<Child extends ServerMenuPouchBase<Child>> extends ServerMenuBase<ServerMenuPouchBase<Child>> {
    protected final ContainerItemCurioPouch pouchContainer;

    protected final MenuSlotData<Byte> activationIndex = new MenuSlotData<>();

    protected ServerMenuPouchBase(MenuType<?> menuType, int id, Inventory inv, Structs.LocationPouch location) {
        super(menuType, id, (ServerPlayer) inv.player);
        this.pouchContainer = new ContainerItemCurioPouch(this.sPlayer, location);
        this.activationIndex.set(this.pouchContainer.getActivationIndex());
    }

    public ContainerItemCurioPouch getPouchContainer() {
        return this.pouchContainer;
    }

    public byte getActivationIndex() {
        return this.activationIndex.get();
    }

    public void setActivationIndex(byte index) {
        this.pouchContainer.player.sendMessage(new TextComponent("server menu tried setting activation index"), NIL_UUID);
        this.pouchContainer.setActivationIndex(index);
        this.activationIndex.set(index);
        this.broadcastChanges();
    }

    public void addSlot(int index) {
        this.addSlot(index, this.pouchContainer);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.pouchContainer.getPouchLocation().getFromEntity(player)
            .map(pouchStack -> pouchStack.equals(this.pouchContainer.getPouchStack()))
            .orElse(false);
    }
}
