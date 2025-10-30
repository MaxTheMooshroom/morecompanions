package net.qiyanamark.companionpouch.screen.ui.button;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

import net.qiyanamark.companionpouch.catalog.CatalogTextures;
import net.qiyanamark.companionpouch.menu.container.MenuPouchBase;
import net.qiyanamark.companionpouch.network.PacketRequestActionTemporal;
import net.qiyanamark.companionpouch.screen.ScreenBase;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;

public class ButtonToggleableSlider<Screen extends ScreenBase<Menu>, Menu extends MenuPouchBase> extends ButtonToggleable<Screen, Menu> {
    private static final ComponentTexture STATE_ON = CatalogTextures.TOGGLER_ON;
    private static final ComponentTexture STATE_OFF = CatalogTextures.TOGGLER_OFF;
    private static final Component LABEL_ON = new TranslatableComponent("screen.companionpouch.interface_pouch_companion.slider.on");
    private static final Component LABEL_OFF = new TranslatableComponent("screen.companionpouch.interface_pouch_companion.slider.off");
    private static final Vec2i SIZE = new Vec2i(12, 5);

    private final byte slotId;

    public ButtonToggleableSlider(byte slotId, @NotNull Vec2i position) {
        super(position, SIZE.copy());
        this.slotId = slotId;
    }

    public ButtonToggleableSlider(byte slotId, Screen screen, @NotNull Vec2i position) {
        super(screen, position, SIZE.copy());
        this.slotId = slotId;
    }

    @Override
    protected void init() {
        this.imageOn.write(STATE_ON);
        this.imageOff.write(STATE_OFF);

        this.screenTarget.read().getMenu().addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(@NotNull AbstractContainerMenu pContainerToSend, int pDataSlotIndex, @NotNull ItemStack pStack) {}

            @Override
            public void dataChanged(@NotNull AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {
                ButtonToggleableSlider.this.markDirty();
            }
        });
    }

    @Override
    public void updateState() {
        this.state = this.screenTarget.read().getMenu().getActivationIndex() == this.slotId;
        this.tooltip = this.state ? LABEL_ON : LABEL_OFF;
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        PacketRequestActionTemporal.SET_SELECTED.sendToServer(this.slotId);
    }
}
