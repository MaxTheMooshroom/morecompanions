package net.qiyanamark.companionpouch.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.container.ContainerPouchCompanion;

public class ScreenPouchCompanion extends AbstractContainerScreen<ContainerPouchCompanion> {
    private static final ResourceLocation BG = new ResourceLocation(ModCompanionPouch.MOD_ID, "textures/gui/container/pouch_companion.png");

    public ScreenPouchCompanion(ContainerPouchCompanion menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 133;
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BG);
        blit(pPoseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTicks);
        renderTooltip(pose, mouseX, mouseY);
    }
}
