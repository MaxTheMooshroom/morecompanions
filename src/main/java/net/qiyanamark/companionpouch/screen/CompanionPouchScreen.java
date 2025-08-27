package net.qiyanamark.companionpouch.screen;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;


import net.qiyanamark.companionpouch.companionpouch;

import net.qiyanamark.companionpouch.menu.CompanionPouchMenu;


public class CompanionPouchScreen extends AbstractContainerScreen<CompanionPouchMenu> {
    private static final ResourceLocation BG = new ResourceLocation(companionpouch.MOD_ID, "textures/gui/companion_pouch.png");


    public CompanionPouchScreen(CompanionPouchMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 133; // compact
    }


    @Override
    protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BG);
        blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }


    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTicks);
        renderTooltip(pose, mouseX, mouseY);
    }
}