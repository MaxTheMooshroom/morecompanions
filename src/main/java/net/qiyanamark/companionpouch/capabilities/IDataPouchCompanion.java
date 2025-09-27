package net.qiyanamark.companionpouch.capabilities;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface IDataPouchCompanion {
    int getSize();
    void setSize(int newSize);

    int getActivationIndex();
    void setActivationIndex(int index);

    NonNullList<ItemStack> getItemStacks();
    NonNullList<Pair<Integer, ItemStack>> getCompanions();
}
