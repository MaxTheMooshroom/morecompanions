package net.qiyanamark.companionpouch.ui.input.button;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.gui.components.events.GuiEventListener;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.qiyanamark.companionpouch.ui.ScreenBase;
import net.qiyanamark.companionpouch.ui.WidgetBasic;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs;
import net.qiyanamark.companionpouch.util.annotations.Implements;

@OnlyIn(Dist.CLIENT)
public abstract class WidgetButton<Screen extends ScreenBase<Screen, ?>> extends WidgetBasic<Screen> {
    public WidgetButton(@NotNull Screen screen, ComponentTexture texture, Structs.@NotNull Vec2i position, Structs.@NotNull Vec2i size) {
        super(screen, texture, position, size);
    }

    public abstract void onClick(double mouseX, double mouseY, int button);

    @Override
    @Implements(value = GuiEventListener.class, introducedBy = WidgetBasic.class)
    public boolean mouseClicked(double mouseX, double mouseY, int buttonWhich) {
        if (buttonWhich == 0 && this.mouseInBounds((int) mouseX, (int) mouseY)) {
            this.onClick(mouseX, mouseY, buttonWhich);
            return true;
        }

        return false;
    }
}
