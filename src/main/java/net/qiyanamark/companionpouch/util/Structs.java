package net.qiyanamark.companionpouch.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.qiyanamark.companionpouch.catalog.CatalogItem;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

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

        @Override
        public String toString() {
            return "Vec2i<" + this.x + ", " + this.y + ">";
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

    public static class CapabilityWrapper<Cap, K> {
        private final K capableObject;
        private final Function<K, LazyOptional<Cap>> retriever;
        private LazyOptional<Cap> inner;

        public CapabilityWrapper(K object, Function<K, LazyOptional<Cap>> retriever) {
            this.capableObject = object;
            this.retriever = retriever;
            this.inner = Objects.requireNonNull(this.retriever.apply(this.capableObject));

            if (this.inner.isPresent()) {
                this.inner.addListener(this::onCapInvalidate);
            }
        }

        public Cap get() {
            if (!this.inner.isPresent()) {
                this.inner = this.retriever.apply(this.capableObject);
            }
            return this.inner.orElseThrow(IllegalStateException::new);
        }

        private void onCapInvalidate(LazyOptional<Cap> opt) {
            this.inner = this.retriever.apply(this.capableObject);
            this.inner.addListener(this::onCapInvalidate);
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
