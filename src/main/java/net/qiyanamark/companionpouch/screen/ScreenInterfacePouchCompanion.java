package net.qiyanamark.companionpouch.screen;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.qiyanamark.companionpouch.capabilities.CapabilityTemporalIndex;
import net.qiyanamark.companionpouch.capabilities.ITemporalIndex;
import net.qiyanamark.companionpouch.catalog.CatalogMenu;
import net.qiyanamark.companionpouch.helper.HelperCompanions;
import net.qiyanamark.companionpouch.menu.container.MenuInterfacePouchCompanion;
import net.qiyanamark.companionpouch.network.PacketRequestActivationTemporal;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;
import net.qiyanamark.companionpouch.util.annotations.Extends;

public class ScreenInterfacePouchCompanion extends AbstractContainerScreen<MenuInterfacePouchCompanion> {
    private final int slotCount;
    private final int toggleIndex;
    private boolean[] activatorsEnabled;

    public ScreenInterfacePouchCompanion(MenuInterfacePouchCompanion menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageHeight = 163;
        this.slotCount = this.menu.getSlotCount();
        this.activatorsEnabled = new boolean[this.slotCount];

        ItemStack pouchStack = this.menu.getPouchStack();
        ITemporalIndex temporalCap = pouchStack.getCapability(CapabilityTemporalIndex.TEMPORAL_INDEX_CAPABILITY).orElseThrow(IllegalStateException::new);
        
        this.toggleIndex = temporalCap.getIndex();

        LocalPlayer lPlayer = Minecraft.getInstance().player;
        Optional<Vault> vaultMaybe = VaultUtils.getVault(lPlayer.level);

        if (vaultMaybe.isEmpty()) {
            Minecraft.getInstance().setScreen(null);
            return;
        }

        Vault vault = vaultMaybe.get();
        int activationY = CatalogMenu.MENU_INTERFACE_SLOT_PADDING_LEFT_TOP.y() + CatalogMenu.MENU_INTERFACE_SLOT_ACTIVATE_OFFSET.y();
        int toggleY = 0;
        for (int i = 0; i < this.slotCount; i++) {
            Slot slot = this.menu.getSlot(i);
            this.activatorsEnabled[i] = HelperCompanions.companionCanUseTemporalInVault(slot.getItem(), vault);
            
            // temporal-activation buttons
            int activationX = slot.x + CatalogMenu.MENU_INTERFACE_SLOT_ACTIVATE_OFFSET.x();
            this.addWidget(new ActivatorButton(
                i, activationX, activationY,
                new TextComponent("test1")
            ));

            // hotkey-toggle buttons
            int toggleX = 0;
            this.addWidget(new ToggleButton(
                temporalCap, i,
                toggleX, toggleY,
                new TextComponent("test1")
            ));
        }
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    protected void renderBgChrome(PoseStack poseStack) {
        CatalogMenu.SCREEN_INTERFACE_CHROME.bind();
        CatalogMenu.SCREEN_INTERFACE_CHROME.blit(poseStack, this.leftPos, this.topPos);
    }

    protected void renderBgSlots(PoseStack poseStack, List<Pair<Integer, Vec2i>> slotPositions) {
        CatalogMenu.MENU_SLOT.bindFor(
            poseStack,
            ctx -> {
                slotPositions.stream()
                    .map(pair -> pair.getSecond())
                    .forEach(pos -> {
                        CatalogMenu.MENU_SLOT.blit(ctx, this.leftPos + pos.x(), this.topPos + pos.y());
                    });
            }
        );
    }

    protected void renderButtons(PoseStack poseStack, List<Pair<Integer, Vec2i>> slotPositions) {
        CatalogMenu.ACTIVATE_READY.bindFor(poseStack, ctx -> {
            slotPositions.stream()
                .filter(pair -> this.activatorsEnabled[pair.getFirst()])
                .map(pair -> pair.getSecond())
                .forEach(pos -> {
                    CatalogMenu.ACTIVATE_READY.blit(
                        ctx,
                        this.leftPos + pos.x() + CatalogMenu.MENU_INTERFACE_SLOT_ACTIVATE_OFFSET.x(),
                        this.topPos + pos.y() + CatalogMenu.MENU_INTERFACE_SLOT_ACTIVATE_OFFSET.y()
                    );
                });
        });

        CatalogMenu.ACTIVATE_RESTING.bindFor(poseStack, ctx -> {
            slotPositions.stream()
                .filter(pair -> !this.activatorsEnabled[pair.getFirst()])
                .map(pair -> pair.getSecond())
                .forEach(pos -> {
                    CatalogMenu.ACTIVATE_RESTING.blit(
                        ctx,
                        this.leftPos + pos.x() + CatalogMenu.MENU_INTERFACE_SLOT_ACTIVATE_OFFSET.x(),
                        this.topPos + pos.y() + CatalogMenu.MENU_INTERFACE_SLOT_ACTIVATE_OFFSET.y()
                    );
                });
        });

        CatalogMenu.TOGGLE_OFF.bindFor(poseStack, ctx -> slotPositions.stream()
            .filter(pair -> pair.getFirst() != this.toggleIndex)
            .map(pair -> pair.getSecond())
            .forEach(pos -> {
                CatalogMenu.TOGGLE_OFF.blit(
                    ctx,
                    this.leftPos + pos.x() + CatalogMenu.MENU_INTERFACE_SLOT_TOGGLE_OFFSET.x(),
                    this.topPos + pos.y() + CatalogMenu.MENU_INTERFACE_SLOT_TOGGLE_OFFSET.y()
                );
            })
        );

        Vec2i pos = slotPositions.stream().filter(p -> p.getFirst() == this.toggleIndex).map(pair -> pair.getSecond()).findFirst().get();
        CatalogMenu.TOGGLE_ON.bind();
        CatalogMenu.TOGGLE_ON.blit(
            poseStack,
            this.leftPos + pos.x() + CatalogMenu.MENU_INTERFACE_SLOT_TOGGLE_OFFSET.x(),
            this.topPos + pos.y() + CatalogMenu.MENU_INTERFACE_SLOT_TOGGLE_OFFSET.y()
        );
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    protected void renderBg(PoseStack poseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        List<Pair<Integer, Vec2i>> slotPositions = IntStream.range(0, this.slotCount)
            .mapToObj(i -> {
                Slot slot = this.menu.slots.get(i);
                return new Pair<Integer, Vec2i>(i, new Vec2i(slot.x, slot.y));
            })
            .collect(Collectors.toList());

        this.renderBgChrome(poseStack);
        this.renderBgSlots(poseStack, slotPositions);
        this.renderButtons(poseStack, slotPositions);
    }

    private static abstract class IndexedButton extends Button {
        protected int index;

        public IndexedButton(int index, int pX, int pY, int pWidth, int pHeight, Component pMessage, Button.OnPress pOnPress, Button.OnTooltip pOnTooltip) {
            super(pX, pY, pWidth, pHeight, pMessage, null, pOnTooltip);
            this.index = index;
        }

        @Override
        @Extends(Button.class)
        public abstract void onPress();
    }

    private static class ToggleButton extends IndexedButton {
        private final ITemporalIndex cap;

        public ToggleButton(ITemporalIndex cap, int index, int pX, int pY, Component pMessage) {
            this(cap, index, pX, pY, pMessage, Button.NO_TOOLTIP);
        }

        public ToggleButton(ITemporalIndex cap, int index, int pX, int pY, Component pMessage, Button.OnTooltip pOnTooltip) {
            super(index, pX, pY, CatalogMenu.ACTIVATE_READY.getSize().x(), CatalogMenu.ACTIVATE_READY.getSize().y(), pMessage, null, pOnTooltip);
            this.cap = cap;
        }

        @Override
        @Extends(Button.class)
        public void onPress() {
            this.cap.setIndex(this.index);
        }
    }

    private static class ActivatorButton extends IndexedButton {
        public ActivatorButton(int index, int pX, int pY, Component pMessage) {
            this(index, pX, pY, pMessage, Button.NO_TOOLTIP);
        }

        public ActivatorButton(int index, int pX, int pY, Component pMessage, Button.OnTooltip pOnTooltip) {
            super(index, pX, pY, CatalogMenu.ACTIVATE_READY.getSize().x(), CatalogMenu.ACTIVATE_READY.getSize().y(), pMessage, null, pOnTooltip);
        }

        @Override
        @Extends(Button.class)
        public void onPress() {
            PacketRequestActivationTemporal.sendToServer(this.index);
        }
    }
}
