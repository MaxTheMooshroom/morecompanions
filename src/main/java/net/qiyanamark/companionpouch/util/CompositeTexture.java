package net.qiyanamark.companionpouch.util;

import java.util.Optional;

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
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;

/**
 * Represents a composite texture resource that can be managed and drawn
 * within Minecraft's rendering pipeline.
 * <p>
 * A {@code CompositeTexture} tracks a texture resource by {@link ResourceLocation},
 * and can create {@link ComponentTexture} instances that reference subregions of
 * the base texture for rendering.
 * </p>
 */
public class CompositeTexture {
    protected final ResourceLocation rel;

    /**
     * The full dimensions of this texture in pixels.
     */
    protected final Vec2i size;

    /**
     * Tracks whether this texture is currently CPU-side only or uploaded to the GPU.
     */
    private HardwareSide side = HardwareSide.CPU;

    /**
     * The GPU texture reference when uploaded, if present.
     */
    private Optional<AbstractTexture> texture = Optional.empty();

    /**
     * Creates a composite texture reference for the given resource.
     *
     * @param rel  the texture's resource location
     * @param size the texture's dimensions
     */
    public CompositeTexture(ResourceLocation rel, Vec2i size) {
        this.rel = rel;
        this.size = size;
    }

    /**
     * Convenience constructor using a string path.
     *
     * @param rel  string-based texture path
     * @param size the texture's dimensions
     */
    public CompositeTexture(String rel, Vec2i size) {
        this(ModCompanionPouch.rel(rel), size);
    }

    /**
     * Creates a composite texture reference with a default 256x256 size.
     *
     * @param rel the texture's resource location
     */
    public CompositeTexture(ResourceLocation rel) {
        this(rel, new Vec2i(256, 256));
    }

    /**
     * Convenience constructor with default 256x256 size from a string path.
     *
     * @param rel string-based texture path
     */
    public CompositeTexture(String rel) {
        this(ModCompanionPouch.rel(rel));
    }


    /**
     * Creates a {@link ComponentTexture} for a subregion of this texture.
     *
     * @param texturePosition the top-left coordinate within the parent texture
     * @param size            the sub-texture size
     * @return a component texture bound to this composite
     */
    public ComponentTexture getComponentTexture(Vec2i texturePosition, Vec2i size) {
        return new ComponentTexture(this, texturePosition, size);
    }

    public void prepareSlow() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.rel);
    }

    /**
     * Unbinds any texture currently bound to the OpenGL context.
     */
    public void unbind() {
        CompositeTexture.executeOnRenderThread(() -> RenderSystem.bindTexture(0));
    }

    /**
     * Releases this texture from GPU memory and marks it as CPU-side only.
     * <p>
     * If already CPU-side, this call is a no-op.
     * </p>
     */
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

    /**
     * Represents a sub-texture (rectangular region) within a parent {@link CompositeTexture}.
     * <p>
     * Provides methods to upload, bind, draw, and release VBO-backed geometry for rendering
     * that subregion.
     * </p>
     */
    public static class ComponentTexture {
        /**
         * @return a copy of this component's top-left coordinate within the parent
         */
        public Vec2i getTexturePosition() {
            return this.texturePosition.copy();
        }

        /**
         * @return a copy of this component's pixel dimensions
         */
        public Vec2i getSize() {
            return this.size.copy();
        }

        public void blitSlow(PoseStack poseStack, int x, int y) {
            this.parent.prepareSlow();
            GuiComponent.blit(poseStack, x, y, this.texturePosition.x(), this.texturePosition.y(), this.size.x(), this.size.y(), 256, 256);
        }

        /**
         * Uploads this component's vertex buffer to the GPU. Replaces any existing VBO.
         */
        public void upload() {
            CompositeTexture.executeOnRenderThread(() -> {
                if (this.vbo.isPresent()) {
                    this._close();
                }
                this._upload();
            });
        }

        /**
         * Ensures this component is uploaded and binds its vertex buffer.
         */
        public void bind() {
            CompositeTexture.executeOnRenderThread(() -> {
                if (this.vbo.isEmpty()) {
                    this._upload();
                }
                this._bind();
            });
        }

        /**
         * Binds and prepares this component for rendering, then invokes the given render action.
         *
         * @param poseStack current pose stack
         * @param runnable  rendering action consuming the context
         * @implNote
         * Using this guarantees that, at the time of invocation, both the parent {@link CompositeTexture}
         * and this {@link ComponentTexture} are uploaded and bound. This guarantee does not extend
         * to subsequent rebindings performed inside the supplied render action
         */
        public void bindFor(PoseStack poseStack, RenderAction action) {
            this.parent.bind();

            if (this.vbo.isEmpty()) {
                this._upload();
            }
            this._bind();

            RenderContext ctx = new RenderContext(poseStack);
            action.run(ctx);
        }

        /**
         * Draws this component's textured quad at the specified screen position.
         *
         * @param ctx the render context, typically supplied by
         *            {@link #bindFor(PoseStack, RenderAction)}
         * @param x   screen-space x offset
         * @param y   screen-space y offset
         *
         * @apiNote
         * This overload is designed for use inside a render action provided to
         * {@link #bindFor(PoseStack, RenderAction)}. The {@code ctx} cannot be constructed
         * manually, and is guaranteed to be valid only within the scope of the render action
         * it was provided to.
         */
        public void blit(RenderContext ctx, int x, int y) {
            CompositeTexture.executeOnRenderThread(() -> {
                if (this.parent.side == HardwareSide.CPU) {
                    this.parent._upload();
                    this.parent.bind();
                }

                if (this.vbo.isEmpty()) {
                    this._upload();
                    this._bind();
                }

                this._blit(ctx, x, y);
            });
        }

        /**
         * Shorthand for {@link #blit(RenderContext, int, int)} using a per-call
         * {@link RenderContext} construction
         *
         * @param poseStack current pose stack
         * @param x         screen-space x offset
         * @param y         screen-space y offset
         * 
         * @apiNote
         * for consecutive uses, {@link #blit(RenderContext, int, int)} within a
         * {@link RenderAction} provided to {@link #bindFor(PoseStack, RenderAction)}
         * should be preferred, as it handles setup only once for the entire render action
         */
        public void blit(PoseStack poseStack, int x, int y) {
            RenderContext ctx = new RenderContext(poseStack);
            this.blit(ctx, x, y);
        }


        /**
         * Shorthand for {@link #blit(RenderContext, int, int)} with a vector position.
         *
         * @param ctx render context
         * @param pos screen-space position
         * 
         * @apiNote
         * for consecutive uses, {@link #blit(RenderContext, Vec2i)} within a
         * {@link RenderAction} provided to {@link #bindFor(PoseStack, RenderAction)}
         * should be preferred, as it handles setup only once for the entire render action
         */
        public void blit(RenderContext ctx, Vec2i pos) {
            this.blit(ctx, pos.x(), pos.y());
        }

        /**
         * Frees the vertex buffer associated with this component. Does nothing if
         * this is already closed.
         */
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

        /**
         * Builds and uploads a VBO containing this component's textured quad.
         * <p>
         * Calculates UV coordinates relative to the parent texture dimensions.
         * Must be called on the render thread.
         */
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

        /**
         * Binds this component's vertex buffer for drawing.
         * Must be called on the render thread.
         */
        private void _bind() {
            RenderSystem.assertOnRenderThread();
            this.vbo.orElseThrow().bind();
        }

        private void _blit(RenderContext ctx, int x, int y) {
            RenderSystem.assertOnRenderThread();

            Matrix4f transform = ctx.poseStack.last().pose().copy();
            transform.translate(new Vector3f(x, y, 0));

            this.vbo.orElseThrow().drawWithShader(transform, ctx.projectionMatrix, ctx.shader);
        }

        /**
         * Releases this component's vertex buffer and clears the internal reference.
         */
        private void _close() {
            this.vbo.get().close();
            this.vbo = Optional.empty();
        }
    }

    @FunctionalInterface
    public static interface RenderAction {
        void run(RenderContext ctx);
    }

    /**
     * Encapsulates rendering state (pose stack, projection matrix, shader)
     * passed into texture draw calls.
     */
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

    /**
     * Runs a {@link RenderCall} immediately if currently on the render thread,
     * otherwise records it for deferred execution
     *
     * @param callable action to execute
     */
    private static void executeOnRenderThread(RenderCall callable) {
        if (RenderSystem.isOnRenderThread()) {
            callable.execute();
        } else {
            RenderSystem.recordRenderCall(callable);
        }
    }

    /**
     * Uploads the concrete texture to GPU memory, if not already uploaded.
     * Must be called on the render thread.
     */
    private void _upload() {
        RenderSystem.assertOnRenderThread();

        TextureManager texManager = Minecraft.getInstance().getTextureManager();
        this.texture = Optional.of(texManager.getTexture(this.rel));
        this.side = HardwareSide.GPU;
    }

    /**
     * Binds this texture to the active OpenGL context.
     * Must be called on the render thread.
     */
    private void _bind() {
        RenderSystem.assertOnRenderThread();

        int textureId = this.texture.orElseThrow().getId();
        RenderSystem.bindTexture(textureId);
    }

    /**
     * Prepares the active shader and sets the bound texture for rendering.
     * Must be called on the render thread.
     */
    private void _prepare() {
        RenderSystem.assertOnRenderThread();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem._setShaderTexture(0, this.texture.orElseThrow().getId());
    }

    /**
     * Ensures this texture is uploaded, bound, and prepared, scheduling on the
     * render thread if needed.
     */
    private void bind() {
        CompositeTexture.executeOnRenderThread(() -> {
            if (this.texture.isEmpty()) {
                this._upload();
            }
            this._bind();
            this._prepare();
        });
    }

    /**
     * Indicates whether a resource exists only CPU-side or if has been
     * uploaded to the GPU.
     */
    private static enum HardwareSide {
        CPU,
        GPU
    }
}
