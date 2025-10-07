package net.qiyanamark.companionpouch;

import iskallia.vault.core.vault.VaultUtils;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.qiyanamark.companionpouch.catalog.CatalogCapability;
import net.qiyanamark.companionpouch.network.PacketRequestOpenInventoryPouch;
import net.qiyanamark.companionpouch.util.Structs;
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
            }
        }

        private static void onKeyPress(InputConstants.Key key) {
            if (useCompanionTemporal.isActiveAndMatches(key)) {
                LocalPlayer lPlayer = ModCompanionPouch.getClientPlayer();
                Structs.LocationPouch.findOnPlayer(lPlayer)
                    .ifPresent(pouchStack -> {
                        if (VaultUtils.getVault(lPlayer.level).isPresent()) {
                            if (lPlayer.isCrouching()) {
                                PacketRequestOpenInterfacePouch.sendToServer();
                            } // else {
//                                PacketRequestActivationTemporal.sendToServer((byte) -1); // use pouch setting
//                            }
                        } else {
                            PacketRequestOpenInventoryPouch.sendToServer();
                        }
                    });
            }
        }

        private static void onKeyRelease(InputConstants.Key key) {}
    }

    @OnlyIn(Dist.CLIENT)
    public static class Debug {
        public static void messageLocal(TextComponent component) {
            if (!DEBUG) {
                return;
            }

            LocalPlayer lPlayer = ModCompanionPouch.getClientPlayer();
            if (lPlayer != null) {
                lPlayer.sendMessage(component, lPlayer.getUUID());
            }
        }

        public static void messageLocal(String message) {
            Debug.messageLocal(new TextComponent(message));
        }
    }
}