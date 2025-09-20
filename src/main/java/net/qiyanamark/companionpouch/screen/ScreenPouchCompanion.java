package net.qiyanamark.companionpouch.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.helper.annotations.Extends;
import net.qiyanamark.companionpouch.menu.container.MenuContainerPouchCompanion;

@OnlyIn(Dist.CLIENT)
public class ScreenPouchCompanion extends AbstractContainerScreen<MenuContainerPouchCompanion> {
    private static final ResourceLocation BG = new ResourceLocation(ModCompanionPouch.MOD_ID, "textures/gui/container/pouch_companion.png");

    public ScreenPouchCompanion(MenuContainerPouchCompanion menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 133;
    }

    private void renderBgChrome(PoseStack pPoseStack) {
        // RenderSystem.setShader(GameRenderer::getPositionColorShader);
        // RenderSystem.setShaderColor(0, 0, 0, 0);
        // GuiComponent.fill(pPoseStack, this.leftPos, this.topPos, this.topPos + this.getXSize(), this.getYSize(), 0);
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BG);
        this.blit(pPoseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    protected void renderBgSlots(PoseStack pPoseStack) {
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        this.renderBgChrome(pPoseStack);
        // this.renderBgSlots(pPoseStack);
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        super.render(pose, mouseX, mouseY, partialTicks);
        this.renderTooltip(pose, mouseX, mouseY);
    }
}
