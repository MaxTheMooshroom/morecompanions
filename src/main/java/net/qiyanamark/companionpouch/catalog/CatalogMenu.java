package net.qiyanamark.companionpouch.catalog;

import net.minecraft.world.inventory.MenuType;

import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.menu.container.MenuInterfacePouchCompanion;
import net.qiyanamark.companionpouch.menu.container.MenuInventoryPouchCompanion;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;

@Mod.EventBusSubscriber(modid = ModCompanionPouch.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CatalogMenu {
    public static final MenuType<MenuInventoryPouchCompanion> COMPANION_POUCH_INVENTORY;
    public static final MenuType<MenuInterfacePouchCompanion> COMPANION_POUCH_INTERFACE;

    public static final Vec2i MENU_INTERFACE_SLOT_PADDING_LEFT_TOP = new Vec2i(18, 7);
    public static final Vec2i MENU_INTERFACE_SLOT_ACTIVATE_OFFSET = new Vec2i(-4, 21);
    public static final Vec2i MENU_INTERFACE_SLOT_SELECT_HOTKEY_OFFSET = new Vec2i(2, 47);

    @SubscribeEvent
    public static void registerMenus(RegistryEvent.Register<MenuType<?>> event) {
        IForgeRegistry<MenuType<?>> registry = event.getRegistry();

        registry.register(COMPANION_POUCH_INTERFACE.setRegistryName(MenuInterfacePouchCompanion.MENU_ID));
        registry.register(COMPANION_POUCH_INVENTORY.setRegistryName(MenuInventoryPouchCompanion.MENU_ID));
    }

    static {
        COMPANION_POUCH_INVENTORY = IForgeMenuType.create(MenuInventoryPouchCompanion::fromNetwork);
        COMPANION_POUCH_INTERFACE = IForgeMenuType.create(MenuInterfacePouchCompanion::fromNetwork);
    }
}
