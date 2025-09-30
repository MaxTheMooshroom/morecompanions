package net.qiyanamark.companionpouch.capabilities;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.common.capabilities.Capability;

import iskallia.vault.item.CompanionItem;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import net.qiyanamark.companionpouch.util.annotations.Extends;
import net.qiyanamark.companionpouch.util.annotations.Implements;
import org.jetbrains.annotations.NotNull;

public class ProviderCapabilityPouchCompanion extends ItemStackHandler implements ICapabilityProvider, IDataPouchCompanion {
    public ProviderCapabilityPouchCompanion(ItemStack stack, @Nullable CompoundTag nbt) {
        super(ProviderCapabilityPouchCompanion.getSizeOrDefault(nbt));

        this.stack = stack;
        this.initFromNbt(nbt);

        this.load();
        this.lazy.resolve();
    }

    private static Optional<Integer> getSize(@Nullable CompoundTag nbt) {
        if (nbt != null && nbt.contains(SIZE_KEY)) {
            return Optional.of((int) nbt.getByte(SIZE_KEY));
        } else {
            return Optional.empty();
        }
    }

    private static int getSizeOrDefault(@Nullable CompoundTag nbt) {
        return ProviderCapabilityPouchCompanion.getSize(nbt).orElse(ItemPouchCompanion.DEFAULT_SLOT_COUNT);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Implements(ICapabilityProvider.class)
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (LazyOptional<T>) this.lazy;
        } else if (cap == CapabilityDataPouchCompanion.COMPANION_POUCH_CAPABILITY) {
            return (LazyOptional<T>) this.lazy;
        } else {
            return LazyOptional.empty();
        }
    }

    @Override
    @Implements(value = IItemHandler.class, introducedBy = ItemStackHandler.class)
    public boolean isItemValid(int slot, ItemStack insert) {
        return insert.getItem() instanceof CompanionItem;
    }

    @Override
    @Implements(IDataPouchCompanion.class)
    public int getActivationIndex() {
        return this.activationIndex;
    }

    @Override
    @Implements(IDataPouchCompanion.class)
    public void setActivationIndex(int index) {
        this.activationIndex = index;
        this.save();
    }

    @Override
    @Implements(IDataPouchCompanion.class)
    public int getSize() {
        return this.size;
    }

    @Override
    @Implements(IDataPouchCompanion.class)
    public NonNullList<ItemStack> getItemStacks() {
        return this.stacks;
    }

    @Override
    @Implements(IDataPouchCompanion.class)
    public NonNullList<Pair<Integer, ItemStack>> getCompanions() {
        NonNullList<Pair<Integer, ItemStack>> list = NonNullList.createWithCapacity(this.size);
        IntStream.range(0, this.size)
            .mapToObj(i -> new Pair<>(i, this.getStackInSlot(i)))
            .filter(pair -> !pair.getSecond().isEmpty())
            .forEach(list::add);
        return list;
    }

    @Override
    @Extends(ItemStackHandler.class)
    protected void onContentsChanged(int slot) {
        ItemStack old = this.getStackInSlot(slot);
        ItemStack newStack = this.stacks.get(slot);

        if (!ItemStack.matches(old, newStack)) {
            this.save();
        }
    }

    private static final String SIZE_KEY = "size";
    private static final String ACTIVATE_KEY = "activate";
    private static final String STORAGE_KEY = "contents";

    private final ItemStack stack;
    private final LazyOptional<?> lazy = LazyOptional.of(() -> this);
    
    private int size = 0;
    private int activationIndex = 0;

    private void initFromNbt(@Nullable CompoundTag nbt) {
        if (nbt == null) {
            return;
        }

        CompoundTag stackData = this.stack.getOrCreateTag();

        if (nbt.contains(STORAGE_KEY)) {
            stackData.put(STORAGE_KEY, Objects.requireNonNull(nbt.get(STORAGE_KEY)));
        }

        int size = nbt.contains(SIZE_KEY) ? nbt.getInt(SIZE_KEY) : 3;
        stackData.putInt(SIZE_KEY, size);

        byte activateSlotIndex = nbt.contains(ACTIVATE_KEY) ? nbt.getByte(ACTIVATE_KEY) : (byte) 0;
        stackData.putByte(ACTIVATE_KEY, activateSlotIndex);
    }

    private void load() {
        CompoundTag tag = this.stack.getOrCreateTag();
        if (tag.contains(STORAGE_KEY)) {
            this.deserializeNBT(tag.getCompound(STORAGE_KEY));
        }
        if (tag.contains(SIZE_KEY)) {
            this.size = tag.getInt(SIZE_KEY);
        }
        if (tag.contains(ACTIVATE_KEY)) {
            this.activationIndex = tag.getByte(ACTIVATE_KEY);
        }
    }

    private void save() {
        CompoundTag tag = this.stack.getOrCreateTag();
        tag.put(STORAGE_KEY, this.serializeNBT());
        tag.putByte(SIZE_KEY, (byte) this.size);
        tag.putByte(ACTIVATE_KEY, (byte) this.activationIndex);
    }
}
