package com.qiyanamark.companionpouch;


import com.qiyanamark.companionpouch.client.ClientEventHandler;
import com.qiyanamark.companionpouch.init.ModContainers;
import com.qiyanamark.companionpouch.init.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("companionpouch")
public class CompanionPouchMod {
    public static final String MOD_ID = "companionpouch";

    public CompanionPouchMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modEventBus);
        ModContainers.CONTAINERS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(ClientEventHandler.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }
}
