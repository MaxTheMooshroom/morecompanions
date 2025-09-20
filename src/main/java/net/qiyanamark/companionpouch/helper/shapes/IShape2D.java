package net.qiyanamark.companionpouch.helper.shapes;

import java.util.Optional;

import net.qiyanamark.companionpouch.helper.shapes.Structs.Vec2;

public interface IShape2D {
    void setDepth(int z);

    void setPosition(Vec2 pos);
    void setScale(Vec2 scale);
    void setRotation(Vec2 rot);

    int getDepth();

    Vec2 getPosition();
    Vec2 getRotation();
    Vec2 getScale();

    void draw(Optional<RenderContext> ctx);

    default void setPosition(int x, int y) {
        this.setPosition(new Vec2(x, y));
    }
    
    default void setScale(int x, int y) {
        this.setScale(new Vec2(x, y));
    }

    default void setRotation(int x, int y) {
        this.setRotation(new Vec2(x, y));
    }
}
