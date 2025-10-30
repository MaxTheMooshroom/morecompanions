package net.qiyanamark.companionpouch.screen.ui.button;

import org.jetbrains.annotations.NotNull;

import net.qiyanamark.companionpouch.menu.container.MenuPouchBase;
import net.qiyanamark.companionpouch.screen.ScreenBase;
import net.qiyanamark.companionpouch.screen.ui.WidgetBasic;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs;

public abstract class WidgetButton<Screen extends ScreenBase<Menu>, Menu extends MenuPouchBase> extends WidgetBasic<Screen, Menu> {
    public WidgetButton(ComponentTexture texture, Structs.@NotNull Vec2i position, Structs.@NotNull Vec2i size) {
        super(texture, position, size);
    }

    public WidgetButton(@NotNull Screen screen, ComponentTexture texture, Structs.@NotNull Vec2i position, Structs.@NotNull Vec2i size) {
        super(screen, texture, position, size);
    }

    public abstract void onClick(double mouseX, double mouseY, int button);

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.mouseInBounds((int) mouseX, (int) mouseY)) {
            this.onClick(mouseX, mouseY, button);
            return true;
        }

        return false;
    }
}
