package net.qiyanamark.companionpouch.catalog;

import net.minecraft.world.item.ItemStack;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.qiyanamark.companionpouch.capability.IDataPouchCompanion;
import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.capability.ProviderStoragePouch;

@Mod.EventBusSubscriber(modid = ModCompanionPouch.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CatalogCapability {
    public static final Capability<IDataPouchCompanion> COMPANION_POUCH_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.register(IDataPouchCompanion.class);
    }

    @SubscribeEvent()
    public static void attachCapabilities(final AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() == CatalogItem.COMPANION_POUCH.get()) {
            event.addCapability(ModCompanionPouch.rel(ProviderStoragePouch.REL_ID), new ProviderStoragePouch(event.getObject()));
        }
    }
}
