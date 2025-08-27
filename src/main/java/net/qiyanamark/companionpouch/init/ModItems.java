package net.qiyanamark.companionpouch.init;


import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.qiyanamark.companionpouch.companionpouch;
import net.qiyanamark.companionpouch.item.CompanionPouchItem;


public class ModItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, companionpouch.MOD_ID);


    public static final RegistryObject<Item> COMPANION_POUCH = REGISTER.register("companion_pouch",
            () -> new CompanionPouchItem(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_COMBAT)));
}