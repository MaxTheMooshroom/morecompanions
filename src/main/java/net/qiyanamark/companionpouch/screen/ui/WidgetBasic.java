package net.qiyanamark.companionpouch.screen.ui;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import net.qiyanamark.companionpouch.menu.container.MenuPouchBase;
import net.qiyanamark.companionpouch.screen.ScreenBase;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;
import net.qiyanamark.companionpouch.util.Structs.WriteOnce;
import net.qiyanamark.companionpouch.util.annotations.Implements;

public abstract class WidgetBasic<Screen extends ScreenBase<Menu>, Menu extends MenuPouchBase> extends GuiComponent implements Widget, GuiEventListener {
    public final WriteOnce<Screen> screenTarget = WriteOnce.empty();

    public @NotNull Vec2i position;
    public @NotNull Vec2i size;

    protected @NotNull Component tooltip = TextComponent.EMPTY;

    protected boolean active = true;
    protected ComponentTexture texture;
    protected boolean isHovered;

    public WidgetBasic(ComponentTexture texture, @NotNull Vec2i position, @NotNull Vec2i size) {
        this.texture = texture;
        this.position = position;
        this.size = size;
    }

    public WidgetBasic(ComponentTexture texture, @NotNull Vec2i position, @NotNull Vec2i size, @NotNull Component tooltip) {
        this.texture = texture;
        this.position = position;
        this.tooltip = tooltip;
        this.size = size;
    }

    public WidgetBasic(@NotNull Screen screenTarget, ComponentTexture texture, @NotNull Vec2i position, @NotNull Vec2i size) {
        this.screenTarget.write(screenTarget);
        this.texture = texture;
        this.position = position;
        this.size = size;
    }

    public WidgetBasic(@NotNull Screen screenTarget, ComponentTexture texture, @NotNull Vec2i position, @NotNull Vec2i size, @NotNull Component tooltip) {
        this.screenTarget.write(screenTarget);
        this.texture = texture;
        this.position = position;
        this.tooltip = tooltip;
        this.size = size;
    }

    public abstract void tick();

    @Override
    @Implements(Widget.class)
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.texture.blitSlow(poseStack, mouseX, mouseY);
        this.isHovered = this.mouseInBounds(mouseX, mouseY);
    }

    public void renderTooltip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        if (this.isHovered) {
            screenTarget.tryRead().ifPresent(screen -> screen.renderComponentTooltip(poseStack, Collections.singletonList(this.tooltip), mouseX, mouseY));
        }
    }

    public void setScreen(@NotNull Screen screen) {
        this.screenTarget.write(screen);
    }

    @SuppressWarnings("unchecked")
    public <Child extends WidgetBasic<Screen, Menu>> Child downcast() {
        return (Child) this;
    }

    public <Child extends WidgetBasic<Screen, Menu>> Child apply(Consumer<Child> transformer) {
        Child child = this.downcast();
        transformer.accept(child);
        return child;
    }

    public <Data, Child extends WidgetBasic<Screen, Menu>> Child apply(Data d, BiConsumer<Data, Child> transformer) {
        Child child = this.downcast();
        transformer.accept(d, child);
        return child;
    }

    public <Child extends WidgetBasic<Screen, Menu>> Child copyInto(@NotNull Child child) {
        child.position = position;
        child.size = size;
        child.isHovered = this.isHovered;

        return child;
    }

    public boolean mouseInBounds(int mouseX, int mouseY) {
        boolean horiBounds = mouseX >= this.position.x() && mouseX <= this.position.x() + this.size.x();
        boolean vertBounds = mouseY >= this.position.y() && mouseY <= this.position.y() + this.size.y();

        return horiBounds && vertBounds;
    }
}
