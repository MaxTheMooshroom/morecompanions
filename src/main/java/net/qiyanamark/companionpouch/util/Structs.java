package net.qiyanamark.companionpouch.util;

import java.util.Optional;

public class Structs {
    public static record Vec2(float x, float y) {
        public Vec2 copy() {
            return new Vec2(this.x, this.y);
        }

        public Vec2 add(float a, float b) {
            return new Vec2(this.x + a, this.y + b);
        }

        public Vec2 sub(float a, float b) {
            return new Vec2(this.x - a, this.y - b);
        }

        public Vec2 mul(float m) {
            return new Vec2(this.x * m, this.y * m);
        }

        public Vec2 div(float d) {
            return new Vec2(this.x / d, this.y / d);
        }
    }

    public static record Vec2i(int x, int y) {
        private Vec2i() { this(0, 0); }
        
        public Vec2i copy() {
            return new Vec2i(this.x, this.y);
        }

        public Vec2i add(int a, int b) {
            return new Vec2i(this.x + a, this.y + b);
        }

        public Vec2i sub(int a, int b) {
            return new Vec2i(this.x - a, this.y - b);
        }

        public Vec2i add(Vec2i other) {
            return new Vec2i(this.x + other.x, this.y + other.y);
        }

        public Vec2i sub(Vec2i other) {
            return new Vec2i(this.x - other.x, this.y - other.y);
        }

        public Vec2i mul(int m) {
            return new Vec2i(this.x * m, this.y * m);
        }

        public Vec2i div(int d) {
            return new Vec2i(this.x / d, this.y / d);
        }
    }

    public static record Vec4(int r, int g, int b, int a) {
        public Vec4 copy() {
            return new Vec4(this.r, this.g, this.b, this.a);
        }
    }

    public static record Vertex2D(
                Vec2 pos,
                Optional<Vec4> color,
                Optional<Vec2i> uv
            ) {
        @SuppressWarnings("unused")
        private Vertex2D() { this(null, Optional.empty(), Optional.empty()); }

        public Vertex2D(Vec2 pos, Vec4 color, Vec2i uv) { this(pos, Optional.of(color), Optional.of(uv)); }

        public Vertex2D(Vec2 pos, Vec4 color) { this(pos, Optional.of(color), Optional.empty()); }

        public Vertex2D(Vec2 pos, Vec2i uv) { this(pos, Optional.empty(), Optional.of(uv)); }

        public Vertex2D(Vec2 pos) { this(pos, Optional.empty(), Optional.empty()); }

        public Vertex2D copy() {
            return new Vertex2D(this.pos.copy(), this.color.map(c -> c.copy()), this.uv.map(uv -> uv.copy()));
        }
    }
}
