package net.qiyanamark.companionpouch.item;


import iskallia.vault.item.CompanionItem; // Vault Hunters item type to restrict


import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;


import org.jetbrains.annotations.Nullable;


import net.qiyanamark.companionpouch.menu.CompanionPouchMenu;

public class CompanionPouchItem extends Item {
    public static final int SLOT_COUNT = 3;
    private static final String INV_KEY = "Inventory";


    public CompanionPouchItem(Properties props) { super(props); }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            NetworkHooks.openGui(sp,
                    new SimpleMenuProvider((id, inv, ply) -> new CompanionPouchMenu(id, inv, stack),
                            Component.nullToEmpty("container.companionpouch.companion_pouch")),
                    buf -> buf.writeBoolean(hand == InteractionHand.MAIN_HAND));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }


    // Capability provider to persist a 3-slot item handler inside the pouch item
    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new PouchInvProvider(stack);
    }


    public static class PouchInvProvider implements ICapabilityProvider {
        private final ItemStack stack;
        private final ItemStackHandler handler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            public boolean isItemValid(int slot, ItemStack insert) {
                if (insert.getItem() instanceof CompanionPouchItem) return false; // no nesting
                return insert.getItem() instanceof CompanionItem; // only companions
            }


            @Override
            protected void onContentsChanged(int slot) { save(); }
        };
        private final LazyOptional<IItemHandler> lazy = LazyOptional.of(() -> handler);


        public PouchInvProvider(ItemStack stack) {
            this.stack = stack;
            load();
        }


        private void load() {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains(INV_KEY)) handler.deserializeNBT(tag.getCompound(INV_KEY));
        }
        private void save() {
            CompoundTag tag = stack.getOrCreateTag();
            tag.put(INV_KEY, handler.serializeNBT());
        }


        @Override
        public <T> LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, net.minecraft.core.Direction side) {
            if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return lazy.cast();
            return LazyOptional.empty();
        }
    }


    public static ItemStackHandler getHandler(ItemStack pouch) {
        ICapabilityProvider provider = pouch.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
        if (provider instanceof ItemStackHandler ish) return ish;
// Fallback: reconstruct through provider
        PouchInvProvider p = new PouchInvProvider(pouch);
        return (ItemStackHandler) p.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElseThrow();
    }
}