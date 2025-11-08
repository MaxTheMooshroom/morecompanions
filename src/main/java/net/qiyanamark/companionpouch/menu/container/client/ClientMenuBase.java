package net.qiyanamark.companionpouch.menu.container.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.menu.container.server.ServerMenuBase;

public abstract class ClientMenuBase<Child extends ClientMenuBase<Child, Server>, Server extends ServerMenuBase<Server>> extends AbstractContainerMenu {
    public final LocalPlayer lPlayer;

    protected boolean initialized;

    protected ClientMenuBase(@Nullable MenuType<Child> menuType, int id) {
        super(menuType, id);
        this.suppressRemoteUpdates();
        this.lPlayer = ModCompanionPouch.getClientPlayer().orElseThrow();
    }

    public void setInitialized() {
        this.initialized = true;
    }

    public boolean isInitializedByServer() {
        return this.initialized;
    }

    public Child downcast() {
        //noinspection unchecked
        return (Child) this;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}
