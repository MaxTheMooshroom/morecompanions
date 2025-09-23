package net.qiyanamark.companionpouch.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.qiyanamark.companionpouch.menu.container.MenuInventoryPouchCompanion;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;
import net.qiyanamark.companionpouch.util.annotations.Extends;

import static net.qiyanamark.companionpouch.catalog.CatalogMenu.SCREEN_INVENTORY_CHROME;
import static net.qiyanamark.companionpouch.catalog.CatalogMenu.MENU_SLOT;

@OnlyIn(Dist.CLIENT)
public class ScreenInventoryPouchCompanion extends AbstractContainerScreen<MenuInventoryPouchCompanion> {
    public ScreenInventoryPouchCompanion(MenuInventoryPouchCompanion menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = SCREEN_INVENTORY_CHROME.getSize().x();
        this.imageHeight = SCREEN_INVENTORY_CHROME.getSize().y();
    }

    public static Vec2i getSize() {
        return SCREEN_INVENTORY_CHROME.getSize().copy();
    }

    protected void renderBgChrome(PoseStack poseStack) {
        SCREEN_INVENTORY_CHROME.blit(poseStack, this.leftPos, this.topPos);
    }

    protected void renderBgSlots(PoseStack poseStack) {
        MENU_SLOT.bindFor(poseStack, () -> {
            this.menu.slots.forEach(slot -> MENU_SLOT.blit(poseStack, this.leftPos + slot.x, this.topPos + slot.y));
        });
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    protected void renderBg(PoseStack poseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        this.renderBgChrome(poseStack);
        this.renderBgSlots(poseStack);
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }
}
