package net.qiyanamark.companionpouch.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class CompanionModifierUtil {

    private static final ResourceLocation COMPANION_CURSE = new ResourceLocation("the_vault", "companion_curse");
    private static final ResourceLocation ROTTEN = new ResourceLocation("the_vault", "rotten");

    public static void serverOpenAllTemporal(Player player) {
        if (!(player instanceof ServerPlayer sp)) return;

        ItemStack pouch = findEquippedPouch(player);
        if (pouch.isEmpty()) return;

        IItemHandler handler = pouch.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
        if (handler == null) return;

        List<ItemStack> companions = new ArrayList<>();
        for (int s = 0; s < handler.getSlots(); s++) {
            ItemStack st = handler.getStackInSlot(s);
            if (!st.isEmpty()) companions.add(st);
        }
        if (companions.isEmpty()) return;

        try {
            // ServerVaults.get(Level) -> Optional<Vault>
            Class<?> serverVaultsCls = Class.forName("iskallia.vault.world.data.ServerVaults");
            Method serverVaultsGet = serverVaultsCls.getMethod("get", net.minecraft.world.level.Level.class);
            Optional<?> vaultOpt = (Optional<?>) serverVaultsGet.invoke(null, player.level);
            if (vaultOpt == null || !vaultOpt.isPresent()) return;
            Object vault = vaultOpt.get();

            // CompanionItem methods
            Class<?> companionItemCls = Class.forName("iskallia.vault.item.CompanionItem");
            Method getTemporalOpt = null;
            Method activateTemporal = null;
            try {
                getTemporalOpt = companionItemCls.getMethod("getTemporalModifier", ItemStack.class);
            } catch (NoSuchMethodException ignored) {
                // maybe name differs; try alternative names found in jar constants
                for (Method m : companionItemCls.getDeclaredMethods()) {
                    if (m.getName().toLowerCase().contains("temporal") && m.getParameterCount() == 1) {
                        getTemporalOpt = m; break;
                    }
                }
            }
            activateTemporal = companionItemCls.getMethod("activateTemporalModifier", ServerPlayer.class, ItemStack.class, Class.forName("iskallia.vault.core.vault.Vault"));

            // Collect temporal ids for duplicate check
            List<ResourceLocation> temporalIds = new ArrayList<>();
            for (ItemStack comp : companions) {
                ResourceLocation id = null;
                if (getTemporalOpt != null) {
                    try {
                        Object opt = getTemporalOpt.invoke(null, comp); // Optional<?>
                        if (opt instanceof Optional<?> o && o.isPresent()) {
                            Object vm = o.get();
                            Method getId = vm.getClass().getMethod("getId");
                            id = (ResourceLocation) getId.invoke(vm);
                        }
                    } catch (Throwable t) {
                        // ignore: treat as no temporal
                    }
                }
                temporalIds.add(id);
            }

            // Activate temporals (Vault's own activation)
            for (ItemStack comp : companions) {
                try {
                    activateTemporal.invoke(null, sp, comp, vault);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            // Duplicate detection vs first slot
            int curses = 0;
            if (!temporalIds.isEmpty() && temporalIds.get(0) != null) {
                ResourceLocation base = temporalIds.get(0);
                for (int i = 1; i < temporalIds.size(); i++) {
                    ResourceLocation other = temporalIds.get(i);
                    if (base.equals(other)) curses++;
                }
            }

            // Apply modifiers into Vault
            if (curses > 0) {
                if (curses >= 3) applyModifierToVault(vault, ROTTEN, 1);
                else applyModifierToVault(vault, COMPANION_CURSE, curses);
            }
        } catch (ClassNotFoundException cnf) {
            cnf.printStackTrace();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Reflectively apply modifier into the Vault instance.
     * Uses VaultModifierRegistry to find modifier by id, then vault.getModifiers().addModifier(...)
     * Falls back to scheduling AddVaultModifierTask if required.
     */
    private static void applyModifierToVault(Object vault, ResourceLocation modifierId, int stacks) {
        try {
            Class<?> vaultCls = Class.forName("iskallia.vault.core.vault.Vault");
            // getModifiers() on Vault
            Method getModifiers = null;
            for (Method m : vaultCls.getDeclaredMethods()) {
                if (m.getName().equalsIgnoreCase("getModifiers") && m.getParameterCount() == 0) {
                    getModifiers = m; break;
                }
            }
            Object modifiersInstance = getModifiers != null ? getModifiers.invoke(vault) : null;

            // Find VaultModifier via registry
            Class<?> registryCls = Class.forName("iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry");
            Method getOpt = null;
            try { getOpt = registryCls.getMethod("getOpt", ResourceLocation.class); }
            catch (NoSuchMethodException e) {
                // fallback: try getOrDefault or similar names
                for (Method m : registryCls.getDeclaredMethods()) {
                    if ((m.getName().toLowerCase().contains("get") || m.getName().toLowerCase().contains("find")) && m.getParameterCount() == 1) {
                        getOpt = m; break;
                    }
                }
            }
            Object vaultModifier = null;
            if (getOpt != null) {
                Object opt = getOpt.invoke(null, modifierId);
                if (opt instanceof Optional<?> o && o.isPresent()) vaultModifier = o.get();
            }

            if (vaultModifier == null) {
                System.out.println("[CompanionPouch] Could not find VaultModifier object for " + modifierId);
                // try scheduling AddVaultModifierTask as fallback
                scheduleAddVaultModifierTask(vault, modifierId, stacks);
                return;
            }

            if (modifiersInstance != null) {
                Class<?> modifiersCls = modifiersInstance.getClass();
                // Try to find addModifier method
                Method addMod = null;
                for (Method m : modifiersCls.getDeclaredMethods()) {
                    if (m.getName().toLowerCase().contains("addmodifier")) { addMod = m; break; }
                }
                if (addMod != null) {
                    addMod.setAccessible(true);
                    // Try several invocation signatures
                    try {
                        // common signature: addModifier(VaultModifier, int, boolean, RandomSource)
                        Class<?> randomSrcCls = Class.forName("net.minecraft.core.random.RandomSource");
                        Method createRS = null;
                        Object rand = null;
                        try {
                            createRS = randomSrcCls.getMethod("create");
                            rand = createRS.invoke(null);
                        } catch (NoSuchMethodException e) {
                            // try RandomSource.create(long)
                            try {
                                createRS = randomSrcCls.getMethod("create", long.class);
                                rand = createRS.invoke(null, System.currentTimeMillis());
                            } catch (NoSuchMethodException ex) {
                                rand = null;
                            }
                        }
                        // try (VaultModifier, int, boolean, RandomSource)
                        try {
                            addMod.invoke(modifiersInstance, vaultModifier, stacks, Boolean.TRUE, rand);
                            return;
                        } catch (Throwable t) {
                            // try (VaultModifier, int, RandomSource)
                        }
                        try {
                            addMod.invoke(modifiersInstance, vaultModifier, stacks, rand);
                            return;
                        } catch (Throwable tt) {
                            // try (VaultModifier, RandomSource)
                        }
                        try {
                            addMod.invoke(modifiersInstance, vaultModifier);
                            return;
                        } catch (Throwable ttt) {
                            // fallback
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }

            // If we reached here, fallback to scheduling AddVaultModifierTask
            scheduleAddVaultModifierTask(vault, modifierId, stacks);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Create & schedule AddVaultModifierTask reflectively as fallback.
     * This method attempts to construct AddVaultModifierTask.Config and instantiate the task,
     * then run it (or schedule it) using Vault's task API.
     */
    private static void scheduleAddVaultModifierTask(Object vault, ResourceLocation modifierId, int stacks) {
        try {
            Class<?> taskCls = Class.forName("iskallia.vault.task.AddVaultModifierTask");
            Class<?> configCls = Class.forName("iskallia.vault.task.AddVaultModifierTask$Config");
            // Try constructor Config(ResourceLocation id, int stacks) or builder-like fields
            Object cfg = null;
            try {
                try {
                    Constructor<?> ctor = configCls.getDeclaredConstructor(ResourceLocation.class, int.class);
                    ctor.setAccessible(true);
                    cfg = ctor.newInstance(modifierId, stacks);
                } catch (NoSuchMethodException nms) {
                    // try default ctor and set fields by reflection
                    Constructor<?> c2 = configCls.getDeclaredConstructor();
                    c2.setAccessible(true);
                    cfg = c2.newInstance();
                    // set fields if available
                    try {
                        Field modField = configCls.getDeclaredField("modifier");
                        modField.setAccessible(true);
                        modField.set(cfg, modifierId.toString());
                    } catch (NoSuchFieldException ignored) {}
                    try {
                        Field stacksField = configCls.getDeclaredField("stacks");
                        stacksField.setAccessible(true);
                        stacksField.setInt(cfg, stacks);
                    } catch (NoSuchFieldException ignored) {}
                }
            } catch (Throwable e) {
                e.printStackTrace();
                cfg = null;
            }

            if (cfg == null) {
                System.out.println("[CompanionPouch] Could not construct AddVaultModifierTask.Config for " + modifierId);
                return;
            }

            // Instantiate the task with config
            Constructor<?> taskCtor = taskCls.getDeclaredConstructor(configCls);
            taskCtor.setAccessible(true);
            Object task = taskCtor.newInstance(cfg);

            // Try to run the task immediately if there's a run() method, else try schedule via Vault tasks manager
            try {
                Method run = taskCls.getMethod("run");
                run.invoke(task);
                return;
            } catch (NoSuchMethodException ns) {
                // find scheduler
                try {
                    Class<?> taskManagerCls = Class.forName("iskallia.vault.task.TaskManager");
                    Method schedule = taskManagerCls.getMethod("schedule", taskCls);
                    schedule.invoke(null, task);
                    return;
                } catch (Throwable tx) {
                    tx.printStackTrace();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }

        } catch (ClassNotFoundException cnf) {
            cnf.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static ItemStack findEquippedPouch(Player player) {
        final ItemStack[] result = {ItemStack.EMPTY};
        try {
            CuriosApi.getCuriosHelper().findCurios(player, stack -> {
                String id = stack.getItem().toString().toLowerCase();
                return id.contains("companion_pouch") || id.endsWith("companion_pouch");
            }).ifPresent(list -> {
                for (SlotResult sr : list) {
                    if ("head".equals(sr.slotContext().identifier())) { result[0] = sr.stack(); return; }
                    if (result[0].isEmpty()) result[0] = sr.stack();
                }
            });
        } catch (Throwable ignored) {}
        if (!result[0].isEmpty()) return result[0];
        if (player.getMainHandItem().getItem().toString().toLowerCase().contains("companion_pouch")) return player.getMainHandItem();
        if (player.getOffhandItem().getItem().toString().toLowerCase().contains("companion_pouch")) return player.getOffhandItem();
        return ItemStack.EMPTY;
    }
}