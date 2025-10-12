package net.qiyanamark.companionpouch.network;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkEvent;
import net.qiyanamark.companionpouch.capability.IDataPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogCapability;
import net.qiyanamark.companionpouch.catalog.CatalogNetwork;
import net.qiyanamark.companionpouch.util.IByteBufEnum;
import net.qiyanamark.companionpouch.util.Structs;

public class PacketRequestActionTemporal {
    public enum Actions implements IByteBufEnum<Actions> {
        ACTIVATE,
        SET_SELECTED;

        public void sendToServer(byte index) {
            CatalogNetwork.sendToServer(new PacketRequestActionTemporal(this, index));
        }
    }

    private final Actions action;
    private final byte companionIndex;

    private PacketRequestActionTemporal(Actions action, byte index) {
        this.action = action;
        this.companionIndex = index;
    }

    public void encode(FriendlyByteBuf buf) {
        this.action.writeByte(buf);
        buf.writeByte(this.companionIndex);
    }

    public static PacketRequestActionTemporal decode(FriendlyByteBuf buf) {
        Actions action = IByteBufEnum.readByte(Actions.class, buf);
        byte index = buf.readByte();

        return new PacketRequestActionTemporal(action, index);
    }

    public static void handle(PacketRequestActionTemporal request, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sPlayer = Objects.requireNonNull(ctx.getSender());
            byte companionIndex = request.companionIndex;

            Optional<Pair<Structs.LocationPouch, ItemStack>> pouchLocationMaybe = Structs.LocationPouch.findOnPlayer(sPlayer);
            pouchLocationMaybe.ifPresent(pair -> {
                IDataPouchCompanion pouchData = pair.getSecond()
                    .getCapability(CatalogCapability.COMPANION_POUCH_CAPABILITY)
                    .orElseThrow(IllegalStateException::new);

                switch (request.action) {
                    case ACTIVATE -> pouchData.tryActivateTemporal(companionIndex, sPlayer);
                    case SET_SELECTED -> pouchData.setActivationIndex(companionIndex);
                }
            });
        });

        ctx.setPacketHandled(true);
    }
}
