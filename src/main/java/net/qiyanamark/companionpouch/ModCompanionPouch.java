package net.qiyanamark.companionpouch;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import top.theillusivec4.curios.api.SlotTypeMessage;

import net.qiyanamark.companionpouch.catalog.CatalogMenu;
import net.qiyanamark.companionpouch.screen.ScreenPouchCompanion;

@Mod(ModCompanionPouch.MOD_ID)
public class ModCompanionPouch {
    public static final String MOD_ID = "companionpouch";

    public static ResourceLocation rel(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    public ModCompanionPouch() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(ModCompanionPouch::clientSetup);
        modBus.addListener(ModCompanionPouch::registerPouchSlot);
        modBus.addListener(ModCompanionPouch::registerTextures);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(CatalogMenu.COMPANION_POUCH, ScreenPouchCompanion::new);
        });
    }

    public static void registerTextures(TextureStitchEvent.Pre event) {
        event.addSprite(rel("slot/pouch_companion"));
    }

    public static void registerPouchSlot(InterModEnqueueEvent event) {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> {
            return new SlotTypeMessage.Builder("pouch_companion")
                .size(1)
                .priority(45)
                .icon(rel("slot/pouch_companion"))
                .build();
        });
    }
}