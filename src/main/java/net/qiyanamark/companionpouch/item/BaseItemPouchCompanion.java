package net.qiyanamark.companionpouch.item;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.network.NetworkHooks;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;

import iskallia.vault.item.CompanionItem;

import static iskallia.vault.init.ModItems.VAULT_MOD_GROUP;

import net.qiyanamark.companionpouch.container.ContainerPouchCompanion;
import net.qiyanamark.companionpouch.helpers.ProviderCapabilityInventoryPouchCompanion;

/**
 * Included for future extensions (adding different companion pouches)
 */
public abstract class BaseItemPouchCompanion extends CompanionItem {
    protected final ResourceLocation itemResource;
    protected final int slotCount;

    public BaseItemPouchCompanion(ResourceLocation id, int slotCount) {
        super(id, new Properties().stacksTo(1).tab(VAULT_MOD_GROUP));
        this.itemResource = id;
        this.slotCount = slotCount;
    }

    public ResourceLocation id() {
        return this.itemResource;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer s_player) {
            SimpleMenuProvider menu_provider = new SimpleMenuProvider(
                (id, inv, ply) -> new ContainerPouchCompanion(id, inv, stack),
                new TextComponent("container.companionpouch.companion_pouch")
            );

            NetworkHooks.openGui(s_player, menu_provider);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ProviderCapabilityInventoryPouchCompanion(stack, this.slotCount);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return CompanionItem.getCompanion(slotContext.entity())
            .map(slotResult -> slotResult.isEmpty() && slotResult == stack)
            .orElse(true);
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            ResourceLocation dim_rel = player.level.dimension().location();
            return !dim_rel.getNamespace().equals("the_vault");
        }

        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> map = LinkedHashMultimap.create();
        // TODO
        return map;
    }
}