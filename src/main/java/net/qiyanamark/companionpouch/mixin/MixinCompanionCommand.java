package net.qiyanamark.companionpouch.mixin;

import java.util.UUID;
import java.util.function.Consumer;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import iskallia.vault.command.Command;
import iskallia.vault.command.CompanionCommand;
import iskallia.vault.item.CompanionItem;
import iskallia.vault.item.CompanionSeries;
import net.qiyanamark.companionpouch.container.ContainerItemCurioPouch;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = CompanionCommand.class, remap = false)
public abstract class MixinCompanionCommand extends Command {
    @Unique
    private static int companionpouch$modifyCompanions(Operation<Integer> operation, MutableComponent successMessage, CommandContext<CommandSourceStack> ctx, Consumer<ItemStack> transformer) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        if (player.getMainHandItem().getItem() instanceof CompanionItem) {
            return operation.call(ctx);
        } else if (player.getMainHandItem().getItem() instanceof ItemPouchCompanion) {
            player.sendMessage(successMessage, Util.NIL_UUID);
            return 1;
        } else if (player.getOffhandItem().getItem() instanceof CompanionItem) {
            return operation.call(ctx);
        }

        ContainerItemCurioPouch container = new ContainerItemCurioPouch(player);
        boolean[] foundAny = {false};
        container.getCompanions()
            .peek(transformer)
            .peek(__ -> foundAny[0] = true)
            .close();


        if (foundAny[0]) {
            player.sendMessage(successMessage, Util.NIL_UUID);
            return 1;
        } else {
            return operation.call(ctx);
        }
    }

    @WrapMethod(method = "addXP")
    private int addXP(CommandContext<CommandSourceStack> ctx, Operation<Integer> addXP) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        MutableComponent successMessage = (new TextComponent("Added " + amount + " XP.")).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(addXP, successMessage, ctx, companionStack -> CompanionItem.addCompanionXP(companionStack, amount));
    }

    @WrapMethod(method = "setLevel")
    private int setLevel(CommandContext<CommandSourceStack> ctx, Operation<Integer> setLevel) throws CommandSyntaxException {
        int level = IntegerArgumentType.getInteger(ctx, "level");
        MutableComponent successMessage = (new TextComponent("Levels set to " + level)).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(setLevel, successMessage, ctx, companionStack -> {
            if (CompanionItem.getCompanionLevel(companionStack) < level) {
                while (CompanionItem.getCompanionLevel(companionStack) < level) {
                    CompanionItem.addCompanionXP(companionStack, CompanionItem.getXPRequiredForNextLevel(companionStack));
                }
            } else {
                CompanionItem.setCompanionLevel(companionStack, level);
            }
        });
    }

    @WrapMethod(method = "setHearts")
    private int setHearts(CommandContext<CommandSourceStack> ctx, Operation<Integer> setHearts) throws CommandSyntaxException {
        int hearts = IntegerArgumentType.getInteger(ctx, "hearts");
        MutableComponent successMessage = (new TextComponent("Hearts set to " + hearts)).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(setHearts, successMessage, ctx, companionStack -> CompanionItem.setCompanionHearts(companionStack, hearts));
    }

    @WrapMethod(method = "setSkin")
    private int setSkin(CommandContext<CommandSourceStack> ctx, Operation<Integer> setSkin) throws CommandSyntaxException {
        String skin = StringArgumentType.getString(ctx, "username");
        MutableComponent successMessage = (new TextComponent("skins set to " + skin)).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(setSkin, successMessage, ctx, companionStack -> CompanionItem.setSkinName(companionStack, skin));
    }

    @WrapMethod(method = "setCooldown")
    private int setCooldown(CommandContext<CommandSourceStack> ctx, Operation<Integer> setCooldown) throws CommandSyntaxException {
        int minutes = IntegerArgumentType.getInteger(ctx, "minutes");
        MutableComponent successMessage = (new TextComponent("Cooldowns set to " + minutes + " minutes.")).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(setCooldown, successMessage, ctx, companionStack -> CompanionItem.setCompanionCooldown(companionStack, minutes));
    }

    @WrapMethod(method = "setRelic")
    private int setRelic(CommandContext<CommandSourceStack> ctx, Operation<Integer> setRelic) throws CommandSyntaxException {
        int slot = IntegerArgumentType.getInteger(ctx, "slot");
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "modifier");
        int model = IntegerArgumentType.getInteger(ctx, "model");

        MutableComponent successMessage = (new TextComponent("Set relic slots " + slot + " to " + id)).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(setRelic, successMessage, ctx, companionStack -> CompanionItem.setRelic(companionStack, model, slot, id));
    }

    @WrapMethod(method = "clearRelics")
    private int clearRelics(CommandContext<CommandSourceStack> ctx, Operation<Integer> clearRelics) throws CommandSyntaxException {
        MutableComponent successMessage = (new TextComponent("Cleared relics")).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(clearRelics, successMessage, ctx, CompanionItem::clearAllRelics);
    }

    @WrapMethod(method = "setXP")
    private int setXP(CommandContext<CommandSourceStack> ctx, Operation<Integer> setXP) throws CommandSyntaxException {
        int xp = IntegerArgumentType.getInteger(ctx, "xp");
        MutableComponent successMessage = (new TextComponent("Set XP to " + xp)).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(setXP, successMessage, ctx, companionStack -> {
            int currentXp = CompanionItem.getCompanionXP(companionStack);
            if (xp > currentXp) {
                CompanionItem.addCompanionXP(companionStack, xp - currentXp);
            } else {
                CompanionItem.setCompanionXP(companionStack, xp);
            }
        });
    }

    @WrapMethod(method = "setMaxHearts")
    private int setMaxHearts(CommandContext<CommandSourceStack> ctx, Operation<Integer> setMaxHearts) throws CommandSyntaxException {
        int max = IntegerArgumentType.getInteger(ctx, "max");
        MutableComponent successMessage = (new TextComponent("Set max hearts to " + max)).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(setMaxHearts, successMessage, ctx, companionStack -> CompanionItem.setCompanionMaxHearts(companionStack, max));
    }

    @WrapMethod(method = "addCooldown")
    private int addCooldown(CommandContext<CommandSourceStack> ctx, Operation<Integer> addCooldown) throws CommandSyntaxException {
        int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
        MutableComponent successMessage = (new TextComponent("Added " + seconds + "s to cooldowns")).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(addCooldown, successMessage, ctx, companionStack -> CompanionItem.addCooldownTime(companionStack, seconds));
    }

    @WrapMethod(method = "reduceCooldown")
    private int reduceCooldown(CommandContext<CommandSourceStack> ctx, Operation<Integer> reduceCooldown) throws CommandSyntaxException {
        int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
        MutableComponent successMessage = (new TextComponent("Reduced cooldowns by " + seconds + "s")).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(reduceCooldown, successMessage, ctx, companionStack -> CompanionItem.reduceCooldown(companionStack, seconds));
    }

    @WrapMethod(method = "startCooldown")
    private int startCooldown(CommandContext<CommandSourceStack> ctx, Operation<Integer> startCooldown) throws CommandSyntaxException {
        MutableComponent successMessage = (new TextComponent("Started cooldowns.")).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(startCooldown, successMessage, ctx, CompanionItem::startCompanionCooldown);
    }

    @WrapMethod(method = "setTemporal")
    private int setTemporal(CommandContext<CommandSourceStack> ctx, Operation<Integer> setTemporal) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "modifier");
        MutableComponent successMessage = (new TextComponent("Set temporal modifiers to " + id)).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(setTemporal, successMessage, ctx, companionStack -> CompanionItem.setTemporalModifier(companionStack, id));
    }

    @WrapMethod(method = "setName")
    private int setName(CommandContext<CommandSourceStack> ctx, Operation<Integer> setName) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        MutableComponent successMessage = (new TextComponent("Set names to " + name)).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(setName, successMessage, ctx, companionStack -> CompanionItem.setPetName(companionStack, name));
    }

    @WrapMethod(method = "setOwner")
    private int setOwner(CommandContext<CommandSourceStack> ctx, Operation<Integer> setOwner) throws CommandSyntaxException {
        ServerPlayer ownerPlayer = EntityArgument.getPlayer(ctx, "owner");
        UUID owner = ownerPlayer.getUUID();
        MutableComponent successMessage = (new TextComponent("Set owners to " + owner)).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(setOwner, successMessage, ctx, companionStack -> CompanionItem.setOwner(companionStack, owner));
    }

    @WrapMethod(method = "setType")
    private int setType(CommandContext<CommandSourceStack> ctx, Operation<Integer> setType) throws CommandSyntaxException {
        String type = StringArgumentType.getString(ctx, "type");
        MutableComponent successMessage = (new TextComponent("Set types to " + type)).withStyle(ChatFormatting.GREEN);

        return companionpouch$modifyCompanions(setType, successMessage, ctx, companionStack -> CompanionItem.setPetType(companionStack, type));
    }

    @WrapMethod(method = "setSeries")
    private int setSeries(CommandContext<CommandSourceStack> ctx, Operation<Integer> setSeries) throws CommandSyntaxException {
        String seriesStr = StringArgumentType.getString(ctx, "series");

        try {
            CompanionSeries series = switch (seriesStr.toLowerCase()) {
                case "legend" -> CompanionSeries.LEGEND;
                case "pet" -> CompanionSeries.PET;
                default -> throw new IllegalArgumentException();
            };

            MutableComponent successMessage = (new TextComponent("Set series to " + series)).withStyle(ChatFormatting.GREEN);

            return companionpouch$modifyCompanions(setSeries, successMessage, ctx, companionStack -> CompanionItem.setPetSeries(companionStack, series));
        } catch (IllegalArgumentException e) {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            player.sendMessage((new TextComponent("Invalid series")).withStyle(ChatFormatting.RED), Util.NIL_UUID);
            return 0;
        }
    }


}
