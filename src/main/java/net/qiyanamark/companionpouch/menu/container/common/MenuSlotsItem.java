package net.qiyanamark.companionpouch.menu.container.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import org.apache.commons.lang3.tuple.Triple;

public class MenuSlotsItem {
    @FunctionalInterface
    public interface ItemSlotListenerIndexed {
        void notify(int index, ItemStack value);
    }

    private final List<ItemSlotListenerIndexed> listeners = new ArrayList<>();
    private final NonNullList<ItemStack> values = NonNullList.create();
    private final NonNullList<CompoundTag> prevValues = NonNullList.create();

    public MenuSlotsItem() {}
    public MenuSlotsItem(int size) {
        IntStream.range(0, size).forEach(__ -> this.add(ItemStack.EMPTY));
    }

    public void subscribe(ItemSlotListenerIndexed listener) {
        this.listeners.add(listener);
    }

    public void add(ItemStack stack) {
        this.values.add(stack);
        this.prevValues.add(new CompoundTag());
    }

    public ItemStack get(int index) {
        return this.values.get(index);
    }

    public void set(int index, ItemStack value) {
        this.values.set(index, value);
    }

    public void broadcastValue(int index, ItemStack stack) {
        for (ItemSlotListenerIndexed listener : this.listeners) {
            listener.notify(index, stack.copy());
        }
    }

    public void broadcastChanges() {
        IntStream.range(0, this.values.size())
            .mapToObj(i -> Triple.of(i, this.values.get(i), this.prevValues.get(i)))
            .filter(trip -> !trip.getMiddle().save(new CompoundTag()).equals(trip.getRight()))
            .forEach(trip -> {
                this.broadcastValue(trip.getLeft(), trip.getMiddle());
                this.prevValues.set(trip.getLeft(), trip.getMiddle().save(new CompoundTag()));
            });
    }
}
