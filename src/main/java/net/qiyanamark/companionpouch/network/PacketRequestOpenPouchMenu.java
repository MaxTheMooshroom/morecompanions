package net.qiyanamark.companionpouch.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.qiyanamark.companionpouch.catalog.CatalogNetwork;
import net.qiyanamark.companionpouch.menu.container.MenuInterfacePouchCompanion;
import net.qiyanamark.companionpouch.menu.container.MenuInventoryPouchCompanion;
import net.qiyanamark.companionpouch.util.Structs;

public enum PacketRequestOpenPouchMenu {
    INVENTORY,
    INTERFACE;

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this);
    }

    public static PacketRequestOpenPouchMenu decode(FriendlyByteBuf buf) {
        return buf.readEnum(PacketRequestOpenPouchMenu.class);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ServerPlayer sPlayer = ctx.getSender();

        if (sPlayer != null) {
            ctx.enqueueWork(() -> {
                SimpleMenuProvider provider = switch (this) {
                    case INTERFACE -> MenuInterfacePouchCompanion.getProviderFromEquippedCurio();
                    case INVENTORY -> MenuInventoryPouchCompanion.getProviderFromEquippedCurio();
                };
                NetworkHooks.openGui(sPlayer, provider, buf -> buf.writeEnum(Structs.LocationPouch.CURIO));
            });
        }

        ctx.setPacketHandled(true);
    }
    
    public void sendToServer() {
        CatalogNetwork.sendToServer(this);
    }
}
