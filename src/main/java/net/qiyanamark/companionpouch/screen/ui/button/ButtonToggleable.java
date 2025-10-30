package net.qiyanamark.companionpouch.screen.ui.button;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.qiyanamark.companionpouch.menu.container.MenuPouchBase;
import net.qiyanamark.companionpouch.screen.ScreenBase;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs;
import net.qiyanamark.companionpouch.util.Structs.WriteOnce;

@OnlyIn(Dist.CLIENT)
public abstract class ButtonToggleable<Screen extends ScreenBase<Menu>, Menu extends MenuPouchBase> extends WidgetButton<Screen, Menu> {
    protected WriteOnce<ComponentTexture> imageOn = WriteOnce.empty();
    protected WriteOnce<ComponentTexture> imageOff = WriteOnce.empty();
    protected WriteOnce<ComponentTexture> imageDisabledOn = WriteOnce.empty();
    protected WriteOnce<ComponentTexture> imageDisabledOff = WriteOnce.empty();

    protected boolean state = true;
    private boolean dirty = true;

    public ButtonToggleable(@NotNull Structs.Vec2i position, @NotNull Structs.Vec2i size) {
        super(null, position, size);

        this.init();
        this.validate();
    }

    public ButtonToggleable(Screen screen, @NotNull Structs.Vec2i position, @NotNull Structs.Vec2i size) {
        super(screen, null, position, size);

        this.init();
        this.validate();
    }

    protected abstract void init();

    protected final void validate() {
        Stream.of(this.imageOn.tryRead(), this.imageOff.tryRead(), this.imageDisabledOn.tryRead(), this.imageDisabledOff.tryRead())
            .filter(Optional::isPresent)
            .map(Optional::orElseThrow)
            .filter(ct -> ct.getSize().x() != this.size.x() || ct.getSize().y() != this.size.y())
            .findAny()
            .map(ct -> {
                throw new IllegalStateException("Button textures failed validation ; mismatch size");
            });
    }

    public abstract void updateState();

    protected final void markDirty() {
        this.dirty = true;
    }

    protected final boolean getDirty() {
        return this.dirty;
    }

    public final boolean getState() {
        if (this.dirty) {
            this.updateState();
            this.dirty = false;
        }

        return this.state;
    }

    @Override
    public void tick() {
        if (this.active) {
            this.texture = this.getState() ? this.imageOn.read() : this.imageDisabledOn.read();
        } else {
            this.texture = this.getState() ? this.imageOff.read() : this.imageDisabledOff.tryRead().or(this.imageDisabledOn::tryRead).orElseThrow();
        }
    }
}
