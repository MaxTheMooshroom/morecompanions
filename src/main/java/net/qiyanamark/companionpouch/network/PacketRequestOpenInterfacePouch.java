package net.qiyanamark.companionpouch.network;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.capabilities.CapabilityDataPouchCompanion;
import net.qiyanamark.companionpouch.capabilities.IDataPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogNetwork;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import net.qiyanamark.companionpouch.menu.container.MenuInterfacePouchCompanion;
import net.qiyanamark.companionpouch.util.Structs;
import org.jetbrains.annotations.Contract;

public class PacketRequestOpenInterfacePouch {
    @SuppressWarnings("UtilityClassWithoutPrivateConstructor")
    private static final PacketRequestOpenInterfacePouch INSTANCE = new PacketRequestOpenInterfacePouch();

    public static void encode(PacketRequestOpenInterfacePouch packet, FriendlyByteBuf buf) {}
    public static PacketRequestOpenInterfacePouch decode(FriendlyByteBuf buf) { return PacketRequestOpenInterfacePouch.INSTANCE; }

    private PacketRequestOpenInterfacePouch() {}

    public static void handle(PacketRequestOpenInterfacePouch packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            Optional<ServerPlayer> sPlayer = Optional.ofNullable(ctx.getSender());
            if (sPlayer.isEmpty()) {
                return;
            }
            Optional<ItemStack> equippedPouchMaybe = HelperCompanions.getCompanionPouch(sPlayer.orElseThrow());

            if (equippedPouchMaybe.isEmpty() || equippedPouchMaybe.get().isEmpty()) {
                return;
            }

            ItemStack equippedPouch = equippedPouchMaybe.get();
            byte slotCount = (byte) equippedPouch.getCapability(CapabilityDataPouchCompanion.COMPANION_POUCH_CAPABILITY)
                .map(IDataPouchCompanion::getSize)
                .orElse(ItemPouchCompanion.DEFAULT_SLOT_COUNT)
                .intValue();
            
            SimpleMenuProvider provider = MenuInterfacePouchCompanion.getProvider(equippedPouch, Structs.InstanceSide.from(sPlayer.get()));
            NetworkHooks.openGui(sPlayer.orElseThrow(), provider, buf -> {
                buf.writeByte(slotCount);
            });
        });

        ctx.setPacketHandled(true);
    }
    
    public static void sendToServer() {
        CatalogNetwork.sendToServer(PacketRequestOpenInterfacePouch.INSTANCE);
    }
}
