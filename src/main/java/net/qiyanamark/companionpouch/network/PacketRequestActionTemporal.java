package net.qiyanamark.companionpouch.network;

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.network.NetworkEvent;
import net.qiyanamark.companionpouch.catalog.CatalogNetwork;
import net.qiyanamark.companionpouch.container.ContainerItemCurioPouch;

public class PacketRequestActionTemporal {
    public enum Actions {
        ACTIVATE,
        SET_SELECTED;

        public void sendToServer(byte index) {
            CatalogNetwork.sendToServer(new PacketRequestActionTemporal(this, index));
        }
    }

    public static final Actions ACTIVATE = Actions.ACTIVATE;
    public static final Actions SET_SELECTED = Actions.SET_SELECTED;

    private final Actions action;
    private final byte companionIndex;

    private PacketRequestActionTemporal(Actions action, byte index) {
        this.action = action;
        this.companionIndex = index;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.action);
        buf.writeByte(this.companionIndex);
    }

    public static PacketRequestActionTemporal decode(FriendlyByteBuf buf) {
        Actions action = buf.readEnum(Actions.class);
        byte index = buf.readByte();

        return new PacketRequestActionTemporal(action, index);
    }

    public static void handle(PacketRequestActionTemporal request, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sPlayer = Objects.requireNonNull(ctx.getSender());
            byte companionIndex = request.companionIndex;

            ContainerItemCurioPouch container = new ContainerItemCurioPouch(sPlayer);
            switch (request.action) {
                case ACTIVATE -> container.tryActivateTemporal(companionIndex);
                case SET_SELECTED -> container.setActivationIndex(companionIndex);
            }
            container.save();
        });

        ctx.setPacketHandled(true);
    }
}
