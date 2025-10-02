package net.qiyanamark.companionpouch.capability;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface IDataPouchCompanion extends IItemHandler {
    byte getActivationIndex();
    void setActivationIndex(byte index);

    boolean tryActivateTemporal(byte index, ServerPlayer sPlayer);
}
