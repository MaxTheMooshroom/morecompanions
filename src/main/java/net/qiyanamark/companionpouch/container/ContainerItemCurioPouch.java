package net.qiyanamark.companionpouch.container;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import iskallia.vault.core.vault.VaultUtils;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.CompanionItem;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;
import net.qiyanamark.companionpouch.menu.container.MenuInterfacePouchCompanion;
import net.qiyanamark.companionpouch.network.PacketRequestActionTemporal;
import net.qiyanamark.companionpouch.util.Structs;
import net.qiyanamark.companionpouch.util.annotations.Extends;
import net.qiyanamark.companionpouch.util.annotations.Implements;

public class ContainerItemCurioPouch extends ItemStackHandler {
    public static final String STORAGE_KEY = "storage";
    public static final String ACTIVATE_KEY = "activate";

    private final Player player;
    private final Structs.LocationPouch pouchLocation;
    private final ItemStack pouchStack;

    private int curseStacks = -1;
    private boolean isDirty = true;

    public ContainerItemCurioPouch(@NotNull Player player, @NotNull Structs.LocationPouch pouchLocation, @NotNull ItemStack pouchStack) {
        super(ItemPouchCompanion.DEFAULT_SLOT_COUNT);
        this.player = player;
        this.pouchLocation = pouchLocation;
        this.pouchStack = pouchStack;
        this.pouchStack.getOrCreateTag();

        this.load();
    }

    public ContainerItemCurioPouch(@NotNull Player player, @NotNull Structs.LocationPouch pouchLocation) {
        this(player, pouchLocation, pouchLocation.getFromEntity(player).orElseThrow());
    }

    private ContainerItemCurioPouch(@NotNull Player player, Pair<Structs.LocationPouch, ItemStack> pair) {
        this(player, pair.getFirst(), pair.getSecond());
    }

    public ContainerItemCurioPouch(@NotNull Player player) {
        this(player, Structs.LocationPouch.findOnPlayer(player).orElseThrow());
    }

    public ItemStack getPouchStack() {
        return this.pouchStack;
    }

    public Structs.LocationPouch getPouchLocation() {
        return this.pouchLocation;
    }

    private void calculateCurseStacks() {
        Set<ResourceLocation> temporals = new HashSet<>();
        Set<ResourceLocation> modifiers = new HashSet<>();

        Set<ResourceLocation> perCompanionModifiers = new HashSet<>();

        this.curseStacks = 0;

        IntStream.range(0, this.getSlots())
            .mapToObj(this::getStackInSlot)
            .forEach(companionStack -> {
                if (CompanionItem.getCompanionHearts(companionStack) <= 0) {
                    return;
                }

                CompanionItem.getTemporalModifier(companionStack).ifPresent(rel -> {
                    if (temporals.contains(rel)) {
                        this.curseStacks++;
                    } else {
                        temporals.add(rel);
                    }
                });

                CompanionItem.getAllRelics(companionStack).values().stream()
                    .map(Pair::getSecond)
                    .forEach(perCompanionModifiers::addAll);

                perCompanionModifiers.stream()
                    .filter(rel -> !rel.getPath().equals("companion_challenge"))
                    .forEach(rel -> {
                        if (modifiers.contains(rel)) {
                            this.curseStacks += 2;
                        } else {
                            modifiers.add(rel);
                        }
                    });
            });
    }

    public Stream<ItemStack> getCompanions() {
        return this.stacks.stream().filter(stack -> !stack.isEmpty()).onClose(this::save);
    }

    public int getCurseStacks() {
        if (this.isDirty) {
            this.calculateCurseStacks();
            this.isDirty = false;
        }

        return this.curseStacks;
    }

    public void markDirty() {
        this.isDirty = true;
    }

    @Override
    @Extends(ItemStackHandler.class)
    protected void onContentsChanged(int index) {
        this.markDirty();
        this.save();
    }

    public byte getActivationIndex() {
        if (!(this.player.containerMenu instanceof MenuInterfacePouchCompanion menu)) {
            return 0;
        }

        return menu.getActivationIndex();
    }

    public void setActivationIndex(byte index) {
        if (index < 0 && index >= this.getSlots()) {
            return;
        }

        if (this.player.containerMenu instanceof MenuInterfacePouchCompanion menu) {
            if (this.player instanceof LocalPlayer) {
                PacketRequestActionTemporal.Actions.SET_SELECTED.sendToServer(index);
            } else if (this.player instanceof ServerPlayer) {
                menu.setActivationIndex(index);
            }
        }
    }

    public void tryActivateTemporal(byte index) {
        if (index < 0 && index >= this.getSlots()) {
            return;
        }

        VaultUtils.getVault(player.level).ifPresent(
            vault -> {
                if (this.player instanceof LocalPlayer) {
                    PacketRequestActionTemporal.Actions.ACTIVATE.sendToServer(index);
                } else if (this.player instanceof ServerPlayer sPlayer) {
                    if (HelperCompanions.companionCanUseTemporalInVault(this.pouchStack, vault)) {
                        CompanionItem.activateTemporalModifier(sPlayer, this.pouchStack, vault);
                        if (this.player.containerMenu instanceof MenuInterfacePouchCompanion menu) {
                            menu.setCompanionTemporalUsed(index, true);
                        }
                    }
                }
            }
        );
    }

    @Override
    @Implements(value = IItemHandler.class, introducedBy = ItemStackHandler.class)
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return stack.is(ModItems.COMPANION);
    }

    public void save() {
        if (this.player instanceof LocalPlayer) {
            return;
        }

        CompoundTag pouchTag = this.pouchTag();

        pouchTag.put(STORAGE_KEY, super.serializeNBT());
        pouchTag.putByte(ACTIVATE_KEY, this.getActivationIndex());

        this.pouchStack.setTag(pouchTag);
    }

    private void load() {
        CompoundTag pouchTag = this.pouchTag();
        super.deserializeNBT(pouchTag.getCompound(STORAGE_KEY));
    }

    @SuppressWarnings("ConstantConditions")
    private @NotNull CompoundTag pouchTag() {
        return this.pouchStack.getTag();
    }
}
