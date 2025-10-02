package net.qiyanamark.companionpouch.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CapabilityPouchCompanion {
    public static final Capability<IDataPouchCompanion> COMPANION_POUCH_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
}
