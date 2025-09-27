package net.qiyanamark.companionpouch.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CapabilityDataPouchCompanion {
    public static final Capability<IDataPouchCompanion> COMPANION_POUCH_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
}
