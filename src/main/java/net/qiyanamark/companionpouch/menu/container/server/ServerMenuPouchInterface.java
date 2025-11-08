package net.qiyanamark.companionpouch.menu.container.server;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.IntStream;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import net.qiyanamark.companionpouch.menu.container.common.MenuSlotDataIndexed;
import net.qiyanamark.companionpouch.util.Structs;

import static net.minecraft.Util.NIL_UUID;
import static net.qiyanamark.companionpouch.catalog.CatalogMenu.COMPANION_POUCH_INTERFACE_CLIENT;

public class ServerMenuPouchInterface extends ServerMenuPouchBase<ServerMenuPouchInterface> {
    public static final ResourceLocation REL = ModCompanionPouch.rel("menu.container.server.interface");
    public static final Component SCREEN_I18N = new TranslatableComponent("screen.companionpouch.interface_pouch_companion");

    @SuppressWarnings("unchecked")
    private final @NotNull MenuSlotDataIndexed<Boolean>[] temporalAvailable = (MenuSlotDataIndexed<Boolean>[]) new MenuSlotDataIndexed<?>[ItemPouchCompanion.MAX_SLOT_COUNT];

    public ServerMenuPouchInterface(int id, Inventory inv, Structs.LocationPouch location) {
        // lie about the menu type so when NetworkHooks.openGui is called, the client is
        // told to open the menu, it receives the client-side version of the menu.
        super(COMPANION_POUCH_INTERFACE_CLIENT, id, inv, location);

        Optional<Vault> vaultMaybe = VaultUtils.getVault(this.sPlayer.level);
        IntStream.range(0, ItemPouchCompanion.MAX_SLOT_COUNT).forEach(
            i -> this.temporalAvailable[i].set(HelperCompanions.companionCanUseTemporalInVault(
                this.pouchContainer.getStackInSlot(i),
                vaultMaybe
            ))
        );
    }

    public static SimpleMenuProvider fromLocation(Structs.LocationPouch location) {
        return new SimpleMenuProvider(
            (id, inv, player) -> new ServerMenuPouchInventory(id, inv, location),
            SCREEN_I18N
        );
    }

    public boolean getTemporalAvailable(int index) {
        return this.temporalAvailable[index].get();
    }

    public void tryActivateTemporal(byte index) {
        if (index == -1) {
            index = this.activationIndex.get();
        }

        this.pouchContainer.player.sendMessage(new TextComponent("server menu tried activating temporal"), NIL_UUID);
        this.pouchContainer.tryActivateTemporal(index);
        this.setCompanionTemporalAvailable(index, true);
    }

    public void setCompanionTemporalAvailable(byte index, boolean value) {
        this.pouchContainer.player.sendMessage(new TextComponent("updating server menu"), NIL_UUID);
        this.temporalAvailable[index].set(value);
        this.broadcastChanges();
    }
}
