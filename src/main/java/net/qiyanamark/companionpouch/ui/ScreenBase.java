package net.qiyanamark.companionpouch.ui;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.qiyanamark.companionpouch.helper.HelperInventory;
import net.qiyanamark.companionpouch.menu.container.client.ClientMenuBase;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.annotations.Extends;
import net.qiyanamark.companionpouch.util.annotations.Implements;

@OnlyIn(Dist.CLIENT)
public abstract class ScreenBase<Screen extends ScreenBase<Screen, Menu>, Menu extends ClientMenuBase<Menu, ?>> extends AbstractContainerScreen<Menu> {
    protected Map<String, List<WidgetBasic<Screen>>> widgetGroups = new HashMap<>();
    protected ComponentTexture chrome;

    private boolean hoverEnabled = true;

    public ScreenBase(ComponentTexture chrome, Menu menu, Inventory playerInventory) {
        super(menu, playerInventory, TextComponent.EMPTY);

        this.chrome = chrome;
        this.imageWidth = this.chrome.getSize().x();
        this.imageHeight = this.chrome.getSize().y();
    }

    @Override
    protected void init() {
        super.init();

        this.widgetGroups.values().stream()
            .flatMap(Collection::stream)
            .forEach(WidgetBasic::reinit);
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

    protected List<WidgetBasic<Screen>> group(String key) {
        return this.widgetGroups.computeIfAbsent(key, __ -> new ArrayList<>());
    }

    protected void renderWidgetGroup(String group, PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.widgetGroups
            .get(group)
            .forEach(btn -> btn.render(poseStack, mouseX, mouseY, partialTicks));
    }

    protected void renderWidgets(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.widgetGroups.values().stream()
            .flatMap(Collection::stream)
            .forEach(b -> b.render(poseStack, mouseX, mouseY, partialTicks));
    }

    protected void renderChrome(PoseStack poseStack) {
        this.chrome.blitSlow(poseStack, this.leftPos, this.topPos);
    }

    protected void renderSlotBackgrounds(PoseStack poseStack) {
        int offsetX = this.leftPos - 1;
        int offsetY = this.topPos - 1;
        this.menu.slots.forEach(slot -> HelperInventory.MENU_SLOT.blitSlow(poseStack, slot.x + offsetX, slot.y + offsetY));
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
            .filter(Slot::isActive)
            .filter(slot -> this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY))
            .forEach(slot -> this.hoveredSlot = slot);

        super.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
        return this.hoverEnabled && super.isHovering(x, y, width, height, mouseX, mouseY);
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);

        this.hoverEnabled = false;
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderWidgets(poseStack, mouseX, mouseY, partialTicks);
        this.hoverEnabled = true;

        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        this.renderChrome(poseStack);
        this.renderSlotBackgrounds(poseStack);
    }
}
