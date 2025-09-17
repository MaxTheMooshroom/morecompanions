package net.qiyanamark.companionpouch.catalog;

import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraft.world.inventory.MenuType;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.menu.container.MenuContainerPouchCompanion;

@Mod.EventBusSubscriber(modid = ModCompanionPouch.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CatalogMenu {
    public static final MenuType<MenuContainerPouchCompanion> COMPANION_POUCH;

    @SubscribeEvent
    public static void registerMenus(RegistryEvent.Register<MenuType<?>> event) {
        IForgeRegistry<MenuType<?>> registry = event.getRegistry();

        registry.register(COMPANION_POUCH.setRegistryName(MenuContainerPouchCompanion.MENU_ID));
    }

    static {
        COMPANION_POUCH = IForgeMenuType.create(MenuContainerPouchCompanion::fromNetwork);
    }
}
