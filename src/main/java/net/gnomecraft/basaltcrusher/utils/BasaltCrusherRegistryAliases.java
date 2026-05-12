package net.gnomecraft.basaltcrusher.utils;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.Map;

public class BasaltCrusherRegistryAliases {
    @SuppressWarnings("UnnecessaryReturnStatement")
    private BasaltCrusherRegistryAliases() {
        return;
    }

    // Use Fabric registry aliases to repair identifier changes
    public static void init() {
        registerStatic();
    }

    private static void registerStatic() {
        // Blocks with identically ID'd items
        Map<Identifier, Identifier> BLOCKS_ITEMS = Map.ofEntries(
                entry("basalt_crusher"),
                entry("grizzly"),
                entry("gravel_mill")
        );
        BLOCKS_ITEMS.forEach(BuiltInRegistries.BLOCK::addAlias);
        BLOCKS_ITEMS.forEach(BuiltInRegistries.ITEM::addAlias);

        // Items without an identically-named block
        Map<Identifier, Identifier> ITEMS = Map.ofEntries(
                entry("iron_jaw_liner"),
                entry("diamond_jaw_liner"),
                entry("netherite_jaw_liner"),
                entry("mill_rod_charge")
        );
        ITEMS.forEach(BuiltInRegistries.ITEM::addAlias);

        // Only when Terrestria is present
        if (TerrestriaIntegration.ENABLED) {
            Map<Identifier, Identifier> TERRESTRIA_ITEMS = Map.ofEntries(
                    entry("obsidian_pile"),
                    entry("obsidian_shard"),
                    entry("volcanic_gravel")
            );
            TERRESTRIA_ITEMS.forEach(BuiltInRegistries.ITEM::addAlias);
        }
    }

    // Changes the mod ID from the old to new
    private static Map.Entry<Identifier, Identifier> entry(String name) {
        return Map.entry(Identifier.fromNamespaceAndPath("basalt-crusher", name), Identifier.fromNamespaceAndPath(BasaltCrusher.MOD_ID, name));
    }

    // Changes the name without changing the mod ID
    private static Map.Entry<Identifier, Identifier> entry(String oldName, String newName) {
        return Map.entry(Identifier.fromNamespaceAndPath(BasaltCrusher.MOD_ID, oldName), Identifier.fromNamespaceAndPath(BasaltCrusher.MOD_ID, newName));
    }
}
