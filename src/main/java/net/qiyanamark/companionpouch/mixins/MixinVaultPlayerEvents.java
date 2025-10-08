package net.qiyanamark.companionpouch.mixins;



import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import com.mojang.datafixers.util.Pair;
import iskallia.vault.init.ModGameRules;
import iskallia.vault.world.VaultMode;
import iskallia.vault.world.data.PlayerAbilitiesData;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.qiyanamark.companionpouch.capability.IDataPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogCapability;
import net.qiyanamark.companionpouch.util.Structs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
    
    @Unique
    private static void on$companionPouch(ServerPlayer sPlayer, ItemStack pouchStack) {
        IDataPouchCompanion pouchData = pouchStack.getCapability(CatalogCapability.COMPANION_POUCH_CAPABILITY)
                .orElseThrow(IllegalStateException::new);

        IntStream.range(0, pouchData.getSlots())
                .mapToObj(pouchData::getStackInSlot)
                .filter(companionStack -> !companionStack.isEmpty() && CompanionItem.isActive(companionStack) && CompanionItem.isOwner(companionStack, sPlayer))
                .forEach((companionStack) -> on$companionItem(sPlayer, companionStack));

        CompoundTag nbt = pouchData.saveChanges();
        pouchStack.setTag(nbt);
    }
    
    @Unique
    private static void on$companionItem(ServerPlayer sPlayer, ItemStack companionStack) {
        sPlayer.sendMessage(new TextComponent(CompanionItem.getPetName(companionStack) + " lost a heart"), sPlayer.getUUID());
        int hearts = Math.max(CompanionItem.getCompanionHearts(companionStack) - 1, 0);
        CompanionItem.setCompanionHearts(companionStack, hearts);
    }
    
    @Inject(
        at = @At("HEAD"),
        method = "on(Lnet/minecraftforge/event/entity/living/LivingDeathEvent;)V",
        cancellable = true
    )
    private static void on(LivingDeathEvent event, CallbackInfo ci) {
        if (event.getEntity() instanceof ServerPlayer sPlayer) {
            PlayerAbilitiesData.deactivateAllAbilities(sPlayer);
            ServerLevel level = sPlayer.getLevel();
            ServerVaults.get(level).ifPresent(vault -> {
                sPlayer.sendMessage(new TextComponent("You died (in a vault)"), sPlayer.getUUID());
                VaultMode mode = level.getGameRules().getRule(ModGameRules.MODE).get();
                boolean specialVault = VaultUtils.isSpecialVault(vault);
                if (mode != VaultMode.CASUAL && !specialVault) {
                    Optional<ItemStack> pouchStackMaybe = Structs.LocationPouch.CURIO.getFromPlayer(sPlayer);
                    pouchStackMaybe.ifPresentOrElse(
                            pouchStack -> on$companionPouch(sPlayer, pouchStack),
                            () -> CompanionItem.getCompanion(sPlayer)
                                    .filter(stack -> !stack.isEmpty() && CompanionItem.isActive(stack) && CompanionItem.isOwner(stack, sPlayer))
                                    .ifPresent(companionStack -> on$companionItem(sPlayer, companionStack))
                    );
                }
            });
        }
        ci.cancel();
    }
}
