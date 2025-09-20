package net.qiyanamark.companionpouch.mixins;

import java.util.Optional;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import top.theillusivec4.curios.api.CuriosApi;

import iskallia.vault.item.CompanionItem;

import net.qiyanamark.companionpouch.item.ItemPouchCompanion;

@Mixin(value = CompanionItem.class, remap = false)
public abstract class MixinCompanionItem {
    /**
     * @reason
     * The logic is completely replaced to search all curio
     * slots for the first companion item or pouch. A helper is not
     * sufficient because we want existing code to use the new behaviour
     * as well.
     * @author MaxTheMooshroom (Maxine Zick <maxine@pnk.dev>)
     */
    @Overwrite(remap = false)
    public static Optional<ItemStack> getCompanion(LivingEntity entity) {
        if (entity.isSpectator()) {
            return Optional.empty();
        }

        return CuriosApi.getCuriosHelper()
            .findFirstCurio(entity, slot -> slot.getItem() instanceof CompanionItem || slot.getItem() instanceof ItemPouchCompanion)
            .map(slot -> slot.stack());
    }
}
