package net.qiyanamark.companionpouch.helper;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.CompanionItem;

public class HelperCompanions {
    public static boolean companionCanUseTemporalInVault(ItemStack companionStack, @NotNull Optional<Vault> vault) {
        if (companionStack == null || vault.isEmpty()) {
            return false;
        }

        boolean result = CompanionItem.getCompanionHearts(companionStack) > 0;
        result &= !CompanionItem.hasUsedTemporalIn(companionStack, vault.get().get(Vault.ID));
        result &= CompanionItem.getCurrentCooldown(companionStack) == 0;
        return result;
    }

    public static boolean companionCanUseTemporalInVault(ItemStack companionStack, @NotNull Vault vault) {
        if (companionStack == null) {
            return false;
        }

        boolean result = CompanionItem.getCompanionHearts(companionStack) > 0;
        result &= !CompanionItem.hasUsedTemporalIn(companionStack, vault.get(Vault.ID));
        result &= CompanionItem.getCurrentCooldown(companionStack) == 0;
        return result;
    }
}
