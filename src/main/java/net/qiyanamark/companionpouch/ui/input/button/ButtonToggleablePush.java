package net.qiyanamark.companionpouch.ui.input.button;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.Slot;

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
public abstract class ButtonToggleablePush<Screen extends ScreenBase<Screen, ?>> extends ButtonToggleable<Screen> {
    public static final ComponentTexture BUTTON_BASE_UNPRESSED = CatalogTextures.createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "button.base.unpressed", new Vec2i(176, 0), new Vec2i(24, 24));
    public static final ComponentTexture BUTTON_BASE_PRESSED =   CatalogTextures.createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "button.base.pressed", new Vec2i(204, 0), new Vec2i(24, 24));

    private static final Vec2i SIZE = new Vec2i(24, 24);

    private final byte slotId;

    public ButtonToggleablePush(byte slotId, @NotNull Screen screen, @NotNull Vec2i position) {
        super(screen, position, SIZE.copy());
        this.slotId = slotId;
    }

    public ButtonToggleablePush(byte slotId, @NotNull Screen screen, @NotNull ComponentTexture texture, @NotNull Vec2i position) {
        super(screen, position, SIZE.copy());
        this.slotId = slotId;
    }

    @Override
    @Extends(WidgetBasic.class)
    public void reinit() {}

    @Override
    @Extends(ButtonToggleable.class)
    public void updateToggleState() {
//        ModCompanionPouch.getClientPlayer().orElseThrow().sendMessage(new TextComponent("toggle state of push recalculated"), NIL_UUID);
//        Menu menu = this.screen.getMenu();
//        this.pressed = !menu.canUseCompanionTemporal(this.slotId);
//        this.tooltip = this.pressed ? LABEL_PRESSED.copy() : LABEL_UNPRESSED.copy();
//        this.active = !menu.getSlot(this.slotId).getItem().isEmpty();
    }

    @Override
    @Extends(WidgetButton.class)
    public void onClick(double mouseX, double mouseY, int button) {
        if (this.active && !this.pressed) {
            ModCompanionPouch.getClientPlayer().orElseThrow().sendMessage(new TextComponent("clicked"), NIL_UUID);
            PacketRequestActionTemporal.ACTIVATE.sendToServer(this.slotId);
        }
    }

    public int getSlotIndex() {
        return this.slotId;
    }

    public void recalculatePosition(Screen screen) {
        Slot slot = screen.getMenu().getSlot(this.slotId);
        this.position = CatalogMenu.MENU_INTERFACE_SLOT_ACTIVATE_OFFSET.add(slot.x + screen.getGuiLeft(), slot.y + screen.getGuiTop());
    }
}
