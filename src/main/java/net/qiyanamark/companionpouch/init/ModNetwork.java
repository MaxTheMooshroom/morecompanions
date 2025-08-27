package net.qiyanamark.companionpouch.init;


import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.SimpleChannel;
import net.qiyanamark.companionpouch.companionpouch;
import net.qiyanamark.companionpouch.network.OpenTemporalPacket;


public class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(companionpouch.MOD_ID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);


    private static int id = 0;
    private static int nextId() { return id++; }


    public static void register() {
        CHANNEL.registerMessage(nextId(), OpenTemporalPacket.class,
                OpenTemporalPacket::encode, OpenTemporalPacket::decode, OpenTemporalPacket::handle);
    }
}