package net.qiyanamark.companionpouch.catalog;

import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraft.world.inventory.MenuType;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.container.ContainerPouchCompanion;

@Mod.EventBusSubscriber(modid = ModCompanionPouch.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CatalogContainer {
    public static final MenuType<ContainerPouchCompanion> COMPANION_POUCH;

    @SubscribeEvent
    public static void registerMenus(RegistryEvent.Register<MenuType<?>> event) {
        IForgeRegistry<MenuType<?>> registry = event.getRegistry();

        registry.register(COMPANION_POUCH.setRegistryName(ContainerPouchCompanion.MENU_ID));
    }

    static {
        COMPANION_POUCH = IForgeMenuType.create(ContainerPouchCompanion::fromNetwork);
    }
}
