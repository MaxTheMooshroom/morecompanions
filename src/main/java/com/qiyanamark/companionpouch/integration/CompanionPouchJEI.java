package com.qiyanamark.companionpouch.integration;


import com.qiyanamark.companionpouch.CompanionPouchMod;
import com.qiyanamark.companionpouch.init.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class CompanionPouchJEI implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(CompanionPouchMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        registration.addIngredientInfo(
                ModItems.COMPANION_POUCH.get().getDefaultInstance(),
                VanillaTypes.ITEM_STACK,
                "Companion Pouch stores up to 3 companions. Equip in head curio slot and press T in vaults to activate all temporal modifiers simultaneously."
        );
    }
}