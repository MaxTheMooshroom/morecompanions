package net.qiyanamark.companionpouch.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.qiyanamark.companionpouch.capabilities.CapabilityDataPouchCompanion;
import net.qiyanamark.companionpouch.capabilities.IDataPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogMenu;
import net.qiyanamark.companionpouch.menu.container.MenuInterfacePouchCompanion;
import net.qiyanamark.companionpouch.network.PacketRequestActivationTemporal;
import net.qiyanamark.companionpouch.screen.ScreenInterfacePouchCompanion.ToggleButton.OnTooltip;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;
import net.qiyanamark.companionpouch.util.annotations.Extends;

public class ScreenInterfacePouchCompanion extends AbstractContainerScreen<MenuInterfacePouchCompanion> {
    private final IDataPouchCompanion pouchCap;

    private List<Pair<Integer, Vec2i>> slotPositions = new ArrayList<>();

    private boolean hoverEnabled = true;

    private static Predicate<Pair<Integer, IDataPouchCompanion>> TOGGLER_IS_ON = state -> state.getFirst() == state.getSecond().getActivationIndex();
    private static Predicate<Pair<Integer, Boolean>> ACTIVATOR_IS_ON = state -> state.getSecond();

    public ScreenInterfacePouchCompanion(MenuInterfacePouchCompanion menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = CatalogMenu.SCREEN_INTERFACE_CHROME.getSize().x();
        this.imageHeight = CatalogMenu.SCREEN_INTERFACE_CHROME.getSize().y();
        
        ItemStack pouchStack = this.menu.getPouchStack();
        this.pouchCap = pouchStack.getCapability(CapabilityDataPouchCompanion.COMPANION_POUCH_CAPABILITY).orElseThrow(IllegalStateException::new);
        int size = this.pouchCap.getSize();

        LocalPlayer lPlayer = Minecraft.getInstance().player;
        Optional<Vault> vaultMaybe = VaultUtils.getVault(lPlayer.level);
        
        lPlayer.sendMessage(new TextComponent("slotCount: " + size), lPlayer.getUUID());

        if (vaultMaybe.isEmpty()) {
            lPlayer.sendMessage(new TextComponent("not in a vault"), lPlayer.getUUID());
            // Minecraft.getInstance().setScreen(null);
            // return;
        }

        // Vault vault = vaultMaybe.get();
        for (int i = 0; i < size; i++) {
            Slot slot = this.menu.getSlot(i);
            Vec2i pos = new Vec2i(this.leftPos + slot.x, this.topPos + slot.y);

            // activators
            Vec2i activatorPos = CatalogMenu.MENU_INTERFACE_SLOT_ACTIVATE_OFFSET.add(pos);
            this.addRenderableWidget(new ToggleButton<>(
                activatorPos.x(), activatorPos.y(),
                new Pair<Integer, Boolean>(i, true),
                ACTIVATOR_STATE_0, ACTIVATOR_STATE_1, ACTIVATOR_IS_ON
            ));

            // togglers
            Vec2i togglePos = CatalogMenu.MENU_INTERFACE_SLOT_TOGGLE_OFFSET.add(pos);
            this.addRenderableWidget(new ToggleButton<>(
                togglePos.x(), togglePos.y(),
                new Pair<Integer, IDataPouchCompanion>(i, this.pouchCap),
                TOGGLER_INDEX_0, TOGGLER_INDEX_1, TOGGLER_IS_ON
            ));

            this.slotPositions.add(new Pair<>(i, pos));
        }
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        return this.hoverEnabled && super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        this.hoverEnabled = false;
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.hoverEnabled = true;
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    protected void renderChrome(PoseStack poseStack) {
        CatalogMenu.SCREEN_INTERFACE_CHROME.blitSlow(poseStack, this.leftPos, this.topPos);
    }

    protected void renderSlots(PoseStack poseStack) {
        this.menu.slots.forEach(slot -> CatalogMenu.MENU_SLOT.blitSlow(poseStack, this.leftPos + slot.x - 1, this.topPos + slot.y - 1));
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    protected void renderBg(PoseStack poseStack, float pPartialTick, int pMouseX, int pMouseY) {
        CatalogMenu.TEXTURE_ATLAS_MENUS_POUCH.prepareSlow();

        this.renderChrome(poseStack);
        this.renderSlots(poseStack);
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // don't render labels
    }

    private static void onActivatorClick(ToggleButton<Pair<Integer, Boolean>> button) {
        Pair<Integer, Boolean> pair = button.getState();
        int idx = pair.getFirst();
        PacketRequestActivationTemporal.sendToServer(idx);
        button.setState(new Pair<>(idx, !pair.getSecond()));
    }

    private static void onTogglerClick(ToggleButton<Pair<Integer, IDataPouchCompanion>> button) {
        Pair<Integer, IDataPouchCompanion> pair = button.getState();
        pair.getSecond().setActivationIndex(pair.getFirst());
    }

    public class ToggleButton<State> extends AbstractButton {
        protected static final TextComponent EMPTY = new TextComponent("");
        protected final ButtonState<State> dataOn;
        protected final ButtonState<State> dataOff;
        protected final Predicate<State> isOn;
        protected State state;

        public ToggleButton(int x, int y, State initialState, ButtonState<State> dataOn, ButtonState<State> dataOff, Predicate<State> isOn) {
            super(x, y, dataOn.texture.getSize().x(), dataOn.texture.getSize().y(), EMPTY);
            this.dataOn = dataOn;
            this.dataOff = dataOff;
            this.isOn = isOn;
            this.state = initialState;
        }

        @Override
        public boolean isActive() {
            return this.isOn.test(this.state);
        }

        @Override
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            if (this.isActive()) {
                this.dataOn.texture.blitSlow(poseStack, this.x, this.y);
                if (this.isHovered && this.dataOn.onTooltip != null) {
                    this.dataOn.onTooltip.draw(this, poseStack, mouseX, mouseY);
                }
            } else {
                this.dataOff.texture.blitSlow(poseStack, this.x, this.y);
                if (this.isHovered && this.dataOff.onTooltip != null) {
                    this.dataOff.onTooltip.draw(this, poseStack, mouseX, mouseY);
                }
            }
        }

        private static final OnTooltip<?> DEFAULT_ON_TOOLTIP = (button, poseStack, mouseX, mouseY) -> {
            // TODO
        };

        @Override
        public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
            this.defaultButtonNarrationText(pNarrationElementOutput);
        }

        public State getState() {
            return this.state;
        }

        public void setState(State state) {
            this.state = state;
        }

        @FunctionalInterface
        public static interface OnTooltip<State> {
            void draw(ToggleButton<State> button, PoseStack poseStack, int mouseX, int mouseY);
        }

        @FunctionalInterface
        public static interface OnClick<State> {
            void click(ToggleButton<State> button);
        }

        public static record ButtonState<State>(
            String label,
            ComponentTexture texture,
            OnClick<State> onClick,
            OnTooltip<State> onTooltip,
            Predicate<State> predicate
        ) {}

        @Override
        public void onPress() {
            if (this.isActive()) {
                this.dataOn.onClick.click(this);
            } else {
                this.dataOff.onClick.click(this);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static final ToggleButton.ButtonState<Pair<Integer, Boolean>> ACTIVATOR_STATE_0 = new ToggleButton.ButtonState<Pair<Integer, Boolean>>(
        "Activate Temporal",
        CatalogMenu.ACTIVATE_READY,
        ScreenInterfacePouchCompanion::onActivatorClick,
        (OnTooltip<Pair<Integer, Boolean>>) ToggleButton.DEFAULT_ON_TOOLTIP,
        state -> state.getSecond()
    );

    @SuppressWarnings("unchecked")
    private static final ToggleButton.ButtonState<Pair<Integer, Boolean>> ACTIVATOR_STATE_1 = new ToggleButton.ButtonState<Pair<Integer, Boolean>>(
        "Resting...",
        CatalogMenu.ACTIVATE_RESTING,
        ScreenInterfacePouchCompanion::onActivatorClick,
        (OnTooltip<Pair<Integer, Boolean>>) ToggleButton.DEFAULT_ON_TOOLTIP,
        state -> !state.getSecond()
    );

    @SuppressWarnings("unchecked")
    private static final ToggleButton.ButtonState<Pair<Integer, IDataPouchCompanion>> TOGGLER_INDEX_0 = new ToggleButton.ButtonState<Pair<Integer, IDataPouchCompanion>>(
        "",
        CatalogMenu.TOGGLER_ON,
        ScreenInterfacePouchCompanion::onTogglerClick,
        (OnTooltip<Pair<Integer, IDataPouchCompanion>>) ToggleButton.DEFAULT_ON_TOOLTIP,
        state -> state.getFirst() == state.getSecond().getActivationIndex()
    );

    @SuppressWarnings("unchecked")
    private static final ToggleButton.ButtonState<Pair<Integer, IDataPouchCompanion>> TOGGLER_INDEX_1 = new ToggleButton.ButtonState<Pair<Integer, IDataPouchCompanion>>(
        "Use this companion's temporal when using the hotkey",
        CatalogMenu.TOGGLER_OFF,
        ScreenInterfacePouchCompanion::onTogglerClick,
        (OnTooltip<Pair<Integer, IDataPouchCompanion>>) ToggleButton.DEFAULT_ON_TOOLTIP,
        state -> state.getFirst() != state.getSecond().getActivationIndex()
    );
}
