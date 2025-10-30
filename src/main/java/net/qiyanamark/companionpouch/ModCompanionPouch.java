package net.qiyanamark.companionpouch;

import java.util.Optional;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

import iskallia.vault.core.vault.VaultUtils;
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
import net.qiyanamark.companionpouch.network.PacketRequestActionTemporal;
import net.qiyanamark.companionpouch.network.PacketRequestOpenPouchMenu;
import net.qiyanamark.companionpouch.screen.ScreenInterfacePouch;
import net.qiyanamark.companionpouch.screen.ScreenInventoryPouch;
import net.qiyanamark.companionpouch.util.Structs;
import org.lwjgl.glfw.GLFW;

import static iskallia.vault.init.ModKeybinds.useCompanionTemporal;

@Mod(ModCompanionPouch.MOD_ID)
public class ModCompanionPouch {
    public static final String MOD_ID = "companionpouch";

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

    public static Optional<LocalPlayer> getClientPlayer() {
        return Optional.ofNullable(Minecraft.getInstance().player);
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(CatalogMenu.COMPANION_POUCH_INVENTORY, ScreenInventoryPouch::new);
            MenuScreens.register(CatalogMenu.COMPANION_POUCH_INTERFACE, ScreenInterfacePouch::new);
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
                ModCompanionPouch.getClientPlayer()
                    .filter(lPlayer -> VaultUtils.getVault(lPlayer.level).isPresent())
                    .ifPresent(
                        lPlayer -> {
                            Optional<Pair<Structs.LocationPouch, ItemStack>> locMaybe = Structs.LocationPouch.findOnPlayer(lPlayer);
                            if (locMaybe.isPresent()) {
                                if (lPlayer.isCrouching()) {
                                    PacketRequestOpenPouchMenu.INTERFACE.sendToServer();
                                } else {
                                    PacketRequestActionTemporal.Actions.ACTIVATE.sendToServer((byte) -1);
                                }
                            }
                        }
                    );

                ModCompanionPouch.getClientPlayer()
                    .filter(lPlayer -> VaultUtils.getVault(lPlayer.level).isEmpty())
                    .ifPresent(lPlayer -> PacketRequestOpenPouchMenu.INVENTORY.sendToServer());
            }
        }

        private static void onKeyRelease(InputConstants.Key key) {}
    }
}