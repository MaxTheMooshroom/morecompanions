package net.qiyanamark.companionpouch.helper;

import java.util.ArrayList;
import java.util.List;

import iskallia.vault.item.CompanionItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import top.theillusivec4.curios.api.CuriosApi;

public class HelperCompanions {
    public static List<ItemStack> getCompanions(LivingEntity entity) {
        if (entity.isSpectator()) return new ArrayList<>();

        return CuriosApi.getCuriosHelper()
            .findCurios(entity, slot -> slot.getItem() instanceof CompanionItem || slot.getItem() instanceof ItemPouchCompanion)
            .stream()
            .map(slot -> slot.stack())
            .toList();
    }
}
