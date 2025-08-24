package net.qiyanamark.companionactivator.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.qiyanamark.companionactivator.net.NetworkHandler;
import net.qiyanamark.companionactivator.net.ActivateEquippedTemporalsC2SPacket;

import java.util.Arrays;
import java.util.Optional;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientKeyHandler {

    private static final String TARGET_KEY_NAME = "key.the_vault.use_companion_temporal";
    private static KeyMapping found = null;

    private static KeyMapping findKeyMapping() {
        if (found != null) return found;
        try {
            KeyMapping[] all = Minecraft.getInstance().options.keyMappings;
            Optional<KeyMapping> opt = Arrays.stream(all).filter(k -> TARGET_KEY_NAME.equals(k.getName())).findFirst();
            if (opt.isPresent()) {
                found = opt.get();
                return found;
            }
        } catch (Throwable ignored) {}

        return null;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent ev) {
        if (FMLEnvironment.dist != Dist.CLIENT || ev.phase != TickEvent.Phase.END) return;
        try {
            KeyMapping km = findKeyMapping();
            if (km != null && km.consumeClick()) {

                NetworkHandler.CHANNEL.sendToServer(new ActivateEquippedTemporalsC2SPacket());
            }
        } catch (Throwable t) {

        }
    }
}
