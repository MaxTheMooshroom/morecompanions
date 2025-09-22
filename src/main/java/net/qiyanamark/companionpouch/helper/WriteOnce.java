package net.qiyanamark.companionpouch.helper;

import java.util.Optional;

public class WriteOnce<T> {
    private Optional<T> inner = Optional.empty();

    public boolean isPresent() {
        return this.inner.isPresent();
    }

    public boolean isEmpty() {
        return this.inner.isEmpty();
    }

    public T get() {
        return this.inner.orElseThrow();
    }

    public void set(T value) {
        if (this.inner.isPresent()) {
            throw new IllegalStateException("Cannot set value of an already set WriteOnce");
        }
        this.inner = Optional.of(value);
    }

    /**
     * Try to set the value instead of throwing on failure
     * @param value
     * @return whether the set was successful
     */
    public boolean trySet(T value) {
        if (this.inner.isPresent()) {
            return false;
        }

        this.inner = Optional.of(value);
        return true;
    }

    public Optional<T> tryGet() {
        return this.inner;
    }
}
