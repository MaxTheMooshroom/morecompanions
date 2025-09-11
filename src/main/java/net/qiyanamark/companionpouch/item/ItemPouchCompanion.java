package net.qiyanamark.companionpouch.item;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import iskallia.vault.item.BasicItem;
import static iskallia.vault.init.ModItems.VAULT_MOD_GROUP;

public class ItemPouchCompanion extends BasicItem {
    public static final String ITEM_ID = "pouch_companion";

    public static final int SLOT_COUNT = 3;

    public ItemPouchCompanion(String id) {
        super(ModCompanionPouch.rel(id), new Properties().stacksTo(1).tab(VAULT_MOD_GROUP));
    }
}