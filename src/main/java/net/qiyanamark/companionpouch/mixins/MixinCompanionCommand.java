package net.qiyanamark.companionpouch.mixins;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.command.CompanionCommand;
import iskallia.vault.item.CompanionItem;
import iskallia.vault.item.CompanionSeries;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.function.Consumer;

@Mixin(value = CompanionCommand.class, remap = false)
public class MixinCompanionCommand {
    @Unique
    private static void companionpouch$modifyCompanions(CallbackInfoReturnable<Integer> cir, MutableComponent successMessage, CommandContext<CommandSourceStack> ctx, Consumer<ItemStack> transformer) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        if (HelperCompanions.forEachCompanion(player, transformer)) {
            player.sendMessage(successMessage, Util.NIL_UUID);
            cir.setReturnValue(1);
        } else {
            cir.setReturnValue(0);
        }
    }

    @Inject(
            at = @At("HEAD"),
            method = "addXP",
            cancellable = true
    )
    private void addXP(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        MutableComponent successMessage = (new TextComponent("Added " + amount + " XP.")).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, companionStack -> CompanionItem.addCompanionXP(companionStack, amount));
    }

    @Inject(
            at = @At("HEAD"),
            method = "setLevel",
            cancellable = true
    )
    private void setLevel(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        int level = IntegerArgumentType.getInteger(ctx, "level");
        MutableComponent successMessage = (new TextComponent("Levels set to " + level)).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, companionStack -> {
            if (CompanionItem.getCompanionLevel(companionStack) < level) {
                while (CompanionItem.getCompanionLevel(companionStack) < level) {
                    CompanionItem.addCompanionXP(companionStack, CompanionItem.getXPRequiredForNextLevel(companionStack));
                }
            } else {
                CompanionItem.setCompanionLevel(companionStack, level);
            }
        });
    }

    @Inject(
            at = @At("HEAD"),
            method = "setHearts",
            cancellable = true
    )
    private void setHearts(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        int hearts = IntegerArgumentType.getInteger(ctx, "hearts");
        MutableComponent successMessage = (new TextComponent("Hearts set to " + hearts)).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, companionStack -> CompanionItem.setCompanionHearts(companionStack, hearts));
    }

    @Inject(
            at = @At("HEAD"),
            method = "setSkin",
            cancellable = true
    )
    private void setSkin(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        String skin = StringArgumentType.getString(ctx, "username");
        MutableComponent successMessage = (new TextComponent("skins set to " + skin)).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, companionStack -> CompanionItem.setSkinName(companionStack, skin));
    }

    @Inject(
            at = @At("HEAD"),
            method = "setCooldown",
            cancellable = true
    )
    private void setCooldown(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        int minutes = IntegerArgumentType.getInteger(ctx, "minutes");
        MutableComponent successMessage = (new TextComponent("Cooldowns set to " + minutes + " minutes.")).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, companionStack -> CompanionItem.setCompanionCooldown(companionStack, minutes));
    }

    @Inject(
            at = @At("HEAD"),
            method = "setRelic",
            cancellable = true
    )
    private void setRelic(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        int slot = IntegerArgumentType.getInteger(ctx, "slot");
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "modifier");
        int model = IntegerArgumentType.getInteger(ctx, "model");

        MutableComponent successMessage = (new TextComponent("Set relic slots " + slot + " to " + id)).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, companionStack -> CompanionItem.setRelic(companionStack, model, slot, id));
    }

    @Inject(
            at = @At("HEAD"),
            method = "clearRelics",
            cancellable = true
    )
    private void clearRelics(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        MutableComponent successMessage = (new TextComponent("Cleared relics")).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, CompanionItem::clearAllRelics);
    }

    @Inject(
            at = @At("HEAD"),
            method = "setXP",
            cancellable = true
    )
    private void setXP(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        int xp = IntegerArgumentType.getInteger(ctx, "xp");
        MutableComponent successMessage = (new TextComponent("Set XP to " + xp)).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, companionStack -> {
            int currentXp = CompanionItem.getCompanionXP(companionStack);
            if (xp > currentXp) {
                CompanionItem.addCompanionXP(companionStack, xp - currentXp);
            } else {
                CompanionItem.setCompanionXP(companionStack, xp);
            }
        });
    }

    @Inject(
            at = @At("HEAD"),
            method = "setMaxHearts",
            cancellable = true
    )
    private void setMaxHearts(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        int max = IntegerArgumentType.getInteger(ctx, "max");
        MutableComponent successMessage = (new TextComponent("Set max hearts to " + max)).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, companionStack -> CompanionItem.setCompanionMaxHearts(companionStack, max));
    }

    @Inject(
            at = @At("HEAD"),
            method = "addCooldown",
            cancellable = true
    )
    private void addCooldown(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
        MutableComponent successMessage = (new TextComponent("Added " + seconds + "s to cooldowns")).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, companionStack -> CompanionItem.addCooldownTime(companionStack, seconds));
    }

    @Inject(
            at = @At("HEAD"),
            method = "reduceCooldown",
            cancellable = true
    )
    private void reduceCooldown(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
        MutableComponent successMessage = (new TextComponent("Reduced cooldowns by " + seconds + "s")).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, companionStack -> CompanionItem.reduceCooldown(companionStack, seconds));
    }

    @Inject(
            at = @At("HEAD"),
            method = "startCooldown",
            cancellable = true
    )
    private void startCooldown(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        MutableComponent successMessage = (new TextComponent("Started cooldowns.")).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, CompanionItem::startCompanionCooldown);
    }

    @Inject(
            at = @At("HEAD"),
            method = "setTemporal",
            cancellable = true
    )
    private void setTemporal(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "modifier");
        MutableComponent successMessage = (new TextComponent("Set temporal modifiers to " + id)).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, companionStack -> CompanionItem.setTemporalModifier(companionStack, id));
    }

    @Inject(
            at = @At("HEAD"),
            method = "setName",
            cancellable = true
    )
    private void setName(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        MutableComponent successMessage = (new TextComponent("Set names to " + name)).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, companionStack -> CompanionItem.setPetName(companionStack, name));
    }

    @Inject(
            at = @At("HEAD"),
            method = "setOwner",
            cancellable = true
    )
    private void setOwner(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        ServerPlayer ownerPlayer = EntityArgument.getPlayer(ctx, "owner");
        UUID owner = ownerPlayer.getUUID();
        MutableComponent successMessage = (new TextComponent("Set owners to " + owner)).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, companionStack -> CompanionItem.setOwner(companionStack, owner));
    }

    @Inject(
            at = @At("HEAD"),
            method = "setType",
            cancellable = true
    )
    private void setType(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        String type = StringArgumentType.getString(ctx, "type");
        MutableComponent successMessage = (new TextComponent("Set types to " + type)).withStyle(ChatFormatting.GREEN);

        companionpouch$modifyCompanions(cir, successMessage, ctx, companionStack -> CompanionItem.setPetType(companionStack, type));
    }

    @Inject(
            at = @At("HEAD"),
            method = "setSeries",
            cancellable = true
    )
    private void setSeries(CommandContext<CommandSourceStack> ctx, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        String seriesStr = StringArgumentType.getString(ctx, "series");

        try {
            CompanionSeries series = switch (seriesStr.toLowerCase()) {
                case "legend" -> CompanionSeries.LEGEND;
                case "pet" -> CompanionSeries.PET;
                default -> throw new IllegalArgumentException();
            };

            MutableComponent successMessage = (new TextComponent("Set series to " + series)).withStyle(ChatFormatting.GREEN);

            companionpouch$modifyCompanions(cir, successMessage, ctx, companionStack -> CompanionItem.setPetSeries(companionStack, series));
        } catch (IllegalArgumentException e) {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            player.sendMessage((new TextComponent("Invalid series")).withStyle(ChatFormatting.RED), Util.NIL_UUID);
            cir.setReturnValue(0);
        }
    }


}
