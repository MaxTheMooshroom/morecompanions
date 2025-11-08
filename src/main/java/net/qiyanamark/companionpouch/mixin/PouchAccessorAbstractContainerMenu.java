package net.qiyanamark.companionpouch.mixin;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerMenu.class)
public interface PouchAccessorAbstractContainerMenu {
    @Accessor("dataSlots")
    List<DataSlot> getDataSlots();

    @Invoker("updateDataSlotListeners")
    void invokeUpdateDataSlotListeners(int index, int value);

    @Invoker("synchronizeSlotToRemote")
    void invokeSynchronizeSlotToRemote(int pSlotIndex, ItemStack pStack, Supplier<ItemStack> pSupplier);
}
