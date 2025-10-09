package net.qiyanamark.companionpouch.event;

import com.mojang.datafixers.util.Pair;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.event.event.VaultJoinEvent;
import iskallia.vault.event.event.VaultLeaveEvent;
import iskallia.vault.init.ModGameRules;
import iskallia.vault.item.CompanionItem;
import iskallia.vault.skill.base.Skill;
import iskallia.vault.skill.expertise.type.CompanionCooldownExpertise;
import iskallia.vault.skill.tree.ExpertiseTree;
import iskallia.vault.world.VaultMode;
import iskallia.vault.world.data.PlayerAbilitiesData;
import iskallia.vault.world.data.PlayerExpertisesData;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.util.Structs;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ModCompanionPouch.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEvents {
    @SubscribeEvent
    public static void onVaultJoinApplyCompanion(VaultJoinEvent event) {
        ServerPlayer player = event.getPlayer();
        Vault vault = event.getVault();
        Set<ResourceLocation> temporals = new HashSet<>();
        Set<ResourceLocation> modifiers = new HashSet<>();

        Set<ResourceLocation> perCompanionModifiers = new HashSet<>();

        int[] curseStack = {0};

        if (Structs.LocationPouch.CURIO.getFromEntity(player).isEmpty()) {
            return;
        }

        HelperCompanions.forEachCompanion(player, stack -> {
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
                            .map(Pair::getSecond)
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
    }

    @SubscribeEvent
    public static void onVaultLeaveCompanionCooldown(VaultLeaveEvent event) {
        ServerPlayer player = event.getPlayer();
        Vault vault = event.getVault();
        if (!VaultUtils.isSpecialVault(vault) && player != null) {
            HelperCompanions.forEachCompanion(player, companionStack -> {
                if (CompanionItem.getCompanionHearts(companionStack) == 0) {
                    return;
                }

                CompanionItem.startCompanionCooldown(companionStack);
                ExpertiseTree expertises = PlayerExpertisesData.get(player.getLevel()).getExpertises(player);

                float reduction = expertises.getAll(CompanionCooldownExpertise.class, Skill::isUnlocked).stream()
                        .map(CompanionCooldownExpertise::getCooldownReduction)
                        .reduce(0f, Float::sum);

                if (reduction > 0.0F) {
                    int current = CompanionItem.getCurrentCooldown(companionStack);
                    int reduceBy = Mth.floor((float)current * reduction);
                    if (reduceBy > 0) {
                        CompanionItem.reduceCooldown(companionStack, reduceBy);
                    }
                }

                CompanionItem.incrementVaultRuns(companionStack);
            });
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer sPlayer) {
            PlayerAbilitiesData.deactivateAllAbilities(sPlayer);
            ServerLevel level = sPlayer.getLevel();

            ServerVaults.get(level).ifPresent(vault -> {
                VaultMode mode = level.getGameRules().getRule(ModGameRules.MODE).get();

                if (mode != VaultMode.CASUAL && !VaultUtils.isSpecialVault(vault)) {
                    HelperCompanions.forEachCompanion(sPlayer, companionStack -> {
                        int hearts = Math.max(CompanionItem.getCompanionHearts(companionStack) - 1, 0);
                        CompanionItem.setCompanionHearts(companionStack, hearts);
                    });
                }
            });
        }
    }
}
