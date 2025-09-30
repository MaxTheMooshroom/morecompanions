package net.qiyanamark.companionpouch.item;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeItem;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.network.NetworkHooks;

import static iskallia.vault.init.ModItems.VAULT_MOD_GROUP;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.capabilities.IDataPouchCompanion;
import net.qiyanamark.companionpouch.util.Structs;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import net.qiyanamark.companionpouch.capabilities.CapabilityDataPouchCompanion;
import net.qiyanamark.companionpouch.capabilities.ProviderCapabilityPouchCompanion;
import net.qiyanamark.companionpouch.menu.container.MenuInterfacePouchCompanion;
import net.qiyanamark.companionpouch.menu.container.MenuInventoryPouchCompanion;
import net.qiyanamark.companionpouch.util.annotations.Extends;
import net.qiyanamark.companionpouch.util.annotations.Implements;

public class ItemPouchCompanion extends Item implements ICurioItem {
    public static final String REL_PATH = "pouch_companion";
    public static final int DEFAULT_SLOT_COUNT = 3;

    public ItemPouchCompanion() {
        super(new Properties().stacksTo(1).tab(VAULT_MOD_GROUP));
    }

    public static List<ItemStack> getContents(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemPouchCompanion)) {
            return Collections.emptyList();
        }

        return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve()
            .map(handler -> IntStream.range(0, handler.getSlots())
                .mapToObj(handler::getStackInSlot)
                .collect(Collectors.toList()))
            .orElseGet(Collections::emptyList);
    }

    @Override
    @Extends(Item.class)
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int slotCount = stack.getCapability(CapabilityDataPouchCompanion.COMPANION_POUCH_CAPABILITY)
            .map(IDataPouchCompanion::getSize)
            .orElse(ItemPouchCompanion.DEFAULT_SLOT_COUNT);

        if (player instanceof ServerPlayer sPlayer) {
            if (!sPlayer.isCrouching()) {
                SimpleMenuProvider provider = MenuInventoryPouchCompanion.getProvider(hand);
                NetworkHooks.openGui(sPlayer, provider, buf -> {
                    buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
                    buf.writeByte(slotCount);
                });
                
            } else if (ModCompanionPouch.DEBUG) {
                SimpleMenuProvider provider = MenuInterfacePouchCompanion.getProvider(stack, Structs.InstanceSide.from(sPlayer));
                NetworkHooks.openGui(sPlayer, provider, buf -> buf.writeByte(slotCount));
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    @Implements(value = IForgeItem.class, introducedBy = Item.class)
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ProviderCapabilityPouchCompanion(stack, nbt);
    }

    // note: dimRel.getNamespace() is not a bug or typo, it is consistent with behaviour defined
    // by the vault mod.
    @Override
    @Implements(ICurioItem.class)
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return !slotContext.entity().level.dimension().location().getNamespace().equals("the_vault");
    }

    // note: dimRel.getNamespace() is not a bug or typo, it is consistent with behaviour defined
    // by the vault mod.
    @Override
    @Implements(ICurioItem.class)
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            ResourceLocation dimRel = player.level.dimension().location();
            return !dimRel.getNamespace().equals("the_vault");
        }

        return true;
    }
}