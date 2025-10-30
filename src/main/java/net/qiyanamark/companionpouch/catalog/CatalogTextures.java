package net.qiyanamark.companionpouch.catalog;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import net.minecraft.resources.ResourceLocation;

import net.qiyanamark.companionpouch.ModCompanionPouch;
import net.qiyanamark.companionpouch.util.CompositeTexture;
import net.qiyanamark.companionpouch.util.CompositeTexture.ComponentTexture;
import net.qiyanamark.companionpouch.util.Structs.Vec2i;

public class CatalogTextures {
    public static final Map<ResourceLocation, CompositeTexture> REGISTRY_COMPOSITE = new HashMap<>();
    public static final Map<ResourceLocation, ComponentTexture> REGISTRY_COMPONENT = new HashMap<>();

    public static final CompositeTexture TEXTURE_ATLAS_MENUS_POUCH = createAtlas("textures/gui/container/pouch_companion.png");

    public static final ComponentTexture SCREEN_INVENTORY_CHROME = createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "inventory.bg", new Vec2i(0, 0), new Vec2i(174, 163));
    public static final ComponentTexture SCREEN_INTERFACE_CHROME = createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "interface.bg", new Vec2i(0, 166), new Vec2i(128, 64));

    public static final ComponentTexture BUTTON_UNPRESSED = createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "button.base.unpressed", new Vec2i(176, 0), new Vec2i(24, 24));
    public static final ComponentTexture BUTTON_PRESSED = createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "button.base.pressed", new Vec2i(176, 0), new Vec2i(24, 24));

    public static final ComponentTexture ACTIVATE_READY = createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "button.activate.unpressed", new Vec2i(176, 28), new Vec2i(24, 24));
    public static final ComponentTexture ACTIVATE_RESTING = createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "button.activate.pressed", new Vec2i(204, 28), new Vec2i(24, 24));

    public static final ComponentTexture TOGGLER_ON = createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "button.toggler.unpressed", new Vec2i(176, 54), new Vec2i(12, 5));
    public static final ComponentTexture TOGGLER_OFF = createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "button.toggler.pressed", new Vec2i(176, 61), new Vec2i(12, 5));

    public static final ComponentTexture MENU_SLOT = createAtlasEntry(TEXTURE_ATLAS_MENUS_POUCH, "ui.slot.bg", new Vec2i(192, 54), new Vec2i(18, 18));

    public static CompositeTexture createAtlas(ResourceLocation rel) {
        CompositeTexture result = new CompositeTexture(rel);
        REGISTRY_COMPOSITE.put(rel, result);
        return result;
    }

    public static CompositeTexture createAtlas(String path) {
        return createAtlas(ModCompanionPouch.rel(path));
    }

    public static CompositeTexture createAtlas(ResourceLocation rel, Vec2i size) {
        CompositeTexture result = new CompositeTexture(rel, size);
        REGISTRY_COMPOSITE.put(rel, result);
        return result;
    }

    public static CompositeTexture createAtlas(String path, Vec2i size) {
        return createAtlas(ModCompanionPouch.rel(path), size);
    }

    public static CompositeTexture createAtlas(ResourceLocation rel, int width, int height) {
        CompositeTexture result = new CompositeTexture(rel, new Vec2i(width, height));
        REGISTRY_COMPOSITE.put(rel, result);
        return result;
    }

    public static CompositeTexture createAtlas(String path, int width, int height) {
        return createAtlas(ModCompanionPouch.rel(path), new Vec2i(width, height));
    }

    // === atlas entries ===

    public static ComponentTexture createAtlasEntry(ResourceLocation parentRel, String path, Vec2i position, Vec2i size) {
        if (!REGISTRY_COMPOSITE.containsKey(parentRel)) {
            throw new NoSuchElementException();
        }

        ResourceLocation rel = ModCompanionPouch.rel(parentRel.getPath() + "/" + path);

        if (REGISTRY_COMPONENT.containsKey(rel)) {
            throw new IllegalArgumentException("Duplicate atlas entry key: " + rel);
        }

        ComponentTexture result = REGISTRY_COMPOSITE.get(parentRel).getComponentTexture(position, size);
        REGISTRY_COMPONENT.put(rel, result);

        return result;
    }

    public static ComponentTexture createAtlasEntry(CompositeTexture parent, String path, Vec2i position, Vec2i size) {
        ResourceLocation parentRel = parent.getRel();
        REGISTRY_COMPOSITE.putIfAbsent(parentRel, parent);
        return createAtlasEntry(parentRel, path, position, size);
    }
}
