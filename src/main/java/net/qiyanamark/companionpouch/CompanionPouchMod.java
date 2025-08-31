package net.qiyanamark.companionpouch;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.client.event.ScreenEvent;

import net.qiyanamark.companionpouch.init.ModItems;
import net.qiyanamark.companionpouch.init.ModMenus;
import net.qiyanamark.companionpouch.init.ModNetwork;
import net.qiyanamark.companionpouch.screen.CompanionPouchScreen;
import net.qiyanamark.companionpouch.util.CompanionModifierUtil;
import net.minecraft.client.gui.screens.MenuScreens;

@Mod(CompanionPouchMod.MOD_ID)
public class CompanionPouchMod {
    public static final String MOD_ID = "companionpouch";

    public CompanionPouchMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.REGISTER.register(modBus);
        ModMenus.REGISTER.register(modBus);
        ModNetwork.register();

        modBus.addListener(this::clientSetup);

        // Client-only ticking key watcher registered on client dist
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientOnly::init);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.COMPANION_POUCH.get(), CompanionPouchScreen::new);
        });
    }

    private static class ClientOnly {
        static void init() { CompanionModifierUtil.registerClientKeyWatcher(); }
    }
}