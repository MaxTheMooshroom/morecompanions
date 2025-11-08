package net.qiyanamark.companionpouch.ui;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;
import net.qiyanamark.companionpouch.util.annotations.Implements;

@OnlyIn(Dist.CLIENT)
public abstract class WidgetBasic<Screen extends ScreenBase<Screen, ?>> extends GuiComponent implements Widget, GuiEventListener {
    public @NotNull Vec2i position;
    public @NotNull Vec2i size;
    public @NotNull Screen screen;

    protected @NotNull Component tooltip = TextComponent.EMPTY;

    protected boolean visible = true;
    protected boolean active = true;
    protected ComponentTexture texture;
    protected boolean isHovered;

    public WidgetBasic(@NotNull Screen screen, ComponentTexture texture, @NotNull Vec2i position, @NotNull Vec2i size) {
        this.screen = screen;
        this.texture = texture;
        this.position = position;
        this.size = size;
    }

    public WidgetBasic(@NotNull Screen screen, ComponentTexture texture, @NotNull Vec2i position, @NotNull Vec2i size, @NotNull Component tooltip) {
        this.screen = screen;
        this.texture = texture;
        this.position = position;
        this.tooltip = tooltip;
        this.size = size;
    }

    /**
     * Initializes the instance of a concrete implementor of this class. Use of this
     * function allows the entire inheritance hierarchy of this class to finish before
     * running post-init code, which may require child-classes to be initialized
     * (such as in the case of overrideable methods)
     *
     * @apiNote Is required to be called after construction because of
     *          field-initialization semantics in the constructor chain.
     */
    public abstract void postInit();

    /**
     * Called by {@link ScreenBase#init()}, so
     * screen parameters may have changed. Positions need to be recalculated, etc.
     */
    public abstract void reinit();

    /**
     * Called by {@link ScreenBase#containerTick()}
     */
    public abstract void tick();

    @Override
    @Implements(Widget.class)
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (this.visible) {
            this.texture.blitSlow(poseStack, this.position.x(), this.position.y());
        }
        this.isHovered = this.mouseInBounds(mouseX, mouseY);
    }

    public void renderTooltip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        if (this.active && this.isHovered && !this.tooltip.getString().isEmpty()) {
            this.screen.renderComponentTooltip(poseStack, Collections.singletonList(this.tooltip), mouseX, mouseY);
        }
    }

    public <Child extends WidgetBasic<Screen>> Child downcast() {
        //noinspection unchecked
        return (Child) this;
    }

    public boolean mouseInBounds(int mouseX, int mouseY) {
        boolean horiBounds = mouseX >= this.position.x() && mouseX <= this.position.x() + this.size.x();
        boolean vertBounds = mouseY >= this.position.y() && mouseY <= this.position.y() + this.size.y();

        return horiBounds && vertBounds;
    }
}
