package net.qiyanamark.companionpouch.network;

import java.util.Optional;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.qiyanamark.companionpouch.capability.IDataPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogCapability;
import net.qiyanamark.companionpouch.catalog.CatalogNetwork;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import net.qiyanamark.companionpouch.menu.container.MenuInterfacePouchCompanion;
import net.qiyanamark.companionpouch.util.Structs;

public enum PacketRequestOpenInterfacePouch {
    INSTANCE;

    public static void handle(PacketRequestOpenInterfacePouch packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sPlayer = ctx.getSender();
            if (sPlayer == null) {
                return;
            }

            Optional<Pair<Structs.LocationPouch, ItemStack>> locationMaybe = Structs.LocationPouch.findOnPlayer(sPlayer);
            if (locationMaybe.isEmpty() || locationMaybe.get().getSecond().isEmpty()) {
                return;
            }

            ItemStack equippedPouch = locationMaybe.get().getSecond();
            int slotCount = equippedPouch.getCapability(CatalogCapability.COMPANION_POUCH_CAPABILITY)
                .map(IDataPouchCompanion::getSlots)
                .orElse(ItemPouchCompanion.DEFAULT_SLOT_COUNT);
            
            SimpleMenuProvider provider = MenuInterfacePouchCompanion.getProvider(equippedPouch);
            NetworkHooks.openGui(sPlayer, provider, buf -> {
                buf.writeByte(slotCount);
            });
        });

        ctx.setPacketHandled(true);
    }
    
    public static void sendToServer() {
        CatalogNetwork.sendToServer(INSTANCE);
    }
}
