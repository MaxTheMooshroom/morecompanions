package net.qiyanamark.companionpouch.init;


import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.qiyanamark.companionpouch.companionpouch;
import net.qiyanamark.companionpouch.menu.CompanionPouchMenu;


import net.minecraftforge.common.extensions.IForgeMenuType;


public class ModMenus {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.MENU_TYPES, companionpouch.MOD_ID);


    public static final RegistryObject<MenuType<CompanionPouchMenu>> COMPANION_POUCH = REGISTER.register("companion_pouch",
            () -> IForgeMenuType.create(CompanionPouchMenu::fromNetwork));
}