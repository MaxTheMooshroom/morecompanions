package net.qiyanamark.companionpouch.network;

import java.util.function.Supplier;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkEvent;
import net.qiyanamark.companionpouch.catalog.CatalogNetwork;
import net.qiyanamark.companionpouch.menu.container.server.ServerMenuBase;

public enum PacketRequestClientMenuSync {
    INSTANCE;

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ServerPlayer sPlayer = ctx.getSender();

        if (sPlayer != null) {
            ctx.enqueueWork(() -> {
                if (sPlayer.containerMenu instanceof ServerMenuBase<?> menu) {
                    menu.broadcastFullState();
                }
            });
        }

        ctx.setPacketHandled(true);
    }

    public static void sendToServer() {
        CatalogNetwork.sendToServer(INSTANCE);
    }
}
