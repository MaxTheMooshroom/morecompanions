package com.qiyanamark.companionpouch.init;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModKeybinds {
    public static KeyMapping USE_COMPANION_TEMPORAL;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            USE_COMPANION_TEMPORAL = new KeyMapping(
                    "key.the_vault.use_companion_temporal",
                    InputConstants.KEY_T,
                    "key.categories.the_vault"
            );

            ClientRegistry.registerKeyBinding(USE_COMPANION_TEMPORAL);
        });
    }
}