package net.qiyanamark.companionpouch.helpers;

import iskallia.vault.item.CompanionItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.qiyanamark.companionpouch.item.BaseItemPouchCompanion;

public class HandlerSlotContainerPouchCompanion extends SlotItemHandler {
    public HandlerSlotContainerPouchCompanion(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof CompanionItem && !(item instanceof BaseItemPouchCompanion);
    }
}
