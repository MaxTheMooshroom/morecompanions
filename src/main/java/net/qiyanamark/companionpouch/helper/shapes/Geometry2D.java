package net.qiyanamark.companionpouch.helper.shapes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;

import net.minecraft.resources.ResourceLocation;
import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.helper.shapes.Structs.Vec2;
import net.qiyanamark.companionpouch.helper.shapes.Structs.Vec2i;
import net.qiyanamark.companionpouch.helper.shapes.Structs.Vec4;
import net.qiyanamark.companionpouch.helper.shapes.Structs.Vertex2D;

public final class Geometry2D {
    Optional<VertexBuffer> vbo = Optional.empty();
    
    private final Vertex2D[] vertexData;
    private boolean colourUsed = false, uvUsed = false;

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static class Builder {
        private List<Vertex2D> vertexData;
        private Optional<Vec4> colour = Optional.empty();
        private Optional<ResourceLocation> texture = Optional.empty();

        private boolean colourUsed = false, uvUsed = false;

        public Builder() {
            this.vertexData = new ArrayList<>();
        }

        public Builder color(Vec4 col) {
            this.colour = Optional.of(col);
            colourUsed = true;
            return this;
        }

        public Builder color(int r, int g, int b, int a) {
            this.colour = Optional.of(new Vec4(r, g, b, a));
            colourUsed = true;
            return this;
        }

        public Builder vertex(Vec2 pos) {
            this.vertexData.add(new Vertex2D(pos, this.colour.map(c -> c.copy()), Optional.empty()));
            return this;
        }

        public Builder vertex(float x, float y) {
            this.vertexData.add(new Vertex2D(new Vec2(x, y), this.colour.map(c -> c.copy()), Optional.empty()));
            return this;
        }

        public Builder vertex(Vec2 pos, Vec4 col) {
            this.vertexData.add(new Vertex2D(pos, col));
            colourUsed = true;
            return this;
        }

        public Builder vertex(float x, float y, int r, int g, int b, int a) {
            this.vertexData.add(new Vertex2D(new Vec2(x, y), new Vec4(r, g, b, a)));
            colourUsed = true;
            return this;
        }

        public Builder vertex(Vec2 pos, Vec2i uv) {
            this.vertexData.add(new Vertex2D(pos, this.colour.map(c -> c.copy()), Optional.of(uv)));
            return this;
        }

        public Builder vertex(float x, float y, int u, int v) {
            this.vertexData.add(new Vertex2D(new Vec2(x, y), this.colour.map(c -> c.copy()), Optional.of(new Vec2i(u, v))));
            return this;
        }

        public Builder vertex(Vec2 pos, Vec4 col, Vec2i uv) {
            this.vertexData.add(new Vertex2D(pos, col, uv));
            return this;
        }

        public Builder vertex(float x, float y, int r, int g, int b, int a, int u, int v) {
            this.vertexData.add(new Vertex2D(new Vec2(x, y), new Vec4(r, g, b, a), new Vec2i(u, v)));
            return this;
        }

        public Builder texture(String rel) {
            this.texture = Optional.of(ModCompanionPouch.rel(rel));
            return this;
        }

        public Builder texture(ResourceLocation rel) {
            this.texture = Optional.of(rel);
            return this;
        }

        public Builder vertex(Vertex2D vert) {
            this.vertexData.add(vert);
            return this;
        }

        public Geometry2D build() {
            return new Geometry2D(this.vertexData.toArray(Vertex2D[]::new));
        }
    }

    private Geometry2D(Vertex2D[] vData) {
        this.vertexData = vData;
    }

    public void toGpu() {

    }

    public void bind() {
        this.vbo.get().bind();
    }

    public void draw() {
        this.vbo.get().draw();
    }

    public void unbind() {
        VertexBuffer.unbind();
    }
}
