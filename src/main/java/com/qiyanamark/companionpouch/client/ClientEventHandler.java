package com.qiyanamark.companionpouch.client;

import com.qiyanamark.companionpouch.item.CompanionPouchItem;
import iskallia.vault.client.VaultAbilityKeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }


        if (isTemporalKeybindPressed()) {
            handleTemporalKeybind(mc.player);
        }
    }

    private static boolean isTemporalKeybindPressed() {

        return false;
    }

    private static void handleTemporalKeybind(Player player) {

        CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
            handler.getStacksHandler("head").ifPresent(stacksHandler -> {
                for (int i = 0; i < stacksHandler.getSlots(); i++) {
                    ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                    if (stack.getItem() instanceof CompanionPouchItem) {
                        if (CompanionPouchItem.isOwner(stack, player)) {

                            CompanionPouchItem.activateAllTemporals(player);
                            return;
                        }
                    }
                }
            });
        });
    }
}
