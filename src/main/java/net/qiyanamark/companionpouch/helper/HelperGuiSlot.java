package net.qiyanamark.companionpouch.helper;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.qiyanamark.companionpouch.helper.shapes.Rect;
import net.qiyanamark.companionpouch.helper.shapes.RenderContext;

@OnlyIn(Dist.CLIENT)
public final class HelperGuiSlot {
    public static final int ICON_WIDTH = 16;
    public static final int ICON_HEIGHT = 16;
    public static final int ICON_SPACING = 2;

    public static final int ICON_WIDTH_TOTAL = ICON_WIDTH + ICON_SPACING;
    public static final int ICON_HEIGHT_TOTAL = ICON_HEIGHT + ICON_SPACING;

    private static final Rect SHAPE = new Rect();

    public static void renderOne(RenderContext ctx, Slot slot) {
        ctx.pushPose();

        ctx.popPose();
    }
}
