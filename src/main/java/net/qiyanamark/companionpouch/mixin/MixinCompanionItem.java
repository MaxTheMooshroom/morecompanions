package net.qiyanamark.companionpouch.mixin;

import java.util.Optional;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.CompanionItem;
import net.qiyanamark.companionpouch.container.ContainerItemCurioPouch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

@Mixin(value = CompanionItem.class, remap = false)
public abstract class MixinCompanionItem {
    @WrapMethod(
        method = "getCompanion(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Optional;"
    )
    private static Optional<ItemStack> getCompanion(LivingEntity entity, Operation<Optional<ItemStack>> getCompanion) {
        if (entity.isSpectator()) {
            return Optional.empty();
        }

        ItemStack handStack = entity.getMainHandItem();
        if (handStack.is(ModItems.COMPANION)) {
            return Optional.of(handStack);
        }

        handStack = entity.getOffhandItem();
        if (handStack.is(ModItems.COMPANION)) {
            return Optional.of(handStack);
        }

        return CuriosApi.getCuriosHelper()
            .findFirstCurio(entity, slot -> slot.getItem() instanceof CompanionItem)
            .map(SlotResult::stack);
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
        method = "grantVaultCompletionXP(Lnet/minecraft/world/entity/player/Player;I)V",
        at = @At(
            value = "INVOKE",
            target = "Liskallia/vault/item/CompanionItem;getCompanion(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/Optional;"
        )
    )
    private static void grantVaultCompletionXP(Player player, int experience, CallbackInfo ci) {
        (new ContainerItemCurioPouch(player)).getCompanions()
            .peek(companionStack -> grantVaultCompletionXP$itemCompanion(player, companionStack, experience))
            .close();
    }
}
