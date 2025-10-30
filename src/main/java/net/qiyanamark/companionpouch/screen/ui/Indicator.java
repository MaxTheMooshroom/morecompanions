package net.qiyanamark.companionpouch.screen.ui;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.qiyanamark.companionpouch.menu.container.MenuPouchBase;
import net.qiyanamark.companionpouch.screen.ScreenBase;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs;

@OnlyIn(Dist.CLIENT)
public class Indicator<State, Screen extends ScreenBase<Menu>, Menu extends MenuPouchBase> extends WidgetBasic<Screen, Menu> {
    protected final Map<State, Pair<ComponentTexture, Component>> stateMap;

    protected Supplier<State> stateEvaluator;
    private boolean dirty = true;

    public Indicator(Supplier<State> stateEvaluator, ComponentTexture texture, Structs.@NotNull Vec2i position, Structs.@NotNull Vec2i size) {
        super(texture, position, size);
        this.stateMap = new HashMap<>();
        this.stateEvaluator = stateEvaluator;

        this.initStateMap();
    }

    public Indicator(Map<State, Pair<ComponentTexture, Component>> stateMap, Supplier<State> stateEvaluator, ComponentTexture texture, Structs.@NotNull Vec2i position, Structs.@NotNull Vec2i size) {
        super(texture, position, size);
        this.stateMap = stateMap;
        this.stateEvaluator = stateEvaluator;

        this.initStateMap();

        this.screenTarget.read().getMenu().addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(@NotNull AbstractContainerMenu pContainerToSend, int pDataSlotIndex, @NotNull ItemStack pStack) {}

            @Override
            public void dataChanged(@NotNull AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {
                Indicator.this.markDirty();
            }
        });
    }

    protected void initStateMap() {}

    protected final void markDirty() {
        this.dirty = true;
    }

    protected final boolean getDirty() {
        return this.dirty;
    }

    public void updateState() {
        Pair<ComponentTexture, Component> pair = this.stateMap.get(this.stateEvaluator.get());
        this.texture = pair.getFirst();
        this.tooltip = pair.getSecond();
    }

    public void clean() {
        if (this.dirty) {
            this.updateState();
            this.dirty = false;
        }
    }

    @Override
    public void tick() {
        this.clean();
    }
}
