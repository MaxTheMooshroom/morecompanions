package net.qiyanamark.companionpouch.mixins;

import java.util.Optional;
import java.util.stream.IntStream;

import iskallia.vault.init.ModConfigs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.qiyanamark.companionpouch.capability.IDataPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogCapability;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.util.Structs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import top.theillusivec4.curios.api.CuriosApi;

import iskallia.vault.item.CompanionItem;
import top.theillusivec4.curios.api.SlotResult;

@Mixin(value = CompanionItem.class, remap = false)
public abstract class MixinCompanionItem {
    @Inject(
        at = @At("HEAD"),
        method = "getCompanion(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Optional;",
        cancellable = true
    )
    private static void getCompanion(LivingEntity entity, CallbackInfoReturnable<Optional<ItemStack>> cir) {
        if (entity.isSpectator()) {
            cir.setReturnValue(Optional.empty());
            cir.cancel();
            return;
        }

        Optional<ItemStack> result = CuriosApi.getCuriosHelper()
            .findFirstCurio(entity, slot -> slot.getItem() instanceof CompanionItem)
            .map(SlotResult::stack);

        cir.setReturnValue(result);
        cir.cancel();
    }

    @Unique
    private static void grantVaultCompletionXP$itemCompanion(Player player, ItemStack companionStack, int experience) {
        int xp = Math.round((float) experience * ModConfigs.COMPANIONS.getCompletionXpShare());
        if (xp > 0) {
            CompanionItem.addCompanionXP(companionStack, xp);
            player.sendMessage(new TextComponent("Granted XP to " + CompanionItem.getPetName(companionStack)), player.getUUID());
        } else {
            player.sendMessage(new TextComponent("No XP granted to " + CompanionItem.getPetName(companionStack)), player.getUUID());
        }
    }

    @Inject(
            at = @At("HEAD"),
            method = "grantVaultCompletionXP(Lnet/minecraft/world/entity/player/Player;I)V",
            cancellable = true
    )
    private static void grantVaultCompletionXP(Player player, int experience, CallbackInfo ci) {
        if (!player.level.isClientSide) {
            HelperCompanions.forEachCompanion(player, companionStack -> grantVaultCompletionXP$itemCompanion(player, companionStack, experience));
        }
        ci.cancel();
    }
}
