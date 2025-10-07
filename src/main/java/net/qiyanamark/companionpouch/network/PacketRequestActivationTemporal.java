package net.qiyanamark.companionpouch.network;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkEvent;
import net.qiyanamark.companionpouch.catalog.CatalogCapability;
import net.qiyanamark.companionpouch.catalog.CatalogNetwork;
import net.qiyanamark.companionpouch.util.Structs;

public class PacketRequestActivationTemporal {
    private final byte companionIndex;

    private PacketRequestActivationTemporal(byte companionIndex) {
        this.companionIndex = companionIndex;
    }

    public static void encode(PacketRequestActivationTemporal packet, FriendlyByteBuf buf) {
        buf.writeByte(packet.companionIndex);
    }

    public static PacketRequestActivationTemporal decode(FriendlyByteBuf buf) {
        return new PacketRequestActivationTemporal(buf.readByte());
    }

    public static void handle(PacketRequestActivationTemporal request, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sPlayer = Objects.requireNonNull(ctx.getSender());
            byte companionIndex = request.companionIndex;
            sPlayer.sendMessage(new TextComponent("Requesting open temporal at index " + companionIndex), sPlayer.getUUID());

            Optional<Pair<Structs.LocationPouch, ItemStack>> pouchLocation = Structs.LocationPouch.findOnPlayer(sPlayer);
            pouchLocation.ifPresent(pair -> pair.getSecond()
                    .getCapability(CatalogCapability.COMPANION_POUCH_CAPABILITY)
                    .orElseThrow(IllegalStateException::new)
                    .tryActivateTemporal(companionIndex, sPlayer));
        });

        ctx.setPacketHandled(true);
    }
    
    public static void sendToServer(byte index) {
        CatalogNetwork.sendToServer(new PacketRequestActivationTemporal(index));
    }
}
