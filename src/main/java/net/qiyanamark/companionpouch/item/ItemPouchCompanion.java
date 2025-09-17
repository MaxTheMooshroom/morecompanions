package net.qiyanamark.companionpouch.item;

import javax.annotation.Nullable;

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
import net.minecraftforge.network.NetworkHooks;

import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import iskallia.vault.item.CompanionItem;

import static iskallia.vault.init.ModItems.VAULT_MOD_GROUP;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.capabilities.CapabilitiesPouchCompanion;
import net.qiyanamark.companionpouch.helper.annotations.Extends;
import net.qiyanamark.companionpouch.helper.annotations.Implements;
import net.qiyanamark.companionpouch.menu.container.MenuContainerPouchCompanion;

public class ItemPouchCompanion extends Item implements ICurioItem {
    public static final String REL_STRING = "pouch_companion";

    protected final String containerI18n;
    protected final int slotCount;

    protected ItemPouchCompanion(ResourceLocation rel, int slotCount, String containerI18n) {
        super(new Properties().stacksTo(1).tab(VAULT_MOD_GROUP));
        this.setRegistryName(rel);

        this.slotCount = slotCount;
        this.containerI18n = containerI18n;
    }

    public ItemPouchCompanion(String subtype, int slotCount) {
        this(ModCompanionPouch.rel(REL_STRING), slotCount, "container.companionpouch." + REL_STRING + "." + subtype);
    }

    @Override
    @Extends(Item.class)
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player instanceof ServerPlayer sPlayer) {
            NetworkHooks.openGui(sPlayer, this.getGui(hand), buf -> buf.writeBoolean(hand == InteractionHand.MAIN_HAND));
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    @Implements(value = IForgeItem.class, introducedBy = Item.class)
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new CapabilitiesPouchCompanion(stack, nbt, this.slotCount);
    }

    // note: slotResult.isEmpty() does not need to be checked because of the MixinCompanionItem
    // changing the CompanionItem.getCompanion logic. The mapped function does not run if there
    // is no companion equipped because the result is Optional.empty(), rather than the old
    // behaviour of searching for the specific "head" curio slot and returning an Optional of
    // whether it was found.
    //
    // note: dimRel.getNamespace() is not a bug or typo, it is consistent with behaviour defined
    // by the vault mod.
    @Override
    @Implements(ICurioItem.class)
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return CompanionItem.getCompanion(slotContext.entity())
            .map(slotResult -> false)
            .orElseGet(() -> {
                ResourceLocation dimRel = slotContext.entity().level.dimension().location();
                String dimMod = dimRel.getNamespace();
                return !dimMod.equals("the_vault");
            });
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

    private SimpleMenuProvider getGui(InteractionHand hand) {
        return new SimpleMenuProvider(
                (id, inv, player) -> new MenuContainerPouchCompanion(id, inv, player.getItemInHand(hand)),
                new TranslatableComponent(this.containerI18n)
            );
    }
}