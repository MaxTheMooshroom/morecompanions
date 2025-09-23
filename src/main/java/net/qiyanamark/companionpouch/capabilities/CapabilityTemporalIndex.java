package net.qiyanamark.companionpouch.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CapabilityTemporalIndex {
    public static final Capability<ITemporalIndex> TEMPORAL_INDEX_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});
}
