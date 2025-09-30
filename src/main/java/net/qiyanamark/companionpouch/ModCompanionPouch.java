package net.qiyanamark.companionpouch;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.qiyanamark.companionpouch.catalog.CatalogItem;
import net.qiyanamark.companionpouch.catalog.CatalogMenu;
import net.qiyanamark.companionpouch.catalog.CatalogNetwork;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.network.PacketRequestActivationTemporal;
import net.qiyanamark.companionpouch.network.PacketRequestOpenInterfacePouch;
import net.qiyanamark.companionpouch.screen.ScreenInterfacePouchCompanion;
import net.qiyanamark.companionpouch.screen.ScreenInventoryPouchCompanion;

import static iskallia.vault.init.ModKeybinds.useCompanionTemporal;

@Mod(ModCompanionPouch.MOD_ID)
public class ModCompanionPouch {
    public static final String MOD_ID = "companionpouch";
    public static final boolean DEBUG = false;

    public static ResourceLocation rel(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    public ModCompanionPouch() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener(ModCompanionPouch::clientSetup);

        CatalogItem.REGISTRY.register(modBus);

        CatalogNetwork.register();
    }

    public static MinecraftServer getServer() {
        return Minecraft.getInstance().level.getServer();
    }

    public static LocalPlayer getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    public static void messageLocalDebug(TextComponent component) {
        if (!DEBUG) {
            return;
        }

        LocalPlayer lPlayer = ModCompanionPouch.getClientPlayer();
        lPlayer.sendMessage(component, lPlayer.getUUID());
    }

    public static void messageLocalDebug(String message) {
        ModCompanionPouch.messageLocalDebug(new TextComponent(message));
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(CatalogMenu.COMPANION_POUCH_INVENTORY, ScreenInventoryPouchCompanion::new);
            MenuScreens.register(CatalogMenu.COMPANION_POUCH_INTERFACE, ScreenInterfacePouchCompanion::new);
        });
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class HandlerInput {
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
                HelperCompanions.getCompanionPouch(ModCompanionPouch.getClientPlayer())
                    .ifPresent(pouchStack -> {
                        if (ModCompanionPouch.getClientPlayer().isCrouching()) {
                            PacketRequestOpenInterfacePouch.sendToServer();
                        } else {
                            PacketRequestActivationTemporal.sendToServer(-1); // use pouch setting
                        }
                    });
            }
        }

        private static void onKeyRelease(InputConstants.Key key) {}
    }
}