package net.qiyanamark.companionpouch.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.common.capabilities.Capability;

import java.util.Optional;

import javax.annotation.Nullable;

import iskallia.vault.item.CompanionItem;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import net.qiyanamark.companionpouch.util.annotations.Extends;
import net.qiyanamark.companionpouch.util.annotations.Implements;

public class CapabilitiesPouchCompanion extends ItemStackHandler implements ICapabilityProvider, ITemporalIndex {
    private static final String SIZE_KEY = "size";
    private static final String ACTIVATE_KEY = "activate";
    private static final String STORAGE_KEY = "contents";

    private final ItemStack stack;
    private final LazyOptional<?> lazy = LazyOptional.of(() -> this);

    private int activationIndex = 0;

    public CapabilitiesPouchCompanion(ItemStack stack, @Nullable CompoundTag nbt) {
        super(CapabilitiesPouchCompanion.getSizeOrDefault(nbt));

        this.stack = stack;
        initFromNbt(nbt);

        load();
        this.lazy.resolve();
    }

    public static Optional<Byte> getSize(@Nullable CompoundTag nbt) {
        if (nbt != null && nbt.contains(SIZE_KEY)) {
            return Optional.of(nbt.getByte(SIZE_KEY));
        } else {
            return Optional.empty();
        }
    }

    public static byte getSizeOrDefault(@Nullable CompoundTag nbt) {
        return CapabilitiesPouchCompanion.getSize(nbt).orElse(ItemPouchCompanion.DEFAULT_SLOT_COUNT);
    }

    @SuppressWarnings("unchecked")
    @Override
    @Implements(ICapabilityProvider.class)
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (LazyOptional<T>) this.lazy;
        } else if (cap == CapabilityTemporalIndex.TEMPORAL_INDEX_CAPABILITY) {
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
    @Extends(ItemStackHandler.class)
    protected void onContentsChanged(int slot) {
        this.save();
    }

    private void initFromNbt(@Nullable CompoundTag nbt) {
        if (nbt == null) {
            return;
        }

        CompoundTag stackData = this.stack.getOrCreateTag();

        if (nbt.contains(STORAGE_KEY)) {
            stackData.put(STORAGE_KEY, nbt.get(STORAGE_KEY));
        }

        byte activateSlotIndex = nbt.contains(ACTIVATE_KEY) ? nbt.getByte(ACTIVATE_KEY) : (byte) 0;
        stackData.putByte(ACTIVATE_KEY, activateSlotIndex);
    }

    private void load() {
        CompoundTag tag = this.stack.getOrCreateTag();
        if (tag.contains(STORAGE_KEY)) {
            this.deserializeNBT(tag.getCompound(STORAGE_KEY));
        }
        if (tag.contains(ACTIVATE_KEY)) {
            this.activationIndex = tag.getByte(ACTIVATE_KEY);
        }
    }

    private void save() {
        CompoundTag tag = this.stack.getOrCreateTag();
        tag.put(STORAGE_KEY, this.serializeNBT());
        tag.putByte(ACTIVATE_KEY, (byte) this.activationIndex);
    }

    @Override
    @Implements(ITemporalIndex.class)
    public int getIndex() {
        return this.activationIndex;
    }

    @Override
    public void setIndex(int index) {
        this.activationIndex = index;
        this.save();
    }
}
