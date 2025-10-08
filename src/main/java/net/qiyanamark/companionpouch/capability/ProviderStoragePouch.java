package net.qiyanamark.companionpouch.capability;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.util.Structs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.CompanionItem;

import net.qiyanamark.companionpouch.catalog.CatalogCapability;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import net.qiyanamark.companionpouch.util.annotations.Extends;
import net.qiyanamark.companionpouch.util.annotations.Implements;

public class ProviderStoragePouch implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final String REL_ID = "companion_pouch_capability";

    public static final String STORAGE_KEY = "storage";
    public static final String ACTIVATE_KEY = "activate";

    HandlerStoragePouch inner;
    LazyOptional<IDataPouchCompanion> capability;

    public ProviderStoragePouch(@NotNull ItemStack pouchStack) {
        this.capability = LazyOptional.of(() -> {
            this.inner = new HandlerStoragePouch(pouchStack);
            return this.inner;
        });
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CatalogCapability.COMPANION_POUCH_CAPABILITY || cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.capability.cast();
        } else {
            return LazyOptional.empty();
        }
    }

    @Override
    @Implements(INBTSerializable.class)
    public CompoundTag serializeNBT() {
        return this.inner != null ? this.inner.serializeNBT() : new CompoundTag();
    }

    @Override
    @Implements(INBTSerializable.class)
    public void deserializeNBT(CompoundTag nbt) {
        if (this.inner != null && nbt != null) {
            this.inner.deserializeNBT(nbt);
        }
    }

    public static class HandlerStoragePouch extends ItemStackHandler implements IDataPouchCompanion {
        private final ItemStack pouchStack;
        private byte activationIndex = 0;

        public HandlerStoragePouch(ItemStack pouchStack) {
            super(ItemPouchCompanion.DEFAULT_SLOT_COUNT);
            this.pouchStack = pouchStack;
            CompoundTag pouchTag = this.pouchStack.getOrCreateTag();

            this.deserializeNBT(pouchTag);
        }

        @Override
        @Extends(ItemStackHandler.class)
        protected void onContentsChanged(int index) {
            this.pouchTag().put(STORAGE_KEY, super.serializeNBT());
        }

        @Override
        @Implements(IDataPouchCompanion.class)
        public byte getActivationIndex() {
            return this.activationIndex;
        }

        @Override
        @Implements(IDataPouchCompanion.class)
        public void setActivationIndex(byte index) {
            if (index >= 0 && index < this.getSlots()) {
                this.activationIndex = index;
                this.saveChanges();
            }
        }

        @Override
        @Implements(IDataPouchCompanion.class)
        public boolean tryActivateTemporal(byte index, ServerPlayer sPlayer) {
            if (index < 0) {
                index = this.activationIndex;
            }

            ItemStack companion = this.getStackInSlot(index);
            Optional<Vault> vaultMaybe = VaultUtils.getVault(sPlayer.level);

            if (HelperCompanions.companionCanUseTemporalInVault(companion, vaultMaybe)) {
                CompanionItem.activateTemporalModifier(sPlayer, companion, vaultMaybe.orElseThrow());
                return true;
            }

            return false;
        }

        @Override
        @Implements(value = IItemHandler.class, introducedBy = ItemStackHandler.class)
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(ModItems.COMPANION);
        }

        @Override
        @Implements(value = INBTSerializable.class, introducedBy = ItemStackHandler.class)
        public CompoundTag serializeNBT() {
            CompoundTag pouchTag = this.pouchTag();

            pouchTag.put(STORAGE_KEY, super.serializeNBT());
            pouchTag.putByte(ACTIVATE_KEY, this.activationIndex);

//            Structs.InstanceSide.get().ifClient(() -> {
//                LocalPlayer lPlayer = ModCompanionPouch.getClientPlayer();
//                lPlayer.sendMessage(new TextComponent("serializing pouch; activationIndex: "), lPlayer.getUUID());
//            });

            return pouchTag.copy();
        }

        @Override
        @Implements(value = INBTSerializable.class, introducedBy = ItemStackHandler.class)
        public void deserializeNBT(CompoundTag nbt) {
            CompoundTag pouchTag = this.pouchTag();

            if (nbt.contains(ACTIVATE_KEY)) {
                this.activationIndex = nbt.getByte(ACTIVATE_KEY);
                pouchTag.putByte(ACTIVATE_KEY, this.activationIndex);
            }

            if (nbt.contains(STORAGE_KEY)) {
                CompoundTag storageTag = nbt.getCompound(STORAGE_KEY);
                super.deserializeNBT(storageTag);
                pouchTag.put(STORAGE_KEY, storageTag);
            }
        }

        public CompoundTag saveChanges() {
            return this.serializeNBT();
        }

        private CompoundTag pouchTag() {
            return this.pouchStack.getOrCreateTag();
        }
    }
}
