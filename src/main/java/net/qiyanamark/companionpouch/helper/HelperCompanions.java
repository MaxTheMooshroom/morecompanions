package net.qiyanamark.companionpouch.helper;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.qiyanamark.companionpouch.capability.IDataPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogCapability;
import net.qiyanamark.companionpouch.util.Structs;
import org.checkerframework.checker.nullness.qual.NonNull;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.item.CompanionItem;

public class HelperCompanions {
    public static boolean forEachCompanion(Player player, Consumer<ItemStack> transformer) {
        Optional<ItemStack> pouchStackMaybe = Structs.LocationPouch.CURIO.getFromEntity(player);
        Optional<ItemStack> companionStackMaybe = CompanionItem.getCompanion(player);

        pouchStackMaybe.ifPresentOrElse(
                pouchStack -> {
                    IDataPouchCompanion handler = pouchStack.getCapability(CatalogCapability.COMPANION_POUCH_CAPABILITY).orElseThrow(IllegalStateException::new);

                    IntStream.range(0, handler.getSlots())
                            .mapToObj(handler::getStackInSlot)
                            .filter(companionStack -> !companionStack.isEmpty())
                            .forEach(transformer);

                    handler.save();
                },
                () -> companionStackMaybe.ifPresent(transformer)
        );

        return pouchStackMaybe.isPresent() || companionStackMaybe.isPresent();
    }

    public static boolean companionCanUseTemporalInVault(ItemStack companionStack, @NonNull Optional<Vault> vault) {
        if (companionStack == null || vault.isEmpty()) {
            return false;
        }

        return !CompanionItem.hasUsedTemporalIn(companionStack, vault.get().get(Vault.ID)) && CompanionItem.getCurrentCooldown(companionStack) == 0;
    }
}
