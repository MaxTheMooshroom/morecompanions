package net.qiyanamark.companionpouch.ui.input.button;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.catalog.CatalogMenu;
import net.qiyanamark.companionpouch.catalog.CatalogTextures;
import net.qiyanamark.companionpouch.network.PacketRequestActionTemporal;
import net.qiyanamark.companionpouch.ui.ScreenBase;
import net.qiyanamark.companionpouch.ui.WidgetBasic;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;
import net.qiyanamark.companionpouch.util.annotations.Extends;

import static net.minecraft.Util.NIL_UUID;
import static net.qiyanamark.companionpouch.catalog.CatalogTextures.TEXTURE_ATLAS_MENUS_POUCH;

@OnlyIn(Dist.CLIENT)
public abstract class ButtonToggleableSlider<Screen extends ScreenBase<Screen, ?>> extends ButtonToggleable<Screen> {
    public static final ComponentTexture BUTTON_TOGGLER_PRESSED = CatalogTextures.createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "button.toggler.pressed", new Vec2i(176, 54), new Vec2i(12, 5));
    public static final ComponentTexture BUTTON_TOGGLER_UNPRESSED = CatalogTextures.createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "button.toggler.unpressed", new Vec2i(176, 61), new Vec2i(12, 5));
    public static final ComponentTexture BUTTON_TOGGLER_DISABLED = CatalogTextures.createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "button.toggler.disabled", new Vec2i(176, 68), new Vec2i(12, 5));

    private static final Vec2i SIZE = new Vec2i(12, 5);

    private final byte slotId;

    public ButtonToggleableSlider(byte slotId, Screen screen, @NotNull Vec2i position) {
        super(screen, position, SIZE.copy());
        this.slotId = slotId;
    }

    public ButtonToggleableSlider(byte slotId, @NotNull Screen screen, @NotNull ComponentTexture texture, @NotNull Vec2i position) {
        super(screen, texture, position, SIZE.copy());
        this.slotId = slotId;
    }

    @Override
    @Extends(WidgetBasic.class)
    public void postInit() {
        this.imagePressedEnabled = BUTTON_TOGGLER_PRESSED;
        this.imageUnpressedEnabled = BUTTON_TOGGLER_UNPRESSED;
        this.imagePressedDisabled = BUTTON_TOGGLER_DISABLED;
        this.imageUnpressedDisabled = BUTTON_TOGGLER_DISABLED;

        super.postInit();

        // preemptive tick to initialize
        this.tick();

        this.screen.getMenu().addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(@NotNull AbstractContainerMenu pContainerToSend, int pDataSlotIndex, @NotNull ItemStack pStack) {}

            @Override
            public void dataChanged(@NotNull AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {
                ModCompanionPouch.getClientPlayer().orElseThrow().sendMessage(new TextComponent("slider update received on client"), NIL_UUID);
                ButtonToggleableSlider.this.markDirty();
            }
        });
    }

    @Override
    @Extends(WidgetBasic.class)
    public void reinit() {}

    @Override
    @Extends(ButtonToggleable.class)
    public void updateToggleState() {
        //noinspection unchecked
//        Menu menu = (Menu) this.screen.getMenu().downcast();
//        this.pressed = menu.getActivationIndex() == this.slotId;
//        this.tooltip = this.pressed ? LABEL_PRESSED.copy() : LABEL_UNPRESSED.copy();
//        this.active = !menu.getSlot(this.slotId).getItem().isEmpty();
    }

    @Override
    @Extends(WidgetButton.class)
    public void onClick(double mouseX, double mouseY, int button) {
        if (this.active) {
            ModCompanionPouch.getClientPlayer().orElseThrow().sendMessage(new TextComponent("clicked"), NIL_UUID);
            PacketRequestActionTemporal.SET_SELECTED.sendToServer(this.slotId);
        }
    }

    public int getSlotIndex() {
        return this.slotId;
    }

    public void recalculatePosition(Screen screen) {
        Slot slot = screen.getMenu().getSlot(this.slotId);
        this.position = CatalogMenu.MENU_INTERFACE_SLOT_SELECT_HOTKEY_OFFSET.add(slot.x + screen.getGuiLeft(), slot.y + screen.getGuiTop());
    }
}
