package net.qiyanamark.companionactivator.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class ActivationHandler {
    private static final Logger LOG = LogManager.getLogger("CompanionActivator");

    public static final ThreadLocal<Boolean> GUARD = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public static void activateAllEquippedTemporals(ServerPlayer player) {
        try {

            List<UUID> companionIds = collectCompanionIdsFromPlayer(player);
            if (companionIds.isEmpty()) {
                return;
            }

            Object vault = findVaultForPlayer(player);

            Class<?> pcdClz = Class.forName("iskallia.vault.world.data.PlayerCompanionData");
            Method getServerMethod = findStaticMethodByParam(pcdClz, "get", MinecraftServer.class);
            Object pcdInstance = null;
            if (getServerMethod != null) {
                MinecraftServer ms = player.getServer();
                pcdInstance = getServerMethod.invoke(null, ms);
            } else {
                LOG.warn("PlayerCompanionData.get(MinecraftServer) not found");
            }

            GUARD.set(Boolean.TRUE);

            for (UUID id : companionIds) {
                try {
                    Object companionData = null;
                    if (pcdInstance != null) {
                        Method m = findMethod(pcdInstance.getClass(), "getOrCreate", UUID.class);
                        if (m == null) m = findMethod(pcdInstance.getClass(), "get", UUID.class);
                        if (m != null) companionData = m.invoke(pcdInstance, id);
                        else LOG.warn("No getOrCreate/get(UUID) on PlayerCompanionData");
                    }

                    if (companionData == null) {
                        LOG.warn("CompanionData for id " + id + " not found via primary methods");
                        continue;
                    }

                    Method setActive = findMethod(companionData.getClass(), "setActive", boolean.class);
                    if (setActive != null) {
                        setActive.invoke(companionData, true);
                    }

                    Method activateTemporal = findMethod(companionData.getClass(), "activateTemporalModifier",
                            ServerPlayer.class, Class.forName("iskallia.vault.core.vault.Vault"));
                    if (activateTemporal == null) {
                        activateTemporal = findMethod(companionData.getClass(), "activateTemporalModifier", ServerPlayer.class, Object.class);
                    }
                    if (activateTemporal != null) {
                        try {
                            activateTemporal.invoke(companionData, player, vault);
                        } catch (InvocationTargetException ite) {
                            LOG.warn("activateTemporalModifier invocation error: " + ite.getCause());
                        }
                    } else {
                        LOG.warn("activateTemporalModifier not found on CompanionData");
                    }
                } catch (Throwable inner) {
                    LOG.warn("Error activating companion " + id + ": " + inner);
                }
            }

        } catch (Throwable t) {
            LOG.warn("Failed to activate equipped temporals: ", t);
        } finally {
            GUARD.set(Boolean.FALSE);
        }
    }

    private static List<UUID> collectCompanionIdsFromPlayer(ServerPlayer player) {
        List<UUID> ids = new ArrayList<>();
        try {

            List<ItemStack> stacks = tryCollectFromCurios(player);

            try {
                ItemStack helmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
                if (helmet != null) stacks.add(helmet);
            } catch (Throwable ignored) {}


            for (ItemStack s : stacks) {
                try {
                    if (s == null || s.isEmpty()) continue;
                    CompoundTag tag = s.getTag();
                    if (tag == null) continue;
                    // Common keys observed in VH
                    if (tag.contains("companionId")) {
                        String val = tag.getString("companionId");
                        try {
                            ids.add(UUID.fromString(val));
                        } catch (IllegalArgumentException ex) {
                            // maybe tag stored binary UUID
                            try {
                                java.util.UUID u = tag.getUUID("companionId");
                                if (u != null) ids.add(u);
                            } catch (Throwable ignored) {}
                        }
                    } else if (tag.contains("Id")) {
                        String val = tag.getString("Id");
                        try { ids.add(UUID.fromString(val)); } catch (Throwable ignored) {}
                    } else {
                        // scan child tag for "Id" compound entries (some stacks store companions nested)
                        for (String k : tag.getAllKeys()) {
                            try {
                                String candidate = tag.getString(k);
                                if (candidate != null && candidate.length() > 16 && candidate.contains("-")) {
                                    try { ids.add(UUID.fromString(candidate)); break; } catch (Throwable ignored) {}
                                }
                            } catch (Throwable ignored) {}
                        }
                    }
                } catch (Throwable ee) {

                }
            }


            return ids.stream().distinct().collect(Collectors.toList());
        } catch (Throwable t) {
            LOG.warn("collectCompanionIdsFromPlayer failed: " + t);
            return ids;
        }
    }


    private static List<ItemStack> tryCollectFromCurios(ServerPlayer player) {
        List<ItemStack> out = new ArrayList<>();
        try {
            Class<?> curiosApi = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Method getHelper = findStaticMethodByParam(curiosApi, "getCuriosHelper");
            if (getHelper == null) {
                // older/newer APIs different - try getCuriosHelper without params
                getHelper = findStaticMethodByParam(curiosApi, "getCuriosHelper", (Class<?>[])null);
            }
            if (getHelper == null) return out;
            Object helper = getHelper.invoke(null);


            Method m1 = findMethod(helper.getClass(), "getEquippedCurios", net.minecraft.world.entity.player.Player.class);
            if (m1 != null) {
                Object map = m1.invoke(helper, player);
                if (map instanceof Map<?,?>) {
                    for (Object key : ((Map<?,?>)map).keySet()) {
                        String keyStr = String.valueOf(key);
                        if (!"head".equals(keyStr) && !"the_vault:head".equals(keyStr) && !"head".equals(keyStr.toLowerCase())) continue;
                        Object val = ((Map<?,?>)map).get(key);
                        if (val instanceof Collection<?>) {
                            for (Object e : (Collection<?>) val) {

                                if (e == null) continue;
                                if (e.getClass().getName().contains("ItemStack")) {
                                    out.add((ItemStack)e);
                                } else {

                                    try {
                                        Method getRight = e.getClass().getMethod("getRight");
                                        Object stack = getRight.invoke(e);
                                        if (stack instanceof ItemStack) out.add((ItemStack)stack);
                                    } catch (Throwable ignore) {
                                        try {
                                            Field f = e.getClass().getDeclaredField("stack");
                                            f.setAccessible(true);
                                            Object stack = f.get(e);
                                            if (stack instanceof ItemStack) out.add((ItemStack)stack);
                                        } catch (Throwable ignore2) {}
                                    }
                                }
                            }
                        }
                    }
                    if (!out.isEmpty()) return out;
                }
            }


            try {
                Method findEquipped = findMethod(helper.getClass(), "findEquippedCurio", Class.class, net.minecraft.world.entity.player.Player.class);
                if (findEquipped != null) {

                    Class<?> companionItemClz = null;
                    try { companionItemClz = Class.forName("iskallia.vault.item.CompanionItem"); } catch (Throwable ignored){}
                    Object opt = findEquipped.invoke(helper, (companionItemClz == null ? Object.class : companionItemClz), player);
                    if (opt instanceof Optional<?>) {
                        Optional<?> o = (Optional<?>) opt;
                        if (o.isPresent()) {
                            Object pair = o.get();

                            try {
                                Method getRight = pair.getClass().getMethod("getRight");
                                Object stack = getRight.invoke(pair);
                                if (stack instanceof ItemStack) out.add((ItemStack)stack);
                            } catch (Throwable ignore) {}
                        }
                    }
                }
            } catch (Throwable ignored) { }

        } catch (ClassNotFoundException cnf) {

        } catch (Throwable t) {
            LOG.warn("Curios reflection error: " + t);
        }
        return out;
    }

    private static Object findVaultForPlayer(ServerPlayer player) {
        try {
            Class<?> svClz = Class.forName("iskallia.vault.world.data.ServerVaults");

            Method m = findMethod(svClz, "get", Level.class);
            if (m != null) {
                Object opt = m.invoke(null, player.level);
                if (opt instanceof Optional<?> o && o.isPresent()) return o.get();
            }
        } catch (Throwable t) {
            LOG.warn("findVaultForPlayer failed: " + t);
        }
        return null;
    }


    private static Method findStaticMethodByParam(Class<?> clz, String name, Class<?>... params) {
        try {
            for (Method m : clz.getDeclaredMethods()) {
                if (m.getName().equals(name) && Modifier.isStatic(m.getModifiers())) {
                    if (params == null || Arrays.equals(m.getParameterTypes(), params)) return m;
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static Method findMethod(Class<?> clz, String name, Class<?>... params) {
        try {
            for (Method m : clz.getMethods()) {
                if (m.getName().equals(name)) {
                    if (params == null || Arrays.equals(m.getParameterTypes(), params)) return m;
                }
            }
            for (Method m : clz.getDeclaredMethods()) {
                if (m.getName().equals(name)) {
                    if (params == null || Arrays.equals(m.getParameterTypes(), params)) { m.setAccessible(true); return m; }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }
}
