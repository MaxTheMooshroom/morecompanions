package net.qiyanamark.companionactivator.net;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.qiyanamark.companionactivator.CompanionActivatorMod;

public class NetworkHandler {
    private static final String PROTOCOL = "1";
    public static SimpleChannel CHANNEL;
    private static int IDX = 0;

    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(CompanionActivatorMod.MODID, "main"),
                () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

        CHANNEL.registerMessage(IDX++, ActivateEquippedTemporalsC2SPacket.class,
                ActivateEquippedTemporalsC2SPacket::encode,
                ActivateEquippedTemporalsC2SPacket::decode,
                ActivateEquippedTemporalsC2SPacket::handle);
    }
}
