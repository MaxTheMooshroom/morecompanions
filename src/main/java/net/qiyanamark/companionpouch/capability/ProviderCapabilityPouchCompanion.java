package net.qiyanamark.companionpouch.capability;

import java.util.Optional;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.CompanionItem;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.common.capabilities.Capability;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import net.qiyanamark.companionpouch.util.annotations.Extends;
import net.qiyanamark.companionpouch.util.annotations.Implements;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
import static net.qiyanamark.companionpouch.capability.CapabilityPouchCompanion.COMPANION_POUCH_CAPABILITY;

public class ProviderCapabilityPouchCompanion implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static final String STORAGE_KEY = "storage";
    public static final String ACTIVATE_KEY = "activate";

    public ProviderCapabilityPouchCompanion(ItemStack stack, @Nullable CompoundTag nbt) {
        this.storageHandler = LazyOptional.of(() -> {
            StorageHandler handler = new StorageHandler(stack);
            if (handler.pouchStack.getOrCreateTag().contains(ACTIVATE_KEY)) {
                handler.activationIndex = handler.pouchStack.getOrCreateTag().getByte(ACTIVATE_KEY);
            }
            return handler;
        });

        if (nbt != null) {
            this.deserializeNBT(nbt);
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ITEM_HANDLER_CAPABILITY || cap == COMPANION_POUCH_CAPABILITY) {
            return this.storageHandler.cast();
        } else {
            return LazyOptional.empty();
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        StorageHandler handler = this.storageHandler.orElseThrow(IllegalStateException::new);
        CompoundTag result = handler.serializeNBT();
        result.putByte(ACTIVATE_KEY, handler.activationIndex);
        return result;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        StorageHandler handler = this.storageHandler.orElseThrow(IllegalStateException::new);
        handler.deserializeNBT(nbt);

        if (nbt.contains(ACTIVATE_KEY)) {
            handler.activationIndex = nbt.getByte(ACTIVATE_KEY);
            handler.pouchStack.getOrCreateTag().putByte(ACTIVATE_KEY, handler.activationIndex);
        }
    }

    private final LazyOptional<StorageHandler> storageHandler;

    private static class StorageHandler extends ItemStackHandler implements IDataPouchCompanion {
        private final ItemStack pouchStack;
        private byte activationIndex;

        public StorageHandler(ItemStack pouchStack) {
            super(ItemPouchCompanion.DEFAULT_SLOT_COUNT);
            this.pouchStack = pouchStack;

            CompoundTag pouchTag = this.pouchStack.getOrCreateTag();
            if (pouchTag.contains(STORAGE_KEY)) {
                this.deserializeNBT(pouchTag.getCompound(STORAGE_KEY));
            }

            if (pouchTag.contains(ACTIVATE_KEY)) {
                this.activationIndex = pouchTag.getByte(ACTIVATE_KEY);
            } else {
                this.activationIndex = 0;
                pouchTag.putByte(ACTIVATE_KEY, this.activationIndex);
            }
        }

        @Override
        @Extends(ItemStackHandler.class)
        protected void onContentsChanged(int index) {
            this.pouchStack.getOrCreateTag().put(STORAGE_KEY, this.serializeNBT());
        }

        @Override
        @Implements(IDataPouchCompanion.class)
        public byte getActivationIndex() {
            return this.activationIndex;
        }

        @Override
        @Implements(IDataPouchCompanion.class)
        public void setActivationIndex(byte index) {
            if (0 <= index && index < this.getSlots()) {
                this.activationIndex = index;
                this.pouchStack.getOrCreateTag().putByte(ACTIVATE_KEY, this.activationIndex);
                ModCompanionPouch.messageLocalDebug("Set activation index to " + this.pouchStack.getOrCreateTag().getByte(ACTIVATE_KEY));
            }
        }

        @Override
        public boolean tryActivateTemporal(byte index, ServerPlayer sPlayer) {
            if (index < 0) {
                index = this.activationIndex;
                sPlayer.sendMessage(new TextComponent("Tried to activate bound companion number " + index + "'s temporal"), sPlayer.getUUID());
            } else {
                sPlayer.sendMessage(new TextComponent("Tried to activate temporal of companion number " + index), sPlayer.getUUID());
            }
            ItemStack companion = this.getStackInSlot(index);
            Optional<Vault> vaultMaybe = VaultUtils.getVault(sPlayer.level);

            if (HelperCompanions.companionCanUseTemporalInVault(companion, vaultMaybe)) {
                CompanionItem.activateTemporalModifier(sPlayer, companion, vaultMaybe.orElseThrow());
                return true;
            } else {
                return false;
            }
        }

        @Override
        @Implements(value = IItemHandler.class, introducedBy = ItemStackHandler.class)
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return stack.is(ModItems.COMPANION);
        }
    }
}
