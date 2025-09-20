package net.qiyanamark.companionpouch.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Modifiers;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.event.event.VaultJoinEvent;
import iskallia.vault.item.CompanionItem;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.qiyanamark.companionpouch.helper.HelperCompanions;

@Mixin(value = iskallia.vault.event.PlayerEvents.class, remap = false)
public class MixinOnVaultJoinApplyCompanion {
    /**
     * @reason
     * The logic is completely replaced to use a helper to find ALL valid
     * equipped companions. An overrite is necessary because the goal is
     * specifically to replace the upstream behaviour.
     * @author MaxTheMooshroom (Maxine Zick <maxine@pnk.dev>)
     */
    @Overwrite(remap = false)
    public static void onVaultJoinApplyCompanion(VaultJoinEvent event) {
        Vault vault = event.getVault();
        ServerPlayer player = event.getPlayer();
        HelperCompanions.getCompanions(player).forEach(stack -> {
        // CompanionItem.getCompanion(player).ifPresent((stack) -> {
            String petName = CompanionItem.getPetName(stack);
            boolean onCd = CompanionItem.isOnCooldown(stack);
            CompanionItem.setActive(stack, !onCd);

            if (onCd) {
                player.sendMessage(new TextComponent("<" + petName + "> I'm resting and cannot help you"), player.getUUID());
            } else if (VaultUtils.allowTemporal(vault)) {
                player.sendMessage(new TextComponent("<" + petName + "> My relics don’t seem to work in this vault"), player.getUUID());
            } else if (VaultUtils.isSpecialVault(vault)) {
                player.sendMessage(new TextComponent("<" + petName + "> I am too weak to modify this vault"), player.getUUID());
            } else {
                if (CompanionItem.getCompanionHearts(stack) > 0 && CompanionItem.isActive(stack)) {
                    CompanionItem.getAllRelics(stack).values().forEach((list) -> {
                        (list.getSecond()).forEach((id) -> {
                            VaultModifier<?> modifier = VaultModifierRegistry.get(id);
                            if (modifier != null) {
                                ((Modifiers)vault.get(Vault.MODIFIERS)).addModifier(modifier, 1, true, JavaRandom.ofNanoTime());
                            }
                            
                        });
                    });
                }
            }
        });
    }
}
