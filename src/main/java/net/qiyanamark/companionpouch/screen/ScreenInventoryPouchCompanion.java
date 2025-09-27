package net.qiyanamark.companionpouch.screen;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.qiyanamark.companionpouch.menu.container.MenuInventoryPouchCompanion;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;
import net.qiyanamark.companionpouch.util.annotations.Extends;

import net.qiyanamark.companionpouch.catalog.CatalogMenu;

@OnlyIn(Dist.CLIENT)
public class ScreenInventoryPouchCompanion extends AbstractContainerScreen<MenuInventoryPouchCompanion> {
    public ScreenInventoryPouchCompanion(MenuInventoryPouchCompanion menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = CatalogMenu.SCREEN_INVENTORY_CHROME.getSize().x();
        this.imageHeight = CatalogMenu.SCREEN_INVENTORY_CHROME.getSize().y();
    }

    public static Vec2i getSize() {
        return CatalogMenu.SCREEN_INVENTORY_CHROME.getSize().copy();
    }

    protected void renderChrome(PoseStack poseStack) {
        CatalogMenu.SCREEN_INVENTORY_CHROME.blitSlow(poseStack, this.leftPos, this.topPos);
    }

    protected void renderSlots(PoseStack poseStack) {
        this.menu.slots.forEach(slot -> CatalogMenu.MENU_SLOT.blitSlow(poseStack, this.leftPos + slot.x - 1, this.topPos + slot.y - 1));
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    protected void renderBg(PoseStack poseStack, float pPartialTick, int pMouseX, int pMouseY) {
        CatalogMenu.TEXTURE_ATLAS_MENUS_POUCH.prepareSlow();

        this.renderChrome(poseStack);
        this.renderSlots(poseStack);
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }
}
