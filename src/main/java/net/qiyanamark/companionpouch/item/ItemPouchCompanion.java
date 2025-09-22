package net.qiyanamark.companionpouch.item;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import iskallia.vault.item.CompanionItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
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

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.capabilities.CapabilitiesPouchCompanion;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.helper.annotations.Extends;
import net.qiyanamark.companionpouch.helper.annotations.Implements;
import net.qiyanamark.companionpouch.menu.container.MenuContainerPouchCompanion;

public class ItemPouchCompanion extends Item implements ICurioItem {
    public static final String REL_STRING = "pouch_companion";
    public static final byte DEFAULT_SLOT_COUNT = 3;

    protected ItemPouchCompanion(ResourceLocation rel) {
        super(new Properties().stacksTo(1).tab(VAULT_MOD_GROUP));
        this.setRegistryName(rel);
    }

    public ItemPouchCompanion() {
        this(ModCompanionPouch.rel(REL_STRING));
    }

    public static List<ItemStack> getContents(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemPouchCompanion)) {
            return Collections.emptyList();
        }

        return stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve()
            .map(handler -> IntStream.range(0, handler.getSlots())
                .mapToObj(i -> handler.getStackInSlot(i))
                .collect(Collectors.toList()))
            .orElseGet(Collections::emptyList);
    }

    @Override
    @Extends(Item.class)
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player instanceof ServerPlayer sPlayer) {
            // TODO replace this placeholder
            String containerI18n = "screen.companionpouch.pouch_companion";

            byte slotCount = CapabilitiesPouchCompanion.getSizeOrDefault(stack.getOrCreateTag());
            NetworkHooks.openGui(sPlayer, this.getGui(hand, containerI18n), buf -> {
                buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
                buf.writeByte(slotCount);
            });
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    @Implements(value = IForgeItem.class, introducedBy = Item.class)
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new CapabilitiesPouchCompanion(stack, nbt);
    }

    public static ItemStack getItemFromCuriosHeadSlot(Player player, Predicate<ItemStack> stackMatcher) {
        return CuriosApi.getCuriosHelper().getCuriosHandler(player).resolve().flatMap((h) -> {
            return h.getStacksHandler(SlotTypePreset.HEAD.getIdentifier()).flatMap((stackHandler) -> {
                IDynamicStackHandler stacks = stackHandler.getStacks();

                for(int slot = 0; slot < stacks.getSlots(); ++slot) {
                    ItemStack stackInSlot = stacks.getStackInSlot(slot);
                    if (stackMatcher.test(stackInSlot)) {
                        return Optional.of(stackInSlot);
                    }
                }

                return Optional.empty();
            });
        }).orElse(ItemStack.EMPTY);
    }

    @Override
    @Implements(ICurioItem.class)
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotContext.identifier().equals("pouch_companion") &&
            HelperCompanions.getCompanions(slotContext.entity()).isEmpty();
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

    private SimpleMenuProvider getGui(InteractionHand hand, String containerI18n) {
        return new SimpleMenuProvider(
                (id, inv, player) -> {
                    ItemStack pouchStack = player.getItemInHand(hand);
                    byte slotCount = CapabilitiesPouchCompanion.getSizeOrDefault(pouchStack.getOrCreateTag());
                    return new MenuContainerPouchCompanion(id, inv, pouchStack, slotCount);
                },
                new TranslatableComponent(containerI18n)
            );
    }
}