package net.qiyanamark.companionpouch.menu.container.client.screens.widgets;

import org.jetbrains.annotations.NotNull;

import net.qiyanamark.companionpouch.menu.container.client.screens.ScreenPouchBase;
import net.qiyanamark.companionpouch.ui.input.button.ButtonToggleableSlider;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;

public class ButtonCompanionHotkeyTargetSelector<Screen extends ScreenPouchBase<Screen, ?>> extends ButtonToggleableSlider<Screen> {
    public ButtonCompanionHotkeyTargetSelector(byte slotId, @NotNull Screen screen, @NotNull Vec2i position) {
        super(slotId, screen, BUTTON_TOGGLER_DISABLED, position);
    }
}
