package net.qiyanamark.companionpouch.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.qiyanamark.companionpouch.catalog.CatalogItem;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;

public class Structs {
    public record Vec2(float x, float y) {
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

    public record Vec2i(int x, int y) {
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

        @Override
        public String toString() {
            return "Vec2i<" + this.x + ", " + this.y + ">";
        }
    }

    public record Vec4(int r, int g, int b, int a) {
        public Vec4 copy() {
            return new Vec4(this.r, this.g, this.b, this.a);
        }
    }

    public record Vertex2D(
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

    public static class WriteOnce<T> {
        private @Nullable T inner = null;

        public static <T> WriteOnce<T> of(T value) {
            return new WriteOnce<>(value);
        }

        public static <T> WriteOnce<T> empty() {
            return new WriteOnce<>(null);
        }

        public void write(T value) {
            if (this.inner != null) {
                throw new IllegalStateException("Cannot set the value of a WriteOnce that already has a value set");
            }
            this.inner = value;
        }

        public @NotNull T read() {
            if (this.inner == null) {
                throw new NoSuchElementException("No value present");
            }
            return this.inner;
        }

        public @NotNull Optional<T> tryRead() {
            return Optional.ofNullable(this.inner);
        }

        private WriteOnce(@Nullable T initial) {
            this.inner = initial;
        }
    }

    public static class ReadOnce<T> {
        private @Nullable T inner;

        public static <T> ReadOnce<T> of(@NotNull T value) {
            return new ReadOnce<>(Objects.requireNonNull(value));
        }

        public static <T> ReadOnce<T> empty() {
            return new ReadOnce<>(null);
        }

        public T read() {
            if (this.inner == null) {
                throw new NoSuchElementException("No value present");
            }
            T result = this.inner;
            this.inner = null;
            return result;
        }

        private ReadOnce(@Nullable T initial) {
            this.inner = initial;
        }
    }

    public enum InstanceSide {
        CLIENT,
        SERVER;

        @Override
        public String toString() {
            return this == CLIENT ? "CLIENT" : "SERVER";
        }

        public static <E extends Entity> InstanceSide from(E entity) {
            return entity.level.isClientSide ? CLIENT : SERVER;
        }
    }

    public enum LocationPouch implements IByteBufEnum<LocationPouch> {
        MAIN_HAND,
        OFF_HAND,
        CURIO;

        public static LocationPouch fromHand(InteractionHand hand) {
            return switch (hand) {
                case MAIN_HAND -> MAIN_HAND;
                case OFF_HAND -> OFF_HAND;
            };
        }

        public static Optional<Pair<LocationPouch, ItemStack>> findOnPlayer(Player player) {
            ItemPouchCompanion itemPouch = CatalogItem.COMPANION_POUCH.get();
            if (player.getMainHandItem().is(itemPouch)) {
                return Optional.of(new Pair<>(MAIN_HAND, player.getMainHandItem()));
            } else if (player.getOffhandItem().is(itemPouch)) {
                return Optional.of(new Pair<>(OFF_HAND, player.getOffhandItem()));
            } else {
                return CuriosApi.getCuriosHelper()
                        .findFirstCurio(player, stack -> stack.getItem() == CatalogItem.COMPANION_POUCH.get())
                        .map(SlotResult::stack)
                        .map(itemStack -> new Pair<>(CURIO, itemStack));
            }
        }

        public Optional<ItemStack> getFromPlayer(Player player) {
            return switch (this) {
                case MAIN_HAND -> Optional.of(player.getMainHandItem());
                case OFF_HAND -> Optional.of(player.getOffhandItem());
                case CURIO -> CuriosApi.getCuriosHelper()
                        .findFirstCurio(
                                player,
                                stack -> stack.getItem() == CatalogItem.COMPANION_POUCH.get()
                        )
                        .map(SlotResult::stack);
                default -> Optional.empty();
            };
        }
    }

    public static <T> Collector<T, ?, NonNullList<T>> toNonNullList() {
        return Collector.of(
                NonNullList::create,
                NonNullList::add,
                (left, right) -> { left.addAll(right); return left; }
        );
    }
}
