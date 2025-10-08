package net.qiyanamark.companionpouch.mixins;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.command.CompanionCommand;
import iskallia.vault.item.CompanionItem;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CompanionCommand.class, remap = false)
public class MixinCompanionCommand {
    @Shadow
    private ItemStack getCompanionItem(ServerPlayer player) {
        throw new RuntimeException("This will be replaced at runtime");
    }

    @Inject(
            at = @At("HEAD"),
            method = "setCooldown",
            cancellable = true
    )
    private void setCooldown(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = this.getCompanionItem(player);
        if (stack.isEmpty()) {
            cir.setReturnValue(0);
        } else {
            int minutes = IntegerArgumentType.getInteger(ctx, "minutes");
            HelperCompanions.getCompanions(player).forEach(companionStack -> CompanionItem.setCompanionCooldown(companionStack, minutes));
            player.sendMessage((new TextComponent("Cooldowns set to " + minutes + " minutes.")).withStyle(ChatFormatting.GREEN), Util.NIL_UUID);
            cir.setReturnValue(1);
        }
    }
}
