package net.qiyanamark.companionpouch.screen;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import net.qiyanamark.companionpouch.catalog.CatalogTextures;
import net.qiyanamark.companionpouch.menu.container.MenuPouchBase;
import net.qiyanamark.companionpouch.screen.ui.WidgetBasic;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.annotations.Extends;
import net.qiyanamark.companionpouch.util.annotations.Implements;

public class ScreenBase<Menu extends MenuPouchBase> extends AbstractContainerScreen<Menu> {
    protected Map<String, List<WidgetBasic<ScreenBase<Menu>, Menu>>> widgetGroups = new HashMap<>();
    protected ComponentTexture background;

    private boolean hoverEnabled = true;

    public ScreenBase(ComponentTexture background, Menu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.background = background;
        this.imageWidth = this.background.getSize().x();
        this.imageHeight = this.background.getSize().y();
    }

    @Override
    public void containerTick() {
        this.widgetGroups.values().stream()
            .flatMap(Collection::stream)
            .forEach(WidgetBasic::tick);
    }

    @Override
    @Implements(value = ContainerEventHandler.class, introducedBy = AbstractContainerEventHandler.class)
    public boolean mouseClicked(double mouseX, double mouseY, int buttonWhich) {
        boolean buttonClicked = this.widgetGroups.values().stream()
            .flatMap(Collection::stream)
            .anyMatch(b -> b.mouseClicked(mouseX, mouseY, buttonWhich));

        // short-circuits
        return buttonClicked || super.mouseClicked(mouseX, mouseY, buttonWhich);
    }

    protected void renderButtonGroup(String group, PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.widgetGroups
            .get(group)
            .forEach(btn -> btn.render(poseStack, mouseX, mouseY, partialTicks));
    }

    protected void renderButtons(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.widgetGroups.values().stream()
            .flatMap(Collection::stream)
            .forEach(b -> b.render(poseStack, mouseX, mouseY, partialTicks));
    }

    protected void renderChrome(PoseStack poseStack) {
        this.background.blitSlow(poseStack, this.leftPos, this.topPos);
    }

    protected void renderSlotBackgrounds(PoseStack poseStack) {
        int offsetX = this.leftPos - 1;
        int offsetY = this.topPos - 1;
        this.menu.slots.forEach(slot -> CatalogTextures.MENU_SLOT.blitSlow(poseStack, slot.x + offsetX, slot.y + offsetY));
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    protected void renderLabels(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        // don't render labels
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    public void renderTooltip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        this.widgetGroups.values().stream()
            .flatMap(Collection::stream)
            .forEach(b -> b.renderTooltip(poseStack, mouseX, mouseY));

        this.menu.slots.stream()
            .filter(slot -> this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY))
            .filter(Slot::isActive)
            .forEach(slot -> this.hoveredSlot = slot);

        super.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        return this.hoverEnabled && super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);

        this.hoverEnabled = false;
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderButtons(poseStack, mouseX, mouseY, partialTicks);
        this.hoverEnabled = true;

        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    protected void renderBg(@NotNull PoseStack poseStack, float pPartialTick, int pMouseX, int pMouseY) {
        this.renderChrome(poseStack);
        this.renderSlotBackgrounds(poseStack);
    }
}
