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
import net.qiyanamark.companionpouch.helper.annotations.Extends;
import net.qiyanamark.companionpouch.helper.annotations.Implements;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;

public class CapabilitiesPouchCompanion extends ItemStackHandler implements ICapabilityProvider {
    private static final String SIZE_KEY = "slots";
    private static final String STORAGE_KEY = "contents";

    private final ItemStack stack;
    private final LazyOptional<IItemHandler> lazy;

    public CapabilitiesPouchCompanion(ItemStack stack, @Nullable CompoundTag nbt) {
        super(getSize(nbt).orElse(ItemPouchCompanion.DEFAULT_SLOT_COUNT));

        this.stack = stack;
        this.lazy = LazyOptional.of(() -> this);

        if (nbt != null && nbt.contains(STORAGE_KEY)) {
            this.stack.getOrCreateTag().put(STORAGE_KEY, nbt.get(STORAGE_KEY));
        }

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

    @Override
    @Implements(ICapabilityProvider.class)
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, this.lazy);
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

    private void load() {
        CompoundTag tag = this.stack.getOrCreateTag();
        if (tag.contains(STORAGE_KEY)) {
            this.deserializeNBT(tag.getCompound(STORAGE_KEY));
        }
    }

    private void save() {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(STORAGE_KEY, this.serializeNBT());
    }
}
