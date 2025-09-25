package net.qiyanamark.companionpouch.catalog;

import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraft.world.inventory.MenuType;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.menu.container.MenuInterfacePouchCompanion;
import net.qiyanamark.companionpouch.menu.container.MenuInventoryPouchCompanion;
import net.qiyanamark.companionpouch.util.CompositeTexture;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;

@Mod.EventBusSubscriber(modid = ModCompanionPouch.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CatalogMenu {
    public static final MenuType<MenuInventoryPouchCompanion> COMPANION_POUCH_INVENTORY;
    public static final MenuType<MenuInterfacePouchCompanion> COMPANION_POUCH_INTERFACE;

    public static final CompositeTexture TEXTURE_ATLAS_MENUS_POUCH = new CompositeTexture("textures/gui/container/pouch_companion.png");

    public static final ComponentTexture SCREEN_INVENTORY_CHROME = TEXTURE_ATLAS_MENUS_POUCH.getComponentTexture(new Vec2i(0, 0), new Vec2i(174, 164));
    public static final ComponentTexture SCREEN_INTERFACE_CHROME = TEXTURE_ATLAS_MENUS_POUCH.getComponentTexture(new Vec2i(0, 166), new Vec2i(252, 64));

    public static final ComponentTexture ACTIVATE_READY = TEXTURE_ATLAS_MENUS_POUCH.getComponentTexture(new Vec2i(0, 0), new Vec2i(0, 0));
    public static final ComponentTexture ACTIVATE_RESTING = TEXTURE_ATLAS_MENUS_POUCH.getComponentTexture(new Vec2i(0, 0), new Vec2i(0, 0));

    public static final ComponentTexture TOGGLE_ON = TEXTURE_ATLAS_MENUS_POUCH.getComponentTexture(new Vec2i(176, 54), new Vec2i(12, 5));
    public static final ComponentTexture TOGGLE_OFF = TEXTURE_ATLAS_MENUS_POUCH.getComponentTexture(new Vec2i(176, 61), new Vec2i(12, 5));

    public static final ComponentTexture MENU_SLOT = TEXTURE_ATLAS_MENUS_POUCH.getComponentTexture(new Vec2i(192, 54), new Vec2i(18, 18));
    public static final Vec2i MENU_INTERFACE_SLOT_PADDING_LEFT_TOP = new Vec2i(11, 7);
    public static final Vec2i MENU_INTERFACE_SLOT_ACTIVATE_OFFSET = new Vec2i(-4, 21);
    public static final Vec2i MENU_INTERFACE_SLOT_TOGGLE_OFFSET = new Vec2i(2, 47);
    public static final int MENU_INTERFACE_SLOT_SPACING = 12;

    @SubscribeEvent
    public static void registerMenus(RegistryEvent.Register<MenuType<?>> event) {
        IForgeRegistry<MenuType<?>> registry = event.getRegistry();

        registry.register(COMPANION_POUCH_INVENTORY.setRegistryName(MenuInventoryPouchCompanion.MENU_ID));
        registry.register(COMPANION_POUCH_INTERFACE.setRegistryName(MenuInterfacePouchCompanion.MENU_ID));
    }

    static {
        COMPANION_POUCH_INVENTORY = IForgeMenuType.create(MenuInventoryPouchCompanion::fromNetwork);
        COMPANION_POUCH_INTERFACE = IForgeMenuType.create(MenuInterfacePouchCompanion::fromNetwork);
    }
}
