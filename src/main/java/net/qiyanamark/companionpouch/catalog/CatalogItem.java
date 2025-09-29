package net.qiyanamark.companionpouch.catalog;

import net.minecraft.world.item.Item;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;

public class CatalogItem {
    public static final DeferredRegister<Item> REGISTRY;

    public static final RegistryObject<ItemPouchCompanion> COMPANION_POUCH;

    static {
        REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ModCompanionPouch.MOD_ID);

        COMPANION_POUCH = REGISTRY.register(ItemPouchCompanion.REL_PATH, () -> new ItemPouchCompanion());
    }
}