package com.qiyanamark.companionpouch.init;


import com.qiyanamark.companionpouch.CompanionPouchMod;
import com.qiyanamark.companionpouch.container.CompanionPouchContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModContainers {
    public static final DeferredRegister<MenuType<?>> CONTAINERS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, CompanionPouchMod.MOD_ID);

    public static final RegistryObject<MenuType<CompanionPouchContainer>> COMPANION_POUCH =
            CONTAINERS.register("companion_pouch",
                    () -> IForgeMenuType.create(CompanionPouchContainer::new));
}
