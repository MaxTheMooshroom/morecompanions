package net.qiyanamark.companionpouch.network;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkEvent;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.item.CompanionItem;
import net.qiyanamark.companionpouch.capabilities.CapabilityTemporalIndex;
import net.qiyanamark.companionpouch.capabilities.ITemporalIndex;
import net.qiyanamark.companionpouch.catalog.CatalogNetwork;
import net.qiyanamark.companionpouch.helper.HelperCompanions;

public class PacketRequestActivationTemporal {
    private final int companionIndex;

    private PacketRequestActivationTemporal(int companionIndex) {
        this.companionIndex = companionIndex;
    }

    public static void encode(PacketRequestActivationTemporal packet, FriendlyByteBuf buf) {
        buf.writeByte(packet.companionIndex);
    }

    public static PacketRequestActivationTemporal decode(FriendlyByteBuf buf) {
        return new PacketRequestActivationTemporal(buf.readByte());
    }

    public static void handle(PacketRequestActivationTemporal request, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();

        ctx.enqueueWork(() -> {
            ServerPlayer sPlayer = ctx.getSender();
            int companionIndex = request.companionIndex;
            
            if (request.companionIndex == -1) {
                Optional<ItemStack> pouchStack = HelperCompanions.getCompanionPouch(sPlayer);
                if (pouchStack.isEmpty()) {
                    return;
                }

                ITemporalIndex cap = pouchStack.orElseThrow()
                    .getCapability(CapabilityTemporalIndex.TEMPORAL_INDEX_CAPABILITY)
                    .orElseThrow(IllegalStateException::new);

                companionIndex = cap.getIndex();
            }

            List<ItemStack> companions = HelperCompanions.getCompanions(sPlayer);

            if (companions.size() <= companionIndex) {
                return;
            }
            
            ServerLevel level = sPlayer.getLevel();
            Optional<Vault> vaultMaybe = VaultUtils.getVault(level);

            if (vaultMaybe.isEmpty()) {
                return;
            }

            Vault vault = vaultMaybe.get();
            ItemStack companionStack = companions.get(companionIndex);
            if (!CompanionItem.hasUsedTemporalIn(companionStack, vault.get(Vault.ID))) {
                CompanionItem.activateTemporalModifier(sPlayer, companionStack, vault);
            }
        });

        ctx.setPacketHandled(true);
    }
    
    public static void sendToServer(int index) {
        CatalogNetwork.sendToServer(new PacketRequestActivationTemporal(index));
    }
}
