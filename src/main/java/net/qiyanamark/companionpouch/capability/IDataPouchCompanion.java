package net.qiyanamark.companionpouch.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.items.IItemHandler;

public interface IDataPouchCompanion extends IItemHandler {
    byte getActivationIndex();
    void setActivationIndex(byte index);

    boolean tryActivateTemporal(byte index, ServerPlayer sPlayer);

    CompoundTag save();
}
