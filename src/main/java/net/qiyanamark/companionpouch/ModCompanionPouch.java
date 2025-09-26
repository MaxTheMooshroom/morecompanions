package net.qiyanamark.companionpouch;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.qiyanamark.companionpouch.catalog.CatalogMenu;
import net.qiyanamark.companionpouch.catalog.CatalogNetwork;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.network.PacketRequestActivationTemporal;
import net.qiyanamark.companionpouch.network.PacketRequestOpenInterfacePouch;
import net.qiyanamark.companionpouch.screen.ScreenInterfacePouchCompanion;
import net.qiyanamark.companionpouch.screen.ScreenInventoryPouchCompanion;

import static iskallia.vault.init.ModKeybinds.useCompanionTemporal;

import java.util.Optional;

@Mod(ModCompanionPouch.MOD_ID)
public class ModCompanionPouch {
    public static final String MOD_ID = "companionpouch";

    public static ResourceLocation rel(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    public ModCompanionPouch() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(ModCompanionPouch::clientSetup);

        CatalogNetwork.register();
    }

    public static MinecraftServer getServer() {
        return Minecraft.getInstance().level.getServer();
    }

    public static LocalPlayer getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(CatalogMenu.COMPANION_POUCH_INVENTORY, ScreenInventoryPouchCompanion::new);
            MenuScreens.register(CatalogMenu.COMPANION_POUCH_INTERFACE, ScreenInterfacePouchCompanion::new);
        });
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class HandlerInput {
        private static boolean shifting = false;

        @SubscribeEvent
        public static void onKey(InputEvent.KeyInputEvent event) {
            InputConstants.Key key = InputConstants.getKey(event.getKey(), event.getScanCode());

            switch (event.getAction()) {
            case GLFW.GLFW_PRESS:
                HandlerInput.onKeyPress(key);
                return;
            case GLFW.GLFW_RELEASE:
                HandlerInput.onKeyRelease(key);
                return;
            case GLFW.GLFW_REPEAT:
                return;
            }
        }

        private static void onKeyPress(InputConstants.Key key) {
            if (useCompanionTemporal.isActiveAndMatches(key)) {
                if (HandlerInput.shifting) {
                    PacketRequestOpenInterfacePouch.sendToServer();
                    return;
                } else {
                    Optional<ItemStack> pouchStackMaybe = HelperCompanions.getCompanionPouch(ModCompanionPouch.getClientPlayer());
                    pouchStackMaybe.ifPresent(pouchStack -> {
                        PacketRequestActivationTemporal.sendToServer(-1); // use pouch setting
                    });
                }
            }

            InputConstants.Key shiftKey = Minecraft.getInstance().options.keyShift.getKey();
            if (key.getValue() == shiftKey.getValue() && Minecraft.getInstance().screen == null) {
                HandlerInput.shifting = true;
            }
        }

        private static void onKeyRelease(InputConstants.Key key) {
            InputConstants.Key shiftKey = Minecraft.getInstance().options.keyShift.getKey();
            if (key.getValue() == shiftKey.getValue()) {
                HandlerInput.shifting = false;
            }
        }
    }
}