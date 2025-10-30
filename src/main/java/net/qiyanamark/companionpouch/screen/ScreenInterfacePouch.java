package net.qiyanamark.companionpouch.screen;

import java.util.stream.IntStream;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import net.qiyanamark.companionpouch.catalog.CatalogMenu;
import net.qiyanamark.companionpouch.catalog.CatalogTextures;
import net.qiyanamark.companionpouch.menu.container.MenuInterfacePouchCompanion;
import net.qiyanamark.companionpouch.screen.ui.WidgetBasic;
import net.qiyanamark.companionpouch.screen.ui.button.ButtonToggleablePush;
import net.qiyanamark.companionpouch.screen.ui.button.ButtonToggleableSlider;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;

public class ScreenInterfacePouch extends ScreenBase<MenuInterfacePouchCompanion> {
    private static final String KEY_PUSH = "group.push";
    private static final String KEY_SLIDER = "group.slider";

    public ScreenInterfacePouch(MenuInterfacePouchCompanion menuInterfacePouchCompanion, Inventory playerInventory, Component title) {
        super(CatalogTextures.SCREEN_INTERFACE_CHROME, menuInterfacePouchCompanion, playerInventory, title);
        this.widgetGroups.compute(KEY_PUSH,
            (key, __) ->
                IntStream.range(0, this.getMenu().getPouchContainer().getSlots())
                    .mapToObj(i -> {
                        Slot slot = this.getMenu().getSlot(i);
                        Vec2i position = CatalogMenu.MENU_INTERFACE_SLOT_ACTIVATE_OFFSET.add(slot.x, slot.y);
                        ButtonToggleablePush<ScreenBase<MenuInterfacePouchCompanion>, MenuInterfacePouchCompanion> button =
                            new ButtonToggleablePush<>((byte) i, this, position);

                        return (WidgetBasic<ScreenBase<MenuInterfacePouchCompanion>, MenuInterfacePouchCompanion>) button;
                    })
                    .toList()
        );
        this.widgetGroups.compute(KEY_SLIDER,
            (key, __) ->
                IntStream.range(0, this.getMenu().getPouchContainer().getSlots())
                    .mapToObj(i -> {
                        Slot slot = this.getMenu().getSlot(i);
                        Vec2i position = CatalogMenu.MENU_INTERFACE_SLOT_SELECT_HOTKEY_OFFSET.add(slot.x, slot.y);
                        ButtonToggleableSlider<ScreenBase<MenuInterfacePouchCompanion>, MenuInterfacePouchCompanion> button =
                            new ButtonToggleableSlider<>((byte) i, this, position);

                        return (WidgetBasic<ScreenBase<MenuInterfacePouchCompanion>, MenuInterfacePouchCompanion>) button;
                    })
                    .toList()
        );
    }
}
