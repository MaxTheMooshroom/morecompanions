package net.qiyanamark.companionpouch.catalog;

import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.qiyanamark.companionpouch.capability.IDataPouchCompanion;
import net.qiyanamark.companionpouch.ModCompanionPouch;

@Mod.EventBusSubscriber(modid = ModCompanionPouch.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CatalogCapability {
    public static final Capability<IDataPouchCompanion> COMPANION_POUCH_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.register(IDataPouchCompanion.class);
    }
}
