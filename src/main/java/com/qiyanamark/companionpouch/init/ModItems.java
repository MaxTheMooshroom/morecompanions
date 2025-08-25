package com.qiyanamark.companionpouch.init;


import com.qiyanamark.companionpouch.CompanionPouchMod;
import com.qiyanamark.companionpouch.item.CompanionPouchItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CompanionPouchMod.MOD_ID);

    public static final RegistryObject<CompanionPouchItem> COMPANION_POUCH =
            ITEMS.register("companion_pouch", CompanionPouchItem::new);
}
