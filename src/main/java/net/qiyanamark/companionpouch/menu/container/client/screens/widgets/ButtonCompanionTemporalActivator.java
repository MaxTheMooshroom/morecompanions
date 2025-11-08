package net.qiyanamark.companionpouch.menu.container.client.screens.widgets;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

import net.qiyanamark.companionpouch.catalog.CatalogTextures;
import net.qiyanamark.companionpouch.ui.ScreenBase;
import net.qiyanamark.companionpouch.ui.WidgetBasic;
import net.qiyanamark.companionpouch.ui.input.button.ButtonToggleablePush;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;
import net.qiyanamark.companionpouch.util.annotations.Extends;

import static net.minecraft.Util.NIL_UUID;
import static net.qiyanamark.companionpouch.catalog.CatalogTextures.TEXTURE_ATLAS_MENUS_POUCH;

public class ButtonCompanionTemporalActivator<Screen extends ScreenBase<Screen, ?>> extends ButtonToggleablePush<Screen> {
    public static final ComponentTexture BUTTON_ACTIVATE_UNPRESSED = CatalogTextures.createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "button.activate.unpressed", new Vec2i(176, 28), new Vec2i(24, 24));
    public static final ComponentTexture BUTTON_ACTIVATE_PRESSED = CatalogTextures.createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "button.activate.pressed", new Vec2i(204, 28), new Vec2i(24, 24));

    private static final Component LABEL_UNPRESSED = new TranslatableComponent("screen.companionpouch.interface_pouch_companion.push.unpressed");
    private static final Component LABEL_PRESSED = new TranslatableComponent("screen.companionpouch.interface_pouch_companion.push.pressed");

    public ButtonCompanionTemporalActivator(byte slotId, @NotNull Screen screen, @NotNull Vec2i position) {
        super(slotId, screen, BUTTON_BASE_UNPRESSED, position);
    }

    @Override
    @Extends(WidgetBasic.class)
    public void postInit() {
        this.imageUnpressedEnabled = BUTTON_ACTIVATE_UNPRESSED;
        this.imagePressedEnabled = BUTTON_ACTIVATE_PRESSED;
        this.imageUnpressedDisabled = BUTTON_BASE_UNPRESSED;
        this.imagePressedDisabled = BUTTON_BASE_PRESSED;

        super.postInit();

        // preemptive tick to initialize
        this.tick();

        this.screen.getMenu().addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(@NotNull AbstractContainerMenu menu, int index, @NotNull ItemStack contents) {}

            @Override
            public void dataChanged(@NotNull AbstractContainerMenu menu, int index, int value) {
//                if ((index - 2) == ButtonCompanionTemporalActivator.this.slotId) {
//                    ModCompanionPouch.getClientPlayer().orElseThrow().sendMessage(new TextComponent("push update received on client"), NIL_UUID);
//                    ButtonCompanionTemporalActivator.this.markDirty();
//                }
                ButtonCompanionTemporalActivator.this.screen.getMenu().lPlayer.sendMessage(new TextComponent("push update received on client"), NIL_UUID);
                ButtonCompanionTemporalActivator.this.markDirty();
            }
        });
    }
}
