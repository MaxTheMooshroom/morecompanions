package net.qiyanamark.companionpouch.item;

import net.qiyanamark.companionpouch.capability.ProviderStoragePouch;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.network.NetworkHooks;

import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import net.qiyanamark.companionpouch.capability.IDataPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogCapability;
import net.qiyanamark.companionpouch.menu.container.MenuInterfacePouchCompanion;
import net.qiyanamark.companionpouch.menu.container.MenuInventoryPouchCompanion;
import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.util.annotations.Extends;
import net.qiyanamark.companionpouch.util.annotations.Implements;
import net.qiyanamark.companionpouch.util.Structs;

import iskallia.vault.world.data.VaultPlayerStats;

import static iskallia.vault.init.ModItems.VAULT_MOD_GROUP;

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
        ItemStack pouchStack = player.getItemInHand(hand);

        boolean success = false;
        if (player instanceof ServerPlayer sPlayer) {
            int slotCount = pouchStack.getCapability(CatalogCapability.COMPANION_POUCH_CAPABILITY)
                    .map(IDataPouchCompanion::getSlots)
                    .orElse(ItemPouchCompanion.DEFAULT_SLOT_COUNT);
            Structs.LocationPouch location = Structs.LocationPouch.fromHand(hand);

            if (!sPlayer.isCrouching()) {
                SimpleMenuProvider provider = MenuInventoryPouchCompanion.getProvider(pouchStack, slotCount);
                NetworkHooks.openGui(sPlayer, provider, buf -> {
                    location.writeByte(buf);
                    buf.writeByte(slotCount);
                });
                success = true;
                
            } else if (ModCompanionPouch.DEBUG) {
                SimpleMenuProvider provider = MenuInterfacePouchCompanion.getProvider(pouchStack);
                NetworkHooks.openGui(sPlayer, provider, buf -> buf.writeByte(slotCount));
                success = true;
            }
        }

        return success ? InteractionResultHolder.sidedSuccess(pouchStack, level.isClientSide) : InteractionResultHolder.pass(pouchStack);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ProviderStoragePouch(stack);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        return Optional.ofNullable(ctx.getPlayer())
                .filter(Player::isCrouching)
                .map(player -> {
                    // TODO
                    return InteractionResult.sidedSuccess(player.level.isClientSide);
                })
                .orElse(InteractionResult.PASS);
    }

    // note: dimension().location().getNamespace() is not a bug or typo, it is consistent
    // with behaviour defined by the vault mod.
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