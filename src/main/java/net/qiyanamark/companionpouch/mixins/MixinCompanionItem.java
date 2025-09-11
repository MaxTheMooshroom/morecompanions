package net.qiyanamark.companionpouch.mixins;

import java.util.Optional;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import top.theillusivec4.curios.api.CuriosApi;

import iskallia.vault.item.CompanionItem;

@Mixin(value = CompanionItem.class, remap = false)
public abstract class MixinCompanionItem extends CompanionItem {
    public MixinCompanionItem(ResourceLocation id, Properties properties) {
        super(id, properties);
    }

    @Overwrite(remap = false)
    public static Optional<ItemStack> getCompanion(LivingEntity entity) {
        if (entity.isSpectator()) return Optional.empty();

        return CuriosApi.getCuriosHelper()
            .findFirstCurio(entity, slot -> slot.getItem() instanceof CompanionItem)
            .map(slot -> slot.stack());
    }
}
