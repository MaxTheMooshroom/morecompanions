package net.qiyanamark.companionpouch.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.function.TriFunction;

import com.mojang.datafixers.util.Pair;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;

public class HelperInventory {
    public static final int PLAYER_INV_TOP = 82;
    public static final int PLAYER_INV_LEFT = 7;
    
    public static final int ICON_WIDTH = 18;
    public static final int ICON_HEIGHT = 18;
    public static final int ICON_SPACING = 0;
    
    public static final int ICON_WIDTH_TOTAL = ICON_WIDTH + ICON_SPACING;
    public static final int ICON_HEIGHT_TOTAL = ICON_HEIGHT + ICON_SPACING;

    private static final List<Pair<Integer, Vec2i>> PLAYER_INVENTORY;

    private static Slot defaultSlotProducer(Container container, int offset, Pair<Integer, Vec2i> pair) {
        return new Slot(container, offset + pair.getFirst(), pair.getSecond().x(), pair.getSecond().y());
    }

    public static <T extends Slot> List<T> playerInventory(Container inv, TriFunction<Container, Integer, Pair<Integer, Vec2i>, T> slotProducer) {
        return PLAYER_INVENTORY.stream()
            .map(p -> slotProducer.apply(inv, 0, p))
            .collect(Collectors.toList());
    }

    public static List<Slot> playerInventory(Container inv) {
        return HelperInventory.playerInventory(inv, HelperInventory::defaultSlotProducer);
    }

    static {
        PLAYER_INVENTORY = new ArrayList<>();
        int hotbar_y = PLAYER_INV_TOP + 3 * ICON_HEIGHT_TOTAL + 4;
        // Player inventory 9x3
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                PLAYER_INVENTORY.add(
                    new Pair<>(
                        col + row * 9 + 9,
                        new Vec2i(
                            PLAYER_INV_LEFT + col * ICON_WIDTH_TOTAL,
                            PLAYER_INV_TOP + row * ICON_HEIGHT_TOTAL
                        )
                    )
                );
            }
        }

        // Hotbar 9x1
        for (int col = 0; col < 9; col++) {
            PLAYER_INVENTORY.add(new Pair<>(col, new Vec2i(PLAYER_INV_LEFT + col * ICON_WIDTH_TOTAL, hotbar_y)));
        }
    }
}
