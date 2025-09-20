package net.qiyanamark.companionpouch.helper;

import java.util.Optional;

public class Either<L, R> {
    private final Optional<L> left;
    private final Optional<R> right;

    private Either(Optional<L> l, Optional<R> r) {
        this.left = l;
        this.right = r;
    }

    public static <L, R> Either<L, R> left(L value) {
        return new Either<>(Optional.of(value), Optional.empty());
    }

    public static <L, R> Either<L, R> right(R value) {
        return new Either<>(Optional.empty(), Optional.of(value));
    }

    public boolean isLeft() {
        return this.left.isPresent();
    }

    public boolean isRight() {
        return this.right.isPresent();
    }

    public Optional<L> left() {
        return this.left;
    }

    public Optional<R> right() {
        return this.right;
    }

    public L leftUnchecked() {
        return left.get();
    }

    public R rightUnchecked() {
        return right.get();
    }
}