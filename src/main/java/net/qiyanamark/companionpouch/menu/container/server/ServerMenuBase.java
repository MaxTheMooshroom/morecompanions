package net.qiyanamark.companionpouch.menu.container.server;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public abstract class ServerMenuBase<Child extends ServerMenuBase<Child>> extends AbstractContainerMenu {
    public final ServerPlayer sPlayer;

    protected boolean policyMayPlace;
    protected boolean policyMayPickup;

    protected ServerMenuBase(MenuType<?> menuType, int id, ServerPlayer sPlayer) {
        super(menuType, id);
        this.sPlayer = sPlayer;
    }

    public Child downcast() {
        //noinspection unchecked
        return (Child) this;
    }

    protected void addSlot(int index, IItemHandler container) {
        this.addSlot(new SlotItemHandler(container, index, 0, 0) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return ServerMenuBase.this.policyMayPlace;
            }

            @Override
            public boolean mayPickup(Player playerIn) {
                return ServerMenuBase.this.policyMayPickup;
            }
        });
    }
}
