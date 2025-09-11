package net.qiyanamark.companionpouch;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.qiyanamark.companionpouch.catalog.CatalogContainer;
import net.qiyanamark.companionpouch.screen.ScreenPouchCompanion;

@Mod(ModCompanionPouch.MOD_ID)
public class ModCompanionPouch {
    public static final String MOD_ID = "companionpouch";

    public static ResourceLocation rel(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    public ModCompanionPouch() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(CatalogContainer.COMPANION_POUCH, ScreenPouchCompanion::new);
        });
    }
}