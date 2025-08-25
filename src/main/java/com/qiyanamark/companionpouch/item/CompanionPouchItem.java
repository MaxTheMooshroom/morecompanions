package com.qiyanamark.companionpouch.item;

import com.companionpouch.container.CompanionPouchContainer;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.item.CompanionItem;
import iskallia.vault.init.ModItems;
import iskallia.vault.world.data.ServerVaults;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CompanionPouchItem extends Item implements ICurioItem {
    private static final String SLOT_1_TAG = "companion_slot_1";
    private static final String SLOT_2_TAG = "companion_slot_2";
    private static final String SLOT_3_TAG = "companion_slot_3";
    private static final String OWNER_TAG = "owner";

    public CompanionPouchItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!isOwner(stack, player)) {
            player.displayClientMessage(new TextComponent("This pouch doesn't belong to you!").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return new TextComponent("Companion Pouch");
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inventory, @NotNull Player player) {
                    return new CompanionPouchContainer(windowId, inventory, stack, hand);
                }
            }, buf -> {
                buf.writeEnum(hand);
                buf.writeItem(stack);
            });
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if (!"head".equals(slotContext.identifier())) {
            return false;
        }

        LivingEntity entity = slotContext.entity();
        if (entity instanceof Player player) {
            return isOwner(stack, player);
        }

        return false;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity instanceof Player player && !player.level.isClientSide) {
            if (getOwner(stack) == null) {
                setOwner(stack, player.getUUID());
            }
        }
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        setOwner(stack, player.getUUID());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(new TextComponent("Stores up to 3 companions").withStyle(ChatFormatting.GRAY));
        tooltip.add(new TextComponent("Right-click to manage companions").withStyle(ChatFormatting.YELLOW));
        tooltip.add(new TextComponent("Equip in head curio slot").withStyle(ChatFormatting.GOLD));
        tooltip.add(new TextComponent("VH temporal keybind activates all").withStyle(ChatFormatting.LIGHT_PURPLE));

        int filledSlots = getFilledSlots(stack);
        tooltip.add(new TextComponent("Companions: " + filledSlots + "/3").withStyle(ChatFormatting.AQUA));


        for (int i = 0; i < 3; i++) {
            ItemStack companion = getCompanionInSlot(stack, i);
            if (!companion.isEmpty()) {
                String name = CompanionItem.getPetName(companion);
                tooltip.add(new TextComponent("Slot " + (i + 1) + ": " + name).withStyle(ChatFormatting.GREEN));

                Optional<ResourceLocation> temporal = CompanionItem.getTemporalModifier(companion);
                if (temporal.isPresent()) {
                    VaultModifier<?> modifier = VaultModifierRegistry.get(temporal.get());
                    String modifierName = modifier != null ? modifier.getDisplayName() : temporal.get().getPath();
                    tooltip.add(new TextComponent("  Temporal: " + modifierName).withStyle(ChatFormatting.LIGHT_PURPLE));

                    if (CompanionItem.isOnCooldown(companion)) {
                        int cooldown = CompanionItem.getCurrentCooldown(companion);
                        tooltip.add(new TextComponent("  Cooldown: " + formatTime(cooldown)).withStyle(ChatFormatting.RED));
                    } else {
                        tooltip.add(new TextComponent("  Ready!").withStyle(ChatFormatting.GREEN));
                    }
                }
            }
        }
    }

    // Static method called by ClientEventHandler to activate all temporals
    public static void activateAllTemporals(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;


        ServerVaults.get(player.level).ifPresent(vault -> {

            CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
                handler.getStacksHandler("head").ifPresent(stacksHandler -> {
                    for (int i = 0; i < stacksHandler.getSlots(); i++) {
                        ItemStack pouchStack = stacksHandler.getStacks().getStackInSlot(i);
                        if (pouchStack.getItem() instanceof CompanionPouchItem) {
                            if (CompanionPouchItem.isOwner(pouchStack, player)) {
                                // Activate all companions with temporal modifiers
                                activateAllTemporalsInPouch(serverPlayer, pouchStack, vault);
                                return;
                            }
                        }
                    }
                });
            });
        });
    }

    private static void activateAllTemporalsInPouch(ServerPlayer player, ItemStack pouchStack, Vault vault) {
        for (int slot = 0; slot < 3; slot++) {
            ItemStack companion = getCompanionInSlot(pouchStack, slot);

            if (!companion.isEmpty() &&
                    CompanionItem.getCompanionHearts(companion) > 0 &&
                    CompanionItem.getTemporalModifier(companion).isPresent() &&
                    !CompanionItem.isOnCooldown(companion) &&
                    !CompanionItem.hasUsedTemporalIn(companion, vault.get(Vault.ID))) {


                CompanionItem.activateTemporalModifier(player, companion, vault);


                CompanionItem.startCompanionCooldown(companion);


                setCompanionInSlot(pouchStack, slot, companion);
            }
        }
    }

    // Utility methods for companion management
    public static ItemStack getCompanionInSlot(ItemStack pouch, int slot) {
        if (slot < 0 || slot > 2) return ItemStack.EMPTY;

        CompoundTag tag = pouch.getOrCreateTag();
        String slotTag = getSlotTagName(slot);

        if (tag.contains(slotTag)) {
            return ItemStack.of(tag.getCompound(slotTag));
        }

        return ItemStack.EMPTY;
    }

    public static void setCompanionInSlot(ItemStack pouch, int slot, ItemStack companion) {
        if (slot < 0 || slot > 2) return;

        CompoundTag tag = pouch.getOrCreateTag();
        String slotTag = getSlotTagName(slot);

        if (companion.isEmpty()) {
            tag.remove(slotTag);
        } else {
            CompoundTag companionTag = new CompoundTag();
            companion.save(companionTag);
            tag.put(slotTag, companionTag);
        }
    }

    public static boolean isValidCompanion(ItemStack stack) {
        return stack.getItem() == ModItems.COMPANION && CompanionItem.hasCompanionData(stack);
    }

    public static UUID getOwner(ItemStack pouch) {
        CompoundTag tag = pouch.getOrCreateTag();
        if (tag.contains(OWNER_TAG)) {
            try {
                return UUID.fromString(tag.getString(OWNER_TAG));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    public static void setOwner(ItemStack pouch, UUID owner) {
        pouch.getOrCreateTag().putString(OWNER_TAG, owner.toString());
    }

    public static boolean isOwner(ItemStack pouch, Player player) {
        UUID owner = getOwner(pouch);
        return owner != null && owner.equals(player.getUUID());
    }

    private static String getSlotTagName(int slot) {
        return switch (slot) {
            case 0 -> SLOT_1_TAG;
            case 1 -> SLOT_2_TAG;
            case 2 -> SLOT_3_TAG;
            default -> "";
        };
    }

    private int getFilledSlots(ItemStack stack) {
        int count = 0;
        for (int i = 0; i < 3; i++) {
            if (!getCompanionInSlot(stack, i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
}
