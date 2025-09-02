package net.qiyanamark.companionpouch.item;

import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.util.ICuriosHelper;
import iskallia.vault.item.BasicItem;
import static iskallia.vault.init.ModItems.VAULT_MOD_GROUP;

public class CompanionPouchItem extends BasicItem implements ICurioItem {
    public static final String ITEM_ID = "companion_pouch";

    public CompanionPouchItem(ResourceLocation id) {
        super(id, new Properties().stacksTo(1).tab(VAULT_MOD_GROUP));
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity player = slotContext.entity();
        if (player.getLevel().isClientSide || !stack.hasTag()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("StoredCurios") || !tag.contains("id")) return;

        LazyOptional<ICuriosItemHandler> optHandler = CuriosApi.getCuriosHelper().getCuriosHandler(player);
        if (!optHandler.isPresent()) return;

        ICuriosItemHandler handler = optHandler.resolve().get();
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        Optional<SlotResult> slot = CuriosApi.getCuriosHelper().findCurio(slotContext.entity(), ITEM_ID, 0);
        return slot.map(slotResult -> slotResult.stack().isEmpty()).orElse(true);
    }
    
    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            String playerDimModId = player.level.dimension().location().getNamespace();
            if (playerDimModId.equals("the_vault")) {
                return false;
            }

            ICuriosHelper cHelper = CuriosApi.getCuriosHelper();
            if (cHelper.findCurios(player, "companion").size() > 1) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> map = LinkedHashMultimap.create();
        // TODO
        return map;
    }
}