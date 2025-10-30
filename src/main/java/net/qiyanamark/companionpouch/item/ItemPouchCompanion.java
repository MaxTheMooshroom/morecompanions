package net.qiyanamark.companionpouch.item;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import net.minecraftforge.network.NetworkHooks;
import net.qiyanamark.companionpouch.menu.container.MenuInventoryPouchCompanion;
import net.qiyanamark.companionpouch.util.Structs;
import net.qiyanamark.companionpouch.util.annotations.Extends;
import net.qiyanamark.companionpouch.util.annotations.Implements;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import static iskallia.vault.init.ModItems.VAULT_MOD_GROUP;

public class ItemPouchCompanion extends Item implements ICurioItem {
    public static final String REL_PATH = "pouch_companion";
    public static final int DEFAULT_SLOT_COUNT = 3;
    public static final int MAX_SLOT_COUNT = 9;

    public ItemPouchCompanion() {
        super(new Properties().stacksTo(1).tab(VAULT_MOD_GROUP));
    }

    @Override
    @Extends(Item.class)
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack pouchStack = player.getItemInHand(hand);

        if (player instanceof ServerPlayer sPlayer) {
            if (!player.isCrouching()) {
                Structs.LocationPouch loc = Structs.LocationPouch.fromHand(hand);
                NetworkHooks.openGui(sPlayer, MenuInventoryPouchCompanion.getProviderFromLocation(loc), buf -> {
                    buf.writeBoolean(true);
                    buf.writeEnum(loc);
                });
                return InteractionResultHolder.sidedSuccess(pouchStack, level.isClientSide);
            }
        }

        return InteractionResultHolder.pass(pouchStack);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        return Optional.ofNullable(ctx.getPlayer())
            .filter(Player::isCrouching)
            .map(player -> {
                // TODO
                return InteractionResult.sidedSuccess(player.level.isClientSide);
            })
            .orElse(InteractionResult.PASS);
    }

    // note: dimension().location().getNamespace() is not a bug or typo, it is consistent
    // with behaviour defined by the vault mod.
    @Override
    @Implements(ICurioItem.class)
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return !slotContext.entity().level.dimension().location().getNamespace().equals("the_vault");
    }

    // note: dimRel.getNamespace() is not a bug or typo, it is consistent with behaviour defined
    // by the vault mod.
    @Override
    @Implements(ICurioItem.class)
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            ResourceLocation dimRel = player.level.dimension().location();
            return !dimRel.getNamespace().equals("the_vault");
        }

        return true;
    }
}