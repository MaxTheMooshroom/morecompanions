package net.qiyanamark.companionpouch.ui.input.button;

import org.jetbrains.annotations.NotNull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.qiyanamark.companionpouch.ui.ScreenBase;
import net.qiyanamark.companionpouch.ui.WidgetBasic;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;
import net.qiyanamark.companionpouch.util.annotations.Extends;

@OnlyIn(Dist.CLIENT)
public abstract class ButtonToggleable<Screen extends ScreenBase<Screen, ?>> extends WidgetButton<Screen> {
    protected ComponentTexture imagePressedEnabled;
    protected ComponentTexture imageUnpressedEnabled;
    protected ComponentTexture imagePressedDisabled;
    protected ComponentTexture imageUnpressedDisabled;

    protected boolean pressed = true;

    private boolean dirty = true;

    public ButtonToggleable(@NotNull Screen screen, @NotNull Vec2i position, @NotNull Vec2i size) {
        super(screen, null, position, size);
    }

    public ButtonToggleable(@NotNull Screen screen, @NotNull ComponentTexture texture, @NotNull Vec2i position, @NotNull Vec2i size) {
        super(screen, texture, position, size);
    }

    protected final void validate() {
        if (this.imagePressedEnabled == null) {
            throw new IllegalStateException("imagePressedEnabled cannot be null");
        } else if (this.imageUnpressedEnabled == null) {
            throw new IllegalStateException("imageUnpressedEnabled cannot be null");
        }

        double imagePressedEnabledMag = this.imagePressedEnabled.getSize().sub(this.size).mag();
        double imageUnpressedEnabledMag = this.imageUnpressedEnabled.getSize().sub(this.size).mag();
        double imagePressedDisabledMag = this.imagePressedDisabled.getSize().sub(this.size).mag();
        double imageUnpressedDisabledMag = this.imageUnpressedDisabled.getSize().sub(this.size).mag();

        if (imagePressedEnabledMag != 0.) {
            throw new IllegalStateException("Button textures failed validation ; mismatch size");
        } else if (imageUnpressedEnabledMag != 0.) {
            throw new IllegalStateException("Button textures failed validation ; mismatch size");
        } else if (imagePressedDisabledMag != 0.) {
            throw new IllegalStateException("Button textures failed validation ; mismatch size");
        } else if (imageUnpressedDisabledMag != 0.) {
            throw new IllegalStateException("Button textures failed validation ; mismatch size");
        }
    }

    @Override
    public void postInit() {
        this.validate();
    }

    public abstract void updateToggleState();

    protected final void markDirty() {
        this.dirty = true;
    }

    protected final boolean getDirty() {
        return this.dirty;
    }

    public final boolean isPressed() {
        if (this.dirty) {
            this.updateToggleState();
            this.dirty = false;
        }

        return this.pressed;
    }

    @Override
    @Extends(WidgetBasic.class)
    public void tick() {
        if (this.active) {
            this.texture = this.isPressed() ? this.imagePressedEnabled : this.imageUnpressedEnabled;
        } else {
            this.texture = this.isPressed() ? this.imagePressedDisabled : this.imageUnpressedDisabled;
        }
    }
}
