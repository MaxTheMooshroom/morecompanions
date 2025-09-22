package net.qiyanamark.companionpouch.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import top.theillusivec4.curios.api.CuriosApi;

import iskallia.vault.item.CompanionItem;

import net.qiyanamark.companionpouch.item.ItemPouchCompanion;

public class HelperCompanions {
    public static List<ItemStack> getCompanions(LivingEntity entity) {
        if (entity.isSpectator()) {
            return Collections.emptyList();
        }

        return CuriosApi.getCuriosHelper()
            .findFirstCurio(entity, stack -> stack.getItem() instanceof CompanionItem || stack.getItem() instanceof ItemPouchCompanion)
            .map(slot -> {
                ItemStack stack = slot.stack();
                if (stack.getItem() instanceof ItemPouchCompanion) {
                    return ItemPouchCompanion.getContents(stack);
                } else {
                    return new ArrayList<>(Arrays.asList(stack));
                }
            })
            .orElseGet(Collections::emptyList);
    }
}
