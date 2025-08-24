package net.qiyanamark.companionactivator;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.qiyanamark.companionactivator.net.NetworkHandler;

@Mod(CompanionActivatorMod.MODID)
public class CompanionActivatorMod {
    public static final String MODID = "morecompanions";

    public CompanionActivatorMod() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent ev) {
        NetworkHandler.register();
    }
}
