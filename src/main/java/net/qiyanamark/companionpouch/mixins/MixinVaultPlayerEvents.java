package net.qiyanamark.companionpouch.mixins;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Inject;

import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.event.PlayerEvents;
import iskallia.vault.event.event.VaultJoinEvent;
import iskallia.vault.event.event.VaultLeaveEvent;
import iskallia.vault.item.CompanionItem;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.expertise.type.CompanionCooldownExpertise;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.world.data.PlayerExpertisesData;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.helper.HelperCompanions;

@Mixin(value = PlayerEvents.class, remap = false)
public class MixinVaultPlayerEvents {
    @Inject(
        at = @At("HEAD"),
        method = "onVaultJoinApplyCompanion(Liskallia/vault/event/event/VaultJoinEvent;)V",
        cancellable = true
    )
    private static void onVaultJoinApplyCompanion(VaultJoinEvent event, CallbackInfo ci) {
        ServerPlayer player = event.getPlayer();
        Vault vault = event.getVault();
        Set<ResourceLocation> temporals = new HashSet<>();
        Set<ResourceLocation> modifiers = new HashSet<>();

        Set<ResourceLocation> perCompanionModifiers = new HashSet<>();

        int[] curseStack = {0};

        HelperCompanions.getCompanions(player).forEach(stack -> {
            String companionName = CompanionItem.getPetName(stack);
            boolean onCd = CompanionItem.isOnCooldown(stack);
            CompanionItem.setActive(stack, !onCd);

            CompanionItem.getTemporalModifier(stack).ifPresent(rel -> {
                if (temporals.contains(rel)) {
                    curseStack[0]++;
                } else {
                    temporals.add(rel);
                }
            });

            if (onCd) {
                player.sendMessage(new TextComponent("<" + companionName + "> I'm resting and cannot help you"), player.getUUID());
            } else if (VaultUtils.isCakeVault(vault)) {
                player.sendMessage(new TextComponent("<" + companionName + "> My relics don’t seem to work in this vault"), player.getUUID());
            } else if (VaultUtils.isSpecialVault(vault)) {
                player.sendMessage(new TextComponent("<" + companionName + "> I am too weak to modify this vault"), player.getUUID());
            } else {
                if (CompanionItem.getCompanionHearts(stack) > 0 && CompanionItem.isActive(stack)) {
                    perCompanionModifiers.clear();
                    player.sendMessage(new TranslatableComponent("companion." + ModCompanionPouch.MOD_ID + ".relic.applied", companionName), player.getUUID());

                    CompanionItem.getAllRelics(stack).values().stream()
                        .map(pair -> pair.getSecond())
                        .forEach(list -> {
                            list.forEach(id -> {
                                VaultModifier<?> modifier = VaultModifierRegistry.get(id);
                                if (modifier != null) {
                                    vault.get(Vault.MODIFIERS).addModifier(modifier, 1, true, JavaRandom.ofNanoTime());
                                }
                                perCompanionModifiers.add(id);
                            });
                        });

                    perCompanionModifiers.stream()
                            .filter(rel -> !rel.getPath().equals("companion_challenge"))
                            .forEach(rel -> {
                                if (modifiers.contains(rel)) {
                                    curseStack[0] += 2;
                                } else {
                                    modifiers.add(rel);
                                }
                            });
                }
            }
        });
        
        if (curseStack[0] > 3) {
            VaultModifier<?> noFruit = VaultModifierRegistry.get(new ResourceLocation("the_vault", "modifier_type/player_no_vault_fruit"));
            vault.get(Vault.MODIFIERS).addModifier(noFruit, 1, true, JavaRandom.ofNanoTime());
        } else {
            VaultModifier<?> companionCurse = VaultModifierRegistry.get(new ResourceLocation("the_vault", "companion_curse"));
            for (int i = 0; i < curseStack[0]; i++) {
                vault.get(Vault.MODIFIERS).addModifier(companionCurse, 1, true, JavaRandom.ofNanoTime());
            }
        }

        ci.cancel();
    }

    @Inject(
        at = @At("HEAD"),
        method = "onVaultLeaveCompanionCooldown(Liskallia/vault/event/event/VaultLeaveEvent;)V",
        cancellable = true
    )
    private static void onVaultLeaveCompanionCooldown(VaultLeaveEvent event, CallbackInfo ci) {
        ServerPlayer player = event.getPlayer();
        Vault vault = event.getVault();
        if (!VaultUtils.isSpecialVault(vault) && player != null) {
            HelperCompanions.getCompanions(player).stream()
                .filter(stack -> CompanionItem.getCompanionHearts(stack) > 0)
                .forEach(stack -> {
                    if (CompanionItem.getCompanionHearts(stack) == 0) {
                        return;
                    }
                    
                    CompanionItem.startCompanionCooldown(stack);
                    ExpertiseTree expertises = PlayerExpertisesData.get(player.getLevel()).getExpertises(player);
                    
                    float reduction = expertises.getAll(CompanionCooldownExpertise.class, Skill::isUnlocked).stream()
                        .map(CompanionCooldownExpertise::getCooldownReduction)
                        .reduce(0f, Float::sum);

                    if (reduction > 0.0F) {
                        int current = CompanionItem.getCurrentCooldown(stack);
                        int reduceBy = Mth.floor((float)current * reduction);
                        if (reduceBy > 0) {
                            CompanionItem.reduceCooldown(stack, reduceBy);
                        }
                    }

                    CompanionItem.incrementVaultRuns(stack);
                });
        }
        ci.cancel();
    }
}
