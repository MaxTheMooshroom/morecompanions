package net.qiyanamark.companionpouch.catalogs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

import net.qiyanamark.companionpouch.CompanionPouchMod;
import net.qiyanamark.companionpouch.item.ItemPouchCompanion;

@Mod.EventBusSubscriber(modid = CompanionPouchMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CatalogItem {
    public static final ItemPouchCompanion COMPANION_POUCH;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();

        registry.register(COMPANION_POUCH);
    }

    static {
        COMPANION_POUCH = new ItemPouchCompanion(new ResourceLocation(ItemPouchCompanion.ITEM_ID));
    }
}