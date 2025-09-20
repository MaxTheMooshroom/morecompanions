package net.qiyanamark.companionpouch.helper.shapes;

import java.util.Optional;

import net.qiyanamark.companionpouch.helper.annotations.Implements;
import net.qiyanamark.companionpouch.helper.shapes.Structs.Vec2;

public class Shape2D implements IShape2D {
    protected Vec2 position = new Vec2(0, 0);
    protected Vec2 scale = new Vec2(1, 1);
    protected Vec2 rotation = new Vec2(0, 0);
    protected int depth = 0;

    protected Shape2D() {}

    @Override
    @Implements(IShape2D.class)
    public void setDepth(int z) {
        this.depth = z;
    }

    @Override
    @Implements(IShape2D.class)
    public void setPosition(Vec2 pos) {
        this.position = pos;
    }

    @Override
    @Implements(IShape2D.class)
    public void setScale(Vec2 scale) {
        this.scale = scale;
    }

    @Override
    @Implements(IShape2D.class)
    public void setRotation(Vec2 rot) {
        this.rotation = rot;
    }

    @Override
    @Implements(IShape2D.class)
    public int getDepth() {
        return depth;
    }

    @Override
    @Implements(IShape2D.class)
    public Vec2 getPosition() {
        return this.position.copy();
    }

    @Override
    @Implements(IShape2D.class)
    public Vec2 getRotation() {
        return this.rotation.copy();
    }

    @Override
    @Implements(IShape2D.class)
    public Vec2 getScale() {
        return this.scale.copy();
    }

    @Override
    public void draw(Optional<RenderContext> ctx) {
        
    }
}
