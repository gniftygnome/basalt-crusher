package net.gnomecraft.basaltcrusher.utils;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Lazy loaded Terrestria integration.  Terrestria blocks are not available (via the registry)
 * at mod initialization time so we need to wait until we actually need these values to set them.
 */
public final class TerrestriaIntegration {
    public static final Block BLACK_GRAVEL_BLOCK;
    public static final BlockItem BLACK_GRAVEL_ITEM;
    public static final Item BLACK_SAND_ITEM;

    public static final Item OBSIDIAN_PILE_ITEM;
    public static final Item OBSIDIAN_SHARD_ITEM;

    public static final boolean ENABLED;

    public static final TagKey<Item> TERRESTRIA_BASALTS;

    static {
        BLACK_SAND_ITEM = Registry.ITEM.getOrEmpty(Identifier.of("terrestria", "basalt_sand")).orElse(Items.SAND);

        if (BasaltCrusher.extendTerrestria && new ItemStack(Items.SAND).isOf(BLACK_SAND_ITEM)) {
            // Safety mechanism in case the registry fails to cough up the Terrestria block.
            BasaltCrusher.LOGGER.warn("Disabling Terrestria integration: 'terrestria:basalt_sand' is not present in the Item registry.");
            ENABLED = false;
        } else {
            ENABLED = BasaltCrusher.extendTerrestria;
        }

        if (ENABLED) {
            BLACK_GRAVEL_BLOCK = BasaltCrusher.BLACK_GRAVEL_BLOCK;
            BLACK_GRAVEL_ITEM = BasaltCrusher.BLACK_GRAVEL_ITEM;
            OBSIDIAN_PILE_ITEM = BasaltCrusher.OBSIDIAN_PILE_ITEM;
            OBSIDIAN_SHARD_ITEM = BasaltCrusher.OBSIDIAN_SHARD_ITEM;
            TERRESTRIA_BASALTS = TagKey.of(Registry.ITEM_KEY, new Identifier("basalt-crusher", "terrestria_basalts"));
        } else {
            // Have some coal in your stocking.  (Hey at least it's not null!)
            BLACK_GRAVEL_BLOCK = Blocks.COAL_BLOCK;
            BLACK_GRAVEL_ITEM = (BlockItem) Items.COAL_BLOCK;
            OBSIDIAN_PILE_ITEM = Items.COAL;
            OBSIDIAN_SHARD_ITEM = Items.COAL;
            TERRESTRIA_BASALTS = ItemTags.COALS;
        }
    }
}
