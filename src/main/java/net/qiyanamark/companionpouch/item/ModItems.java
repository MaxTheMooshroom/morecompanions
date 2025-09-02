package net.qiyanamark.companionpouch.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

import net.qiyanamark.companionpouch.CompanionPouchMod;

@Mod.EventBusSubscriber(modid = CompanionPouchMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
    public static CompanionPouchItem COMPANION_POUCH;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(COMPANION_POUCH);
    }

    static {
        COMPANION_POUCH = new CompanionPouchItem(new ResourceLocation(CompanionPouchItem.ITEM_ID));
    }
}