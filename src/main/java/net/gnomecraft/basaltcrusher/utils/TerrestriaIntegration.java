package net.gnomecraft.basaltcrusher.utils;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * Lazy loaded Terrestria integration.  Terrestria blocks are not available (via the registry)
 * at mod initialization time so we need to wait until we actually need these values to set them.
 */
@NullMarked
public final class TerrestriaIntegration {
    public static final Block VOLCANIC_GRAVEL_BLOCK;
    public static final BlockItem VOLCANIC_GRAVEL_ITEM;
    public static final Item VOLCANIC_SAND_ITEM;

    public static final Item OBSIDIAN_PILE_ITEM;
    public static final Item OBSIDIAN_SHARD_ITEM;

    public static final boolean ENABLED;

    public static final TagKey<Item> TERRESTRIA_BASALTS;

    static {
        VOLCANIC_SAND_ITEM = BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath("terrestria", "volcanic_sand")).orElse(Items.SAND);

        if (BasaltCrusher.extendTerrestria && new ItemStack(Items.SAND).is(VOLCANIC_SAND_ITEM)) {
            // Safety mechanism in case the registry fails to cough up the Terrestria block.
            BasaltCrusher.LOGGER.warn("Disabling Terrestria integration: 'terrestria:volcanic_sand' is not present in the Item registry.");
            ENABLED = false;
        } else {
            ENABLED = BasaltCrusher.extendTerrestria;
        }

        if (ENABLED) {
            VOLCANIC_GRAVEL_BLOCK = Objects.requireNonNull(BasaltCrusher.VOLCANIC_GRAVEL_BLOCK);
            VOLCANIC_GRAVEL_ITEM = Objects.requireNonNull(BasaltCrusher.VOLCANIC_GRAVEL_ITEM);
            OBSIDIAN_PILE_ITEM = Objects.requireNonNull(BasaltCrusher.OBSIDIAN_PILE_ITEM);
            OBSIDIAN_SHARD_ITEM = Objects.requireNonNull(BasaltCrusher.OBSIDIAN_SHARD_ITEM);
            TERRESTRIA_BASALTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(BasaltCrusher.MOD_ID, "terrestria_basalts"));
        } else {
            // Have some coal in your stocking.  (Hey at least it's not null!)
            VOLCANIC_GRAVEL_BLOCK = Blocks.COAL_BLOCK;
            VOLCANIC_GRAVEL_ITEM = (BlockItem) Items.COAL_BLOCK;
            OBSIDIAN_PILE_ITEM = Items.COAL;
            OBSIDIAN_SHARD_ITEM = Items.COAL;
            TERRESTRIA_BASALTS = ItemTags.COALS;
        }
    }
}
