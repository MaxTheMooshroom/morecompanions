package net.qiyanamark.companionpouch.network;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import net.qiyanamark.companionpouch.capabilities.CapabilitiesPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogNetwork;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.menu.container.MenuInterfacePouchCompanion;

public class PacketRequestOpenInterfacePouch {
    private static PacketRequestOpenInterfacePouch INSTANCE = new PacketRequestOpenInterfacePouch();

    public static void encode(PacketRequestOpenInterfacePouch packet, FriendlyByteBuf buf) {}
    public static PacketRequestOpenInterfacePouch decode(FriendlyByteBuf buf) { return PacketRequestOpenInterfacePouch.INSTANCE; }

    public static void handle(PacketRequestOpenInterfacePouch packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sPlayer = ctx.getSender();
            Optional<ItemStack> equippedPouchMaybe = HelperCompanions.getCompanionPouch(sPlayer);

            if (equippedPouchMaybe.isEmpty()) {
                ctx.setPacketHandled(true);
                return;
            }

            sPlayer.sendMessage(new TextComponent("request received"), sPlayer.getUUID());

            // TODO replace this placeholder
            String containerI18n = "screen.companionpouch.interface_pouch_companion";

            ItemStack equippedPouch = equippedPouchMaybe.get();
            byte slotCount = CapabilitiesPouchCompanion.getSizeOrDefault(equippedPouch.getOrCreateTag());
            
            SimpleMenuProvider provider = MenuInterfacePouchCompanion.getProvider(equippedPouch, containerI18n);
            NetworkHooks.openGui(sPlayer, provider, buf -> {
                buf.writeByte(slotCount);
                buf.writeUUID(sPlayer.getUUID());
            });
        });

        ctx.setPacketHandled(true);
    }
    
    public static void sendToServer() {
        CatalogNetwork.sendToServer(PacketRequestOpenInterfacePouch.INSTANCE);
    }
}
