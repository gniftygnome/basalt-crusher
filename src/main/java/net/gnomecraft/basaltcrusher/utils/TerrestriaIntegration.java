package net.gnomecraft.basaltcrusher.utils;

import com.terraformersmc.terrestria.init.TerrestriaBlocks;
import com.terraformersmc.terrestria.init.TerrestriaItems;
import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

/**
 * This class provides a local interface to Terrestria-dependent blocks, so:
 * - Our other code does not directly reference Terrestria classes
 * - Our code has null-safe access to these blocks
 */
@NullMarked
@SuppressWarnings("unused")
public final class TerrestriaIntegration {
    // from Basalt Crusher
    public static final Item OBSIDIAN_PILE_ITEM = Objects.requireNonNull(BasaltCrusher.OBSIDIAN_PILE_ITEM);
    public static final Item OBSIDIAN_SHARD_ITEM = Objects.requireNonNull(BasaltCrusher.OBSIDIAN_SHARD_ITEM);

    public static final Block VOLCANIC_GRAVEL_BLOCK = Objects.requireNonNull(BasaltCrusher.VOLCANIC_GRAVEL_BLOCK);
    public static final Item VOLCANIC_GRAVEL_ITEM = Objects.requireNonNull(BasaltCrusher.VOLCANIC_GRAVEL_ITEM);

    // from Terrestria
    public static final Block VOLCANIC_SAND_BLOCK = TerrestriaBlocks.VOLCANIC_SAND;
    public static final Item VOLCANIC_SAND_ITEM = TerrestriaItems.VOLCANIC_SAND;
}
