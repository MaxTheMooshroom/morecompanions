package net.qiyanamark.companionpouch.network;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;


import java.util.function.Supplier;


import net.qiyanamark.companionpouch.util.CompanionModifierUtil;


public class OpenTemporalPacket {
    public OpenTemporalPacket() {}
    public static void encode(OpenTemporalPacket pkt, FriendlyByteBuf buf) {}
    public static OpenTemporalPacket decode(FriendlyByteBuf buf) { return new OpenTemporalPacket(); }


    public static void handle(OpenTemporalPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) CompanionModifierUtil.serverOpenAllTemporal(player);
        });
        ctx.get().setPacketHandled(true);
    }
}