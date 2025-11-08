package net.qiyanamark.companionpouch.menu.container.common;

import java.util.ArrayList;
import java.util.List;

public class MenuSlotData<T> {
    @FunctionalInterface
    public interface DataSlotListenerIndexed<T> {
        void notify(T value);
    }

    private final List<DataSlotListenerIndexed<T>> listeners = new ArrayList<>();

    private T prevValue = null;
    private T value = null;

    public void subscribe(DataSlotListenerIndexed<T> listener) {
        this.listeners.add(listener);
    }

    public T get() {
        return this.value;
    }

    public void set(T value) {
        this.value = value;
    }

    public boolean checkAndClearUpdateFlag() {
        boolean flag = !this.value.equals(this.prevValue);
        this.prevValue = this.value;
        return flag;
    }

    public void broadcastValue() {
        for (DataSlotListenerIndexed<T> listener : this.listeners) {
            listener.notify(this.value);
        }
    }
}
