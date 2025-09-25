package net.qiyanamark.companionpouch.util;

import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
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

    public ComponentTexture getComponentTexture(Vec2i texturePosition, Vec2i size) {
        return new ComponentTexture(this, texturePosition, size);
    }

    private void bindFor(PoseStack poseStack, Consumer<RenderContext> consumer) {
        CompositeTexture.executeOnRenderThread(() -> {
            RenderContext ctx = new RenderContext(poseStack);
            this._bind();
            this._prepare();
            consumer.accept(ctx);
        });
    }
    
    public void unbind() {
        CompositeTexture.executeOnRenderThread(() -> RenderSystem.bindTexture(0));
    }
    
    public void pull() {
        CompositeTexture.executeOnRenderThread(() -> {
            if (this.side == HardwareSide.CPU) {
                return;
            }

            this.texture.get().releaseId();
            this.texture = Optional.empty();

            Minecraft.getInstance().getTextureManager().release(this.rel);
            this.side = HardwareSide.CPU;
        });
    }

    public static class ComponentTexture {
        public Vec2i getTexturePosition() {
            return this.texturePosition.copy();
        }

        public Vec2i getSize() {
            return this.size.copy();
        }

        public void upload() {
            CompositeTexture.executeOnRenderThread(() -> {
                if (this.vbo.isPresent()) {
                    this._close();
                }

                this._upload();
            });
        }

        public void bind() {
            CompositeTexture.executeOnRenderThread(() -> {
                if (this.vbo.isEmpty()) {
                    this._upload();
                }
                this._bind();
            });
        }

        public void bindFor(PoseStack poseStack, Consumer<RenderContext> runnable) {
            this.parent.bindFor(poseStack, ctx -> {
                if (this.vbo.isEmpty()) {
                    this._upload();
                }
                this._bind();
                runnable.accept(ctx);
            });
        }

        public void blit(RenderContext ctx, int x, int y) {
            CompositeTexture.executeOnRenderThread(() -> {
                this._blit(ctx, x, y);
            });
        }

        public void blit(PoseStack poseStack, int x, int y) {
            RenderContext ctx = new RenderContext(poseStack);
            this.blit(ctx, x, y);
        }

        public void blit(RenderContext ctx, Vec2i pos) {
            this.blit(ctx, pos.x(), pos.y());
        }

        public void close() {
            if (this.vbo.isEmpty()) {
                return;
            }
            CompositeTexture.executeOnRenderThread(() -> this._close());
        }

        protected final CompositeTexture parent;
        protected final Vec2i texturePosition;
        protected final Vec2i size;

        protected Optional<VertexBuffer> vbo = Optional.empty();

        private ComponentTexture(CompositeTexture parent, Vec2i texturePosition, Vec2i size) {
            this.parent = parent;
            this.texturePosition = texturePosition;
            this.size = size;
        }

        private void _upload() {
            RenderSystem.assertOnRenderThread();

            float minU = this.texturePosition.x() / this.parent.size.x();
            float minV = this.texturePosition.y() / this.parent.size.y();
            float maxU = (this.texturePosition.x() + this.size.x()) / this.parent.size.x();
            float maxV = (this.texturePosition.y() + this.size.y()) / this.parent.size.y();

            VertexBuffer vbo = new VertexBuffer();
            BufferBuilder builder = new BufferBuilder(20);

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
            this.vbo.orElseThrow().bind();
        }

        private void _blit(RenderContext ctx, int x, int y) {
            RenderSystem.assertOnRenderThread();

            if (this.parent.side == HardwareSide.CPU) {
                this.parent._upload();
                this.parent._bind();
            }

            VertexBuffer vbo = this.vbo.orElseGet(() -> {
                this._bind();
                return this.vbo.orElseThrow();
            });

            Matrix4f transform = ctx.poseStack.last().pose().copy();
            transform.translate(new Vector3f(x, y, 0));

            vbo.drawWithShader(transform, ctx.projectionMatrix, ctx.shader);
        }

        private void _close() {
            this.vbo.get().close();
            this.vbo = Optional.empty();
        }
    }

    public static class RenderContext {
        public final PoseStack poseStack;
        public final Matrix4f projectionMatrix;
        public final ShaderInstance shader;

        private RenderContext(PoseStack poseStack) {
            this.poseStack = poseStack;
            this.projectionMatrix = RenderSystem.getProjectionMatrix();
            this.shader = RenderSystem.getShader();
        }
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

    private void _bind() {
        RenderSystem.assertOnRenderThread();

        int textureId = this.texture.orElseGet(() -> {
            this._upload();
            return this.texture.orElseThrow();
        }).getId();

        RenderSystem.bindTexture(textureId);
    }

    private void _prepare() {
        RenderSystem.assertOnRenderThread();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem._setShaderTexture(0, this.texture.orElseThrow().getId());
    }

    private static enum HardwareSide {
        CPU,
        GPU
    }
}
