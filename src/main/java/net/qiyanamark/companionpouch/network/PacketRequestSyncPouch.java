package net.qiyanamark.companionpouch.network;

import java.util.Objects;
import java.util.function.Supplier;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.catalog.CatalogNetwork;
import net.qiyanamark.companionpouch.util.Structs.LocationPouch;

public class PacketRequestSyncPouch {
    private final LocationPouch location;
    private final CompoundTag data;

    private PacketRequestSyncPouch(LocationPouch location, CompoundTag pouchTag) {
        this.location = location;
        this.data = pouchTag;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.location);
        buf.writeNbt(this.data);
    }

    public static PacketRequestSyncPouch decode(FriendlyByteBuf buf) {
        LocationPouch location = buf.readEnum(LocationPouch.class);
        CompoundTag nbt = buf.readNbt();
        return new PacketRequestSyncPouch(location, nbt);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            LocalPlayer lPlayer = Objects.requireNonNull(ModCompanionPouch.getClientPlayer());
            ItemStack pouchStack = this.location.getFromEntity(lPlayer).orElseThrow();
            pouchStack.setTag(this.data);
        }));

        ctx.setPacketHandled(true);
    }

    public static void sendToPlayer(ServerPlayer sPlayer, LocationPouch location) {
        ItemStack pouchStack = location.getFromEntity(sPlayer).orElseThrow();
        CompoundTag nbt = pouchStack.getOrCreateTag();

        CatalogNetwork.sendToClient(sPlayer, new PacketRequestSyncPouch(location, nbt));
    }
}
