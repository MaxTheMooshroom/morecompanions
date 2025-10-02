package net.qiyanamark.companionpouch.network;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.qiyanamark.companionpouch.capability.CapabilityPouchCompanion;
import net.qiyanamark.companionpouch.capability.IDataPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogNetwork;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import net.qiyanamark.companionpouch.menu.container.MenuInventoryPouchCompanion;
import net.qiyanamark.companionpouch.util.Structs;

import java.util.Optional;
import java.util.function.Supplier;

public class PacketRequestOpenInventoryPouch {
    private static final PacketRequestOpenInventoryPouch INSTANCE = new PacketRequestOpenInventoryPouch();

    public static void encode(PacketRequestOpenInventoryPouch packet, FriendlyByteBuf buf) {}
    public static PacketRequestOpenInventoryPouch decode(FriendlyByteBuf buf) { return PacketRequestOpenInventoryPouch.INSTANCE; }

    private PacketRequestOpenInventoryPouch() {}

    public static void handle(PacketRequestOpenInventoryPouch packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            Optional<ServerPlayer> sPlayerMaybe = Optional.ofNullable(ctx.getSender());
            if (sPlayerMaybe.isEmpty()) {
                return;
            }
            ServerPlayer sPlayer = sPlayerMaybe.orElseThrow();

            Optional<Pair<Structs.LocationPouch, ItemStack>> locationMaybe = Structs.LocationPouch.findOnPlayer(sPlayer);
            if (locationMaybe.isEmpty()) {
                return;
            }

            Structs.LocationPouch location = locationMaybe.get().getFirst();
            ItemStack equippedPouch = locationMaybe.get().getSecond();

            int slotCount = equippedPouch.getCapability(CapabilityPouchCompanion.COMPANION_POUCH_CAPABILITY)
                .map(IDataPouchCompanion::getSlots)
                .orElse(ItemPouchCompanion.DEFAULT_SLOT_COUNT);

            SimpleMenuProvider provider = MenuInventoryPouchCompanion.getProvider(equippedPouch, slotCount);
            NetworkHooks.openGui(sPlayer, provider, buf -> {
                location.writeByte(buf);
                buf.writeByte(slotCount);
            });
        });

        ctx.setPacketHandled(true);
    }
    
    public static void sendToServer() {
        CatalogNetwork.sendToServer(PacketRequestOpenInventoryPouch.INSTANCE);
    }
}
