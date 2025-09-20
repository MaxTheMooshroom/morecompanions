package net.qiyanamark.companionpouch.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.function.TriFunction;

import com.mojang.datafixers.util.Pair;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.qiyanamark.companionpouch.helper.shapes.Structs.Vec2i;

public class HelperInventory {
    private static final int PLAYER_INV_TOP = 51;
    private static final int PLAYER_INV_LEFT = 8;
    private static final List<Pair<Integer, Vec2i>> PLAYER_INVENTORY;

    private static final TriFunction<Container, Integer, Pair<Integer, Vec2i>, Slot> DEFAULT_SLOT_PRODUCER =
        (container, offset, pair) -> new Slot(
            container,
            offset + pair.getFirst(),
            pair.getSecond().x(),
            pair.getSecond().y()
        );

    public static <T extends Slot> List<T> playerInventory(Container inv, int offset, TriFunction<Container, Integer, Pair<Integer, Vec2i>, T> slotProducer) {
        return PLAYER_INVENTORY.stream()
            .map(p -> slotProducer.apply(inv, offset, p))
            .collect(Collectors.toList());
    }

    public static List<Slot> playerInventory(Container inv, int offset) {
        return playerInventory(inv, offset, DEFAULT_SLOT_PRODUCER);
    }

    static {
        PLAYER_INVENTORY = new ArrayList<>();
        for (int col = 0; col < 9; col++) {
            // Player inventory 9x3
            for (int row = 0; row < 3; row++) {
                PLAYER_INVENTORY.add(
                    new Pair<>(
                        col + row * 9 + 9,
                        new Vec2i(
                            PLAYER_INV_LEFT + col * HelperGuiSlot.ICON_WIDTH_TOTAL,
                            PLAYER_INV_TOP + row * HelperGuiSlot.ICON_HEIGHT_TOTAL
                        )
                    )
                );
            }

            // Hotbar 9x1
            PLAYER_INVENTORY.add(new Pair<>(col, new Vec2i(PLAYER_INV_LEFT + col * HelperGuiSlot.ICON_WIDTH_TOTAL, 109)));
        }
    }
}
