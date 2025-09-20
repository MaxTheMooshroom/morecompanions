package net.qiyanamark.companionpouch.helper.shapes;

import com.mojang.blaze3d.vertex.VertexFormat;

import net.qiyanamark.companionpouch.helper.shapes.Structs.Vertex2D;

public class Rect extends Shape2D {
    public Rect() {
        super();
    }
    
    private static final Geometry2D POINTS = new Geometry2D.Builder()
        .vertex(-1, -1)
        .vertex(1, -1)
        .vertex(-1, 1)
        .vertex(1, 1)
        .build();
}
