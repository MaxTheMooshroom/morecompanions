package net.qiyanamark.companionpouch.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkEvent;
import net.qiyanamark.companionpouch.catalog.CatalogNetwork;
import net.qiyanamark.companionpouch.util.Structs;

public class PacketRequestClientPouchSync {
    private final Structs.LocationPouch location;

    private PacketRequestClientPouchSync(Structs.LocationPouch location) {
        this.location = location;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.location);
    }

    public PacketRequestClientPouchSync decode(FriendlyByteBuf buf) {
        return new PacketRequestClientPouchSync(buf.readEnum(Structs.LocationPouch.class));
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ServerPlayer sPlayer = ctx.getSender();

        if (sPlayer != null) {
            ctx.enqueueWork(
                () -> this.location.getFromEntity(sPlayer)
                    .ifPresent(pouchStack -> PacketPerformClientSyncPouch.sentToClient(sPlayer, this.location, pouchStack))
            );
        }

        ctx.setPacketHandled(true);
    }

    public static void sendToServer(Structs.LocationPouch location) {
        CatalogNetwork.sendToServer(new PacketRequestClientPouchSync(location));
    }
}
