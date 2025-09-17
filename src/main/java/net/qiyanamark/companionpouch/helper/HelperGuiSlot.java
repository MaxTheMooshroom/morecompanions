package net.qiyanamark.companionpouch.helper;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

import net.qiyanamark.companionpouch.ModCompanionPouch;

public final class HelperGuiSlot {
    public static final ResourceLocation ICON_REL = new ResourceLocation(ModCompanionPouch.MOD_ID, "textures/gui/slot.png");
    public static final int ICON_WIDTH = 16;
    public static final int ICON_HEIGHT = 16;

    public static void prepareRenderer() {
        RenderSystem.setShaderTexture(0, ICON_REL);
    }

    public static void renderOne(PoseStack pPoseStack, Slot slot, boolean prepareRenderer) {
        if (prepareRenderer) {
            HelperGuiSlot.prepareRenderer();
        }
        GuiComponent.blit(pPoseStack, slot.x, slot.y, 0, 0, ICON_WIDTH, ICON_HEIGHT, ICON_WIDTH, ICON_HEIGHT);
    }

    public static void renderOne(PoseStack pPoseStack, Slot slot) {
        HelperGuiSlot.renderOne(pPoseStack, slot, true);
    }

    public static void renderEach(PoseStack pPoseStack, NonNullList<Slot> slots) {
        HelperGuiSlot.prepareRenderer();
        for (Slot slot : slots) {
            HelperGuiSlot.renderOne(pPoseStack, slot, false);
        }
    }
}
