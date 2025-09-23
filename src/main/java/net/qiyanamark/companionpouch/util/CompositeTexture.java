package net.qiyanamark.companionpouch.util;

import java.util.Optional;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;

public class CompositeTexture {
    protected final ResourceLocation rel;
    protected final Vec2i size;

    private HardwareSide side = HardwareSide.CPU;
    private Optional<AbstractTexture> texture = Optional.empty();

    public static class ComponentTexture {
        protected final CompositeTexture parent;
        protected final Vec2i texturePosition;
        protected final Vec2i size;
        
        protected Optional<VertexBuffer> vbo = Optional.empty();

        private ComponentTexture(CompositeTexture parent, Vec2i texturePosition, Vec2i size) {
            this.parent = parent;
            this.texturePosition = texturePosition;
            this.size = size;
        }

        public Vec2i getTexturePosition() {
            return this.texturePosition.copy();
        }

        public Vec2i getSize() {
            return this.size.copy();
        }

        public void upload() {
            float minU = this.texturePosition.x() / this.parent.size.x();
            float minV = this.texturePosition.y() / this.parent.size.y();
            float maxU = (this.texturePosition.x() + this.size.x()) / this.parent.size.x();
            float maxV = (this.texturePosition.y() + this.size.y()) / this.parent.size.y();

            VertexBuffer vbo = new VertexBuffer();
            BufferBuilder builder = new BufferBuilder(4);
            builder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(0, 0, 0).uv(minU, maxV).endVertex();
            builder.vertex(this.size.x(), 0, 0).uv(maxU, maxV).endVertex();
            builder.vertex(this.size.x(), this.size.y(), 0).uv(maxU, minV).endVertex();
            builder.vertex(0, this.size.y(), 0).uv(minU, minV).endVertex();
            builder.end();

            vbo.upload(builder);

            this.vbo = Optional.of(vbo);
        }

        private void _bind() {
            RenderSystem.assertOnRenderThread();

            this.parent._bind();

            if (this.vbo.isEmpty()) {
                this.upload();
            }

            this.vbo.orElseThrow().bind();
        }

        public void bind() {
            CompositeTexture.executeOnRenderThread(() -> this._bind());
        }

        public void bindFor(PoseStack poseStack, Runnable runnable) {
            this.parent.bindFor(poseStack, () -> {
                this._bind();
                runnable.run();
            });
        }

        private void _blit(PoseStack poseStack, int x, int y) {
            RenderSystem.assertOnRenderThread();

            if (this.parent.side == HardwareSide.CPU) {
                this.parent.upload();
                this.parent.bind();
            }

            this.vbo.ifPresentOrElse(
                vbo -> vbo.draw(),
                () -> {
                    this.upload();
                    this.bind();
                    this.vbo.get().draw();
                }
            );
        }

        public void blit(PoseStack poseStack, int x, int y) {
            CompositeTexture.executeOnRenderThread(() -> this._blit(poseStack, x, y));
        }
        
        public void blit(PoseStack poseStack, Vec2i pos) {
            this.blit(poseStack, pos.x(), pos.y());
        }

        public void close() {
            if (this.vbo.isEmpty()) {
                return;
            }

            this.vbo.get().close();
            this.vbo = Optional.empty();
        }
    }

    public CompositeTexture(ResourceLocation rel, Vec2i size) {
        this.rel = rel;
        this.size = size;
    }

    public CompositeTexture(String rel, Vec2i size) {
        this(ModCompanionPouch.rel(rel), size);
    }

    public CompositeTexture(ResourceLocation rel) {
        this(rel, new Vec2i(256, 256));
    }

    public CompositeTexture(String rel) {
        this(ModCompanionPouch.rel(rel));
    }

    public ComponentTexture getComponent(Vec2i texturePosition, Vec2i size) {
        return new ComponentTexture(this, texturePosition, size);
    }

    private static void executeOnRenderThread(RenderCall callable) {
        if (RenderSystem.isOnRenderThread()) {
            callable.execute();
        } else {
            RenderSystem.recordRenderCall(callable);
        }
    }

    private void _upload() {
        RenderSystem.assertOnRenderThread();

        TextureManager texManager = Minecraft.getInstance().getTextureManager();
        this.texture = Optional.of(texManager.getTexture(this.rel));
        this.side = HardwareSide.GPU;
    }

    public void upload() {
        CompositeTexture.executeOnRenderThread(() -> this._upload());
    }

    private void _bind() {
        RenderSystem.assertOnRenderThread();

        this.texture.ifPresentOrElse(
            tex -> RenderSystem.bindTexture(tex.getId()),
            () -> {
                this._upload();
                RenderSystem.bindTexture(this.texture.orElseThrow().getId());
            }
        );
    }

    public void bind() {
        CompositeTexture.executeOnRenderThread(() -> this._bind());
    }

    public void unbind() {
        RenderSystem.bindTexture(0);
    }

    public void pull() {
        CompositeTexture.executeOnRenderThread(() -> {
            if (this.side == HardwareSide.CPU) {
                return;
            }

            Minecraft.getInstance().getTextureManager().release(this.rel);
            this.side = HardwareSide.CPU;
        });
    }

    public void bindFor(PoseStack poseStack, Runnable consumer) {
        CompositeTexture.executeOnRenderThread(() -> {
            this._bind();
            consumer.run();
        });
    }

    private static enum HardwareSide {
        CPU,
        GPU
    }
}
