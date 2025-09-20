package net.qiyanamark.companionpouch.helper.shapes;

import com.mojang.blaze3d.vertex.VertexFormat;

public class Circle extends Shape2D {
    public static final int CIRCLE_SEGMENTS = 32;
    public static final Geometry2D POINTS;

    public Circle() {
        super();
    }
    
    static {
        double arcLength = 2.0 * Math.PI / CIRCLE_SEGMENTS;
        Geometry2D.Builder builder = new Geometry2D.Builder();
        builder.vertex(0, 0);
        for (int i = 0; i <= CIRCLE_SEGMENTS; i++) {
            double angle = i * arcLength;
            builder.vertex((float) Math.cos(angle), (float) Math.sin(angle));
        }
        POINTS = builder.build();
    }
}
