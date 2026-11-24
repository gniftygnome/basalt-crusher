package net.gnomecraft.basaltcrusher.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
public class BasaltCrusherBlockTagsProvider extends FabricTagsProvider.BlockTagsProvider {
	protected BasaltCrusherBlockTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	public void addTags(HolderLookup.Provider registries) {
		// Terrestria integration is always registered when running datagen
		assert BasaltCrusher.VOLCANIC_GRAVEL_BLOCK != null;

		/*
		 * Basic block tags
		 */
		valueLookupBuilder(BlockTags.MINEABLE_WITH_PICKAXE)
				.add(BasaltCrusher.BASALT_CRUSHER_BLOCK)
				.add(BasaltCrusher.GRIZZLY_BLOCK)
				.add(BasaltCrusher.GRAVEL_MILL_BLOCK);

		valueLookupBuilder(BlockTags.MINEABLE_WITH_SHOVEL)
				.addOptional(BasaltCrusher.VOLCANIC_GRAVEL_BLOCK);


		/*
		 * Conventional block tags
		 */
		valueLookupBuilder(ConventionalBlockTags.GRAVELS)
				.add(Blocks.GRAVEL)
				.addOptional(BasaltCrusher.VOLCANIC_GRAVEL_BLOCK);
	}

	@Override
	public String getName() {
		return "Basalt Crusher Block Tags";
	}
}
