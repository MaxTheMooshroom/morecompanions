package net.qiyanamark.companionpouch.network;

import java.util.function.Supplier;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkEvent;
import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.catalog.CatalogNetwork;
import net.qiyanamark.companionpouch.util.Structs;

public class PacketPerformClientSyncPouch {
    private CompoundTag pouchTag;
    private Structs.LocationPouch location;

    private PacketPerformClientSyncPouch(ItemStack pouchStack, Structs.LocationPouch location) {
        this.pouchTag = pouchStack.getOrCreateTag();
        this.location = location;
    }

    private PacketPerformClientSyncPouch(CompoundTag pouchTag, Structs.LocationPouch location) {
        this.pouchTag = pouchTag;
        this.location = location;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(this.pouchTag);
        buf.writeEnum(this.location);
    }

    public static PacketPerformClientSyncPouch decode(FriendlyByteBuf buf) {
        CompoundTag pouchTag = buf.readNbt();
        Structs.LocationPouch location = buf.readEnum(Structs.LocationPouch.class);
        return new PacketPerformClientSyncPouch(pouchTag, location);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(
            () -> ModCompanionPouch.getClientPlayer()
                .flatMap(lPlayer -> this.location.getFromEntity(lPlayer))
                .ifPresent(pouchStack -> pouchStack.setTag(this.pouchTag))
        );

        ctx.setPacketHandled(true);
    }

    public static void sentToClient(ServerPlayer sPlayer, Structs.LocationPouch location, ItemStack pouchStack) {
        CatalogNetwork.sendToClient(sPlayer, new PacketPerformClientSyncPouch(pouchStack, location));
    }
}
