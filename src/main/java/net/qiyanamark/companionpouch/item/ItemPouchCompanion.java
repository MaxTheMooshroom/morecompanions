package net.qiyanamark.companionpouch.item;

import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import iskallia.vault.item.BasicItem;
import static iskallia.vault.init.ModItems.VAULT_MOD_GROUP;

public class ItemPouchCompanion extends BasicItem implements ICurioItem {
    public static final String ITEM_ID = "pouch_companion";

    public static final int SLOT_COUNT = 3;

    public ItemPouchCompanion(ResourceLocation id) {
        super(id, new Properties().stacksTo(1).tab(VAULT_MOD_GROUP));
    }
}