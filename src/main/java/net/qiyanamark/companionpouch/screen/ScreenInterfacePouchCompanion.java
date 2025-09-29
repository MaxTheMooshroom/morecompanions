package net.qiyanamark.companionpouch.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import iskallia.vault.core.vault.Vault;
import iskallia.vault.core.vault.VaultUtils;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.capabilities.CapabilityDataPouchCompanion;
import net.qiyanamark.companionpouch.capabilities.IDataPouchCompanion;
import net.qiyanamark.companionpouch.catalog.CatalogMenu;
import net.qiyanamark.companionpouch.menu.container.MenuInterfacePouchCompanion;
import net.qiyanamark.companionpouch.network.PacketRequestActivationTemporal;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;
import net.qiyanamark.companionpouch.util.annotations.Extends;
import net.qiyanamark.companionpouch.util.annotations.Implements;

public class ScreenInterfacePouchCompanion extends AbstractContainerScreen<MenuInterfacePouchCompanion> {
    private final IDataPouchCompanion pouchCap;

    public static class IndexedItem<T> extends Pair<Integer, T> {
        public IndexedItem(int index, T item) { super(index, item); }
        public int index() { return this.getFirst(); }
        public T item() { return this.getSecond(); }
    }

    private List<ToggleButton<?>> buttons = new ArrayList<>();

    private boolean hoverEnabled = true;

    private static final Predicate<IndexedItem<IDataPouchCompanion>> TOGGLER_PREDICATE = state -> state.index() == state.item().getActivationIndex();
    private static final Predicate<IndexedItem<Boolean>> ACTIVATOR_PREDICATE = state -> state.item();

    public ScreenInterfacePouchCompanion(MenuInterfacePouchCompanion menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = CatalogMenu.SCREEN_INTERFACE_CHROME.getSize().x();
        this.imageHeight = CatalogMenu.SCREEN_INTERFACE_CHROME.getSize().y();
        
        ItemStack pouchStack = this.menu.getPouchStack();
        this.pouchCap = pouchStack.getCapability(CapabilityDataPouchCompanion.COMPANION_POUCH_CAPABILITY).orElseThrow(IllegalStateException::new);
        int size = this.pouchCap.getSize();

        LocalPlayer lPlayer = Minecraft.getInstance().player;
        Optional<Vault> vaultMaybe = VaultUtils.getVault(lPlayer.level);

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
            this.buttons.add(new ToggleButton<>(
                activatorPos.x(), activatorPos.y(),
                new IndexedItem<>(i, true),
                ACTIVATOR_STATE_0, ACTIVATOR_STATE_1, ACTIVATOR_PREDICATE
            ));

            // togglers
            Vec2i togglePos = CatalogMenu.MENU_INTERFACE_SLOT_TOGGLE_OFFSET.add(pos);
            this.buttons.add(new ToggleButton<>(
                togglePos.x(), togglePos.y(),
                new IndexedItem<>(i, this.pouchCap),
                TOGGLER_INDEX_0, TOGGLER_INDEX_1, TOGGLER_PREDICATE
            ));
        }
    }

    @Override
    @Implements(value = ContainerEventHandler.class, introducedBy = AbstractContainerEventHandler.class)
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        ModCompanionPouch.messageLocal("clicked");
        for (ToggleButton<?> button : this.buttons) {
            if (button.isActive() && button.cursorInBounds(pMouseX, pMouseY)) {
                button.onPress();
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        return this.hoverEnabled && super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    public void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        for (ToggleButton<?> button : this.buttons) {
            if (button.cursorInBounds(mouseX, mouseY)) {
                button.drawTooltip(poseStack, mouseX, mouseY);
            }
        }
        for (Slot slot : this.menu.slots) {
            if (this.isHovering(slot.x, slot.y, 16, 16, (double)mouseX, (double)mouseY) && slot.isActive()) {
                this.hoveredSlot = slot;
            }
        }
        super.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);

        this.hoverEnabled = false;
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.hoverEnabled = true;

        this.renderButtons(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    protected void renderButtons(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        for (ToggleButton<?> button : this.buttons) {
            button.render(poseStack, mouseX, mouseY, partialTicks);
            button.renderButton(poseStack, mouseX, mouseY, partialTicks);
        }
    }

    protected void renderChrome(PoseStack poseStack) {
        CatalogMenu.SCREEN_INTERFACE_CHROME.blitSlow(poseStack, this.leftPos, this.topPos);
    }

    protected void renderSlotBackgrounds(PoseStack poseStack) {
        this.menu.slots.forEach(slot -> CatalogMenu.MENU_SLOT.blitSlow(poseStack, this.leftPos + slot.x - 1, this.topPos + slot.y - 1));
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    protected void renderBg(PoseStack poseStack, float pPartialTick, int pMouseX, int pMouseY) {
        CatalogMenu.TEXTURE_ATLAS_MENUS_POUCH.prepareSlow();

        this.renderChrome(poseStack);
        this.renderSlotBackgrounds(poseStack);
    }

    @Override
    @Extends(AbstractContainerScreen.class)
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // don't render labels
    }

    public class ToggleButton<State> extends AbstractButton {
        protected static final TextComponent EMPTY = new TextComponent("");
        protected final ButtonData<State> dataOn;
        protected final ButtonData<State> dataOff;
        protected final Predicate<State> isOn;
        protected State state;

        public ToggleButton(int x, int y, State initialState, ButtonData<State> dataOn, ButtonData<State> dataOff, Predicate<State> isOn) {
            super(x, y, dataOn.texture.getSize().x(), dataOn.texture.getSize().y(), EMPTY);
            this.dataOn = dataOn;
            this.dataOff = dataOff;
            this.isOn = isOn;
            this.state = initialState;
        }

        @Override
        @Extends(AbstractWidget.class)
        public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            ButtonData<State> toBlit = this.checkPredicate() ? this.dataOn : this.dataOff;
            toBlit.texture.blitSlow(poseStack, this.x, this.y);
        }

        @Override
        @Implements(NarrationSupplier.class)
        public void updateNarration(NarrationElementOutput pNarrationElementOutput) {
            this.defaultButtonNarrationText(pNarrationElementOutput);
        }

        @Override
        @Extends(AbstractButton.class)
        public void onPress() {
            this.getData().onClick.click(this);
        }

        public boolean checkPredicate() {
            return this.isOn.test(this.state);
        }

        public ButtonData<State> getData() {
            return this.checkPredicate() ? this.dataOn : this.dataOff;
        }

        public void drawTooltip(PoseStack poseStack, int mouseX, int mouseY) {
            this.getData().onTooltip.draw(this, poseStack, mouseX, mouseY);
        }

        public State getState() {
            return this.state;
        }

        public void setState(State state) {
            this.state = state;
        }

        public boolean cursorInBounds(double mouseX, double mouseY) {
            return this.clicked(mouseX, mouseY);
        }

        @FunctionalInterface
        public static interface OnTooltip<State> {
            void draw(ToggleButton<State> button, PoseStack poseStack, int mouseX, int mouseY);
        }

        @FunctionalInterface
        public static interface OnClick<State> {
            void click(ToggleButton<State> button);
        }

        public static record ButtonData<State>(
            String label,
            ComponentTexture texture,
            OnClick<State> onClick,
            OnTooltip<State> onTooltip,
            Predicate<State> predicate
        ) {
            @SuppressWarnings("unchecked")
            public static <T> ButtonData<T> of(String label, ComponentTexture texture, OnClick<?> onClick, OnTooltip<?> onTooltip, Predicate<?> predicate) {
                return new ButtonData<T>(label, texture, (OnClick<T>) onClick, (OnTooltip<T>) onTooltip, (Predicate<T>) predicate);
            }
        }

        private static final OnTooltip<?> DEFAULT_ON_TOOLTIP = (button, poseStack, mouseX, mouseY) -> {
            Font font = Minecraft.getInstance().font;
            int x = mouseX + 12;
            int y = mouseY - 12;
            font.drawShadow(poseStack, new TextComponent(button.getData().label), x, y, 0xFFFFFF);
        };
    }

    private static void onActivatorClick(ToggleButton<IndexedItem<Boolean>> button) {
        ModCompanionPouch.messageLocal("Activator clicked");

        IndexedItem<Boolean> pair = button.getState();
        int idx = pair.getFirst();
        PacketRequestActivationTemporal.sendToServer(idx);
        button.setState(new IndexedItem<>(idx, !pair.item()));
    }

    private static void onTogglerClick(ToggleButton<IndexedItem<IDataPouchCompanion>> button) {
        ModCompanionPouch.messageLocal("Toggler clicked");

        IndexedItem<IDataPouchCompanion> pair = button.getState();
        pair.item().setActivationIndex(pair.index());
    }

    private static final ToggleButton.ButtonData<IndexedItem<Boolean>> ACTIVATOR_STATE_0 = ToggleButton.ButtonData.<IndexedItem<Boolean>>of(
        "Activate Temporal",
        CatalogMenu.ACTIVATE_READY,
        (ToggleButton.OnClick<? extends IndexedItem<Boolean>>) ScreenInterfacePouchCompanion::onActivatorClick,
        ToggleButton.DEFAULT_ON_TOOLTIP,
        (Predicate<? extends IndexedItem<Boolean>>) state -> state.getSecond()
    );

    private static final ToggleButton.ButtonData<IndexedItem<Boolean>> ACTIVATOR_STATE_1 = ToggleButton.ButtonData.<IndexedItem<Boolean>>of(
        "Resting...",
        CatalogMenu.ACTIVATE_RESTING,
        (ToggleButton.OnClick<? extends IndexedItem<Boolean>>) ScreenInterfacePouchCompanion::onActivatorClick,
        ToggleButton.DEFAULT_ON_TOOLTIP,
        (Predicate<? extends IndexedItem<Boolean>>) state -> !state.getSecond()
    );

    private static final ToggleButton.ButtonData<IndexedItem<IDataPouchCompanion>> TOGGLER_INDEX_0 = ToggleButton.ButtonData.<IndexedItem<IDataPouchCompanion>>of(
        "",
        CatalogMenu.TOGGLER_ON,
        (ToggleButton.OnClick<? extends IndexedItem<IDataPouchCompanion>>) ScreenInterfacePouchCompanion::onTogglerClick,
        ToggleButton.DEFAULT_ON_TOOLTIP,
        (Predicate<? extends IndexedItem<IDataPouchCompanion>>) state -> state.getFirst() == state.getSecond().getActivationIndex()
    );

    private static final ToggleButton.ButtonData<IndexedItem<IDataPouchCompanion>> TOGGLER_INDEX_1 = ToggleButton.ButtonData.<IndexedItem<IDataPouchCompanion>>of(
        "Use this companion's temporal when using the hotkey",
        CatalogMenu.TOGGLER_OFF,
        (ToggleButton.OnClick<? extends IndexedItem<IDataPouchCompanion>>) ScreenInterfacePouchCompanion::onTogglerClick,
        ToggleButton.DEFAULT_ON_TOOLTIP,
        (Predicate<? extends IndexedItem<IDataPouchCompanion>>) state -> state.getFirst() != state.getSecond().getActivationIndex()
    );
}
