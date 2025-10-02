package net.qiyanamark.companionpouch.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import org.checkerframework.checker.nullness.qual.NonNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.CompanionItem;

import net.qiyanamark.companionpouch.catalog.CatalogItem;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;

public class HelperCompanions {
    public static List<ItemStack> getCompanions(LivingEntity entity) {
        if (entity.isSpectator()) {
            return Collections.emptyList();
        }

        return CuriosApi.getCuriosHelper()
            .findFirstCurio(entity, stack -> stack.getItem() == ModItems.COMPANION || stack.getItem() == CatalogItem.COMPANION_POUCH.get())
            .map(slot -> {
                ItemStack stack = slot.stack();
                if (stack.getItem() instanceof ItemPouchCompanion) {
                    return ItemPouchCompanion.getContents(stack);
                } else {
                    return new ArrayList<>(List.of(stack));
                }
            })
            .orElseGet(Collections::emptyList);
    }

    public static boolean companionCanUseTemporalInVault(ItemStack companionStack, @NonNull Optional<Vault> vault) {
        if (companionStack == null || vault.isEmpty()) {
            return false;
        }

        return !CompanionItem.hasUsedTemporalIn(companionStack, vault.get().get(Vault.ID)) && CompanionItem.getCurrentCooldown(companionStack) == 0;
    }
}
