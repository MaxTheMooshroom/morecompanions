package net.qiyanamark.companionactivator.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.server.level.ServerPlayer;
import net.qiyanamark.companionactivator.server.ActivationHandler;

import java.util.function.Supplier;

public class ActivateEquippedTemporalsC2SPacket {
    public ActivateEquippedTemporalsC2SPacket() {}

    public static void encode(ActivateEquippedTemporalsC2SPacket pkt, FriendlyByteBuf buf) {

    }

    public static ActivateEquippedTemporalsC2SPacket decode(FriendlyByteBuf buf) {
        return new ActivateEquippedTemporalsC2SPacket();
    }

    public static void handle(ActivateEquippedTemporalsC2SPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        ServerPlayer sp = c.getSender();
        c.enqueueWork(() -> {
            if (sp == null) return;
            ActivationHandler.activateAllEquippedTemporals(sp);
        });
        c.setPacketHandled(true);
    }
}
