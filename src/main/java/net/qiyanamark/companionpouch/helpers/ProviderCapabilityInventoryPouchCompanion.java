package net.qiyanamark.companionpouch.helpers;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import iskallia.vault.item.CompanionItem;

import net.qiyanamark.companionpouch.item.BaseItemPouchCompanion;

public class ProviderCapabilityInventoryPouchCompanion implements ICapabilityProvider {
    private static final String STORAGE_KEY = "held_companions";

    private final ItemStack stack;
    private final ItemStackHandler handler;
    private final LazyOptional<IItemHandler> lazy;

    public ProviderCapabilityInventoryPouchCompanion(ItemStack stack, int companionCount) {
        this.stack = stack;
        this.handler = new ItemStackHandler(companionCount) {
            @Override
            public boolean isItemValid(int slot, ItemStack insert) {
                Item item = insert.getItem();
                
                if (item instanceof BaseItemPouchCompanion) {
                    return false;
                }

                return item instanceof CompanionItem;
            }


            @Override
            protected void onContentsChanged(int slot) {
                save();
            }
        };
        this.lazy = LazyOptional.of(() -> this.handler);

        load();
    }

    // this.handler = this.tags[STORAGE_KEY]
    private void load() {
        CompoundTag tag = this.stack.getOrCreateTag();
        if (tag.contains(STORAGE_KEY)) {
            this.handler.deserializeNBT(tag.getCompound(STORAGE_KEY));
        }
    }
    
    // this.tags[STORAGE_KEY] = this.handler
    private void save() {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(STORAGE_KEY, handler.serializeNBT());
    }

    @Override
    public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, net.minecraft.core.Direction side) {
        return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? this.lazy.cast() : LazyOptional.empty();
    }
}
