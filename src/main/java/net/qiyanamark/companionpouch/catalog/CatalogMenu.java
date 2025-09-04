package net.qiyanamark.companionpouch.catalog;

import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraft.world.inventory.MenuType;

import net.qiyanamark.companionpouch.CompanionPouchMod;
import net.qiyanamark.companionpouch.menu.MenuPouchCompanion;

@Mod.EventBusSubscriber(modid = CompanionPouchMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CatalogMenu {
    public static final MenuType<MenuPouchCompanion> COMPANION_POUCH;

    @SubscribeEvent
    public static void registerMenus(RegistryEvent.Register<MenuType<?>> event) {
        IForgeRegistry<MenuType<?>> registry = event.getRegistry();

        registry.register(COMPANION_POUCH.setRegistryName(MenuPouchCompanion.MENU_ID));
    }

    static {
        COMPANION_POUCH = IForgeMenuType.create(MenuPouchCompanion::fromNetwork);
    }
}
