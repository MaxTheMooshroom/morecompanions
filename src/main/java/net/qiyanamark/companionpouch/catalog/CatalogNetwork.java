package net.qiyanamark.companionpouch.catalog;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.network.PacketRequestActivationTemporal;
import net.qiyanamark.companionpouch.network.PacketRequestOpenInterfacePouch;
import net.qiyanamark.companionpouch.network.PacketRequestOpenInventoryPouch;

public class CatalogNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        ModCompanionPouch.rel("network"),
        CatalogNetwork::getNetworkProtocolVersion,
        CatalogNetwork::canEncodeProtocolVersion,
        CatalogNetwork::canDecodeProtocolVersion
    );

    public static void register() {
        registerMessage("1", PacketRequestOpenInterfacePouch.class, CatalogNetwork::nopEncode, CatalogNetwork.nopDecode(PacketRequestOpenInterfacePouch.INSTANCE), PacketRequestOpenInterfacePouch::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage("1", PacketRequestOpenInventoryPouch.class, CatalogNetwork::nopEncode, CatalogNetwork.nopDecode(PacketRequestOpenInventoryPouch.INSTANCE), PacketRequestOpenInventoryPouch::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        registerMessage("1", PacketRequestActivationTemporal.class, PacketRequestActivationTemporal::encode, PacketRequestActivationTemporal::decode, PacketRequestActivationTemporal::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static <MSG> void registerMessage(String protocolVersion, Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        CatalogNetwork.CHANNEL.registerMessage(next(), messageType, encoder, decoder, messageConsumer);
    }

    public static <MSG> void registerMessage(String protocolVersion, Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer, final Optional<NetworkDirection> networkDirection) {
        CatalogNetwork.CHANNEL.registerMessage(next(), messageType, encoder, decoder, messageConsumer, networkDirection);
    }

    public static <MSG> void sendToServer(MSG msg) {
        CatalogNetwork.CHANNEL.sendToServer(msg);
    }

    public static <MSG> void sendTo(MSG message, Connection manager, NetworkDirection direction) {
        CatalogNetwork.CHANNEL.sendTo(message, manager, direction);
    }

    private static int id_count = 0;
    
    private static final String VERSION = "1";
    private static final String[] VERSION_ENCODE_SUPPORTED = new String[] { "1" };
    private static final String[] VERSION_DECODE_SUPPORTED = new String[] { "1" };

    private static int next() {
        return id_count++;
    }

    private static String getNetworkProtocolVersion() {
        return VERSION;
    }

    private static boolean canEncodeProtocolVersion(String version) {
        return Arrays.asList(VERSION_ENCODE_SUPPORTED).contains(version);
    }

    private static boolean canDecodeProtocolVersion(String version) {
        return Arrays.asList(VERSION_DECODE_SUPPORTED).contains(version);
    }

    private static <T> void nopEncode(T packet, FriendlyByteBuf buf) {}
    private static <T> Function<FriendlyByteBuf, T> nopDecode(T instance) { return buf -> instance; }
}
