package net.qiyanamark.companionpouch.helper.shapes;

import java.util.Optional;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.resources.ResourceLocation;

import net.qiyanamark.companionpouch.ModCompanionPouch;

public class Mesh2D {
    private Geometry2D geometry;
    private VertexFormat.Mode vertexFormat;
    private Optional<ResourceLocation> texture = Optional.empty();
    private int[] ibo;

    public Mesh2D(Geometry2D geo, VertexFormat.Mode fmt, int... buf) {
        this.geometry = geo;
        this.vertexFormat = fmt;
        this.ibo = buf;
    }

    public Mesh2D(Geometry2D geo, VertexFormat.Mode fmt, ResourceLocation texture, int... buf) {
        this.geometry = geo;
        this.vertexFormat = fmt;
        this.texture = Optional.of(texture);
        this.ibo = buf;
    }

    public void ibo(int... buf) {
        this.ibo = buf;
    }

    public Mesh2D texture(ResourceLocation rel) {
        this.texture = Optional.of(rel);
        return this;
    }

    public Mesh2D texture(String rel) {
        this.texture = Optional.of(ModCompanionPouch.rel(rel));
        return this;
    }

    public void upload() {

    }

    public void bind() {

    }

    public void draw() {

    }

    public static void unbind() {

    }

    public static void delete() {

    }
}
