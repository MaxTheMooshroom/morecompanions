package net.qiyanamark.companionpouch.helper.shapes;

import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;

public class RenderContext extends PoseStack {
    protected Optional<Runnable> onRender;

    public RenderContext(PoseStack poseStack) {
        poseStack.pushPose();
        this.last().pose().load(poseStack.last().pose());
        poseStack.popPose();
    }

    public void render() {
        this.onRender.ifPresent(r -> r.run());
    }

    public void renderEach(IShape2D... shapes) {
        this.pushPose();
        
        this.popPose();
    }
}
