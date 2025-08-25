package com.qiyanamark.companionpouch.client.gui;


import com.qiyanamark.companionpouch.container.CompanionPouchContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.qiyanamark.companionpouch.CompanionPouchMod;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CompanionPouchScreen extends AbstractContainerScreen<CompanionPouchContainer> {
    private static final ResourceLocation BACKGROUND_TEXTURE =
            new ResourceLocation(CompanionPouchMod.MOD_ID, "textures/gui/companion_pouch.png");

    public CompanionPouchScreen(CompanionPouchContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        this.blit(poseStack, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(poseStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);

        // Draw slot labels
        this.font.draw(poseStack, "1", 67, 25, 4210752);
        this.font.draw(poseStack, "2", 85, 25, 4210752);
        this.font.draw(poseStack, "3", 103, 25, 4210752);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }
}
