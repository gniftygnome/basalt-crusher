package net.gnomecraft.basaltcrusher.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.core.HolderLookup;
import net.minecraft.references.BlockItemIds;
import net.minecraft.tags.BlockTags;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
public class BasaltCrusherBlockTagsProvider extends FabricTagsProvider.BlockTagsProvider {
	protected BasaltCrusherBlockTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	public void addTags(HolderLookup.Provider registries) {
		/*
		 * Basic block tags
		 */
		tag(BlockTags.MINEABLE_WITH_PICKAXE)
				.add(BasaltCrusher.BASALT_CRUSHER_BLOCK_ID.block())
				.add(BasaltCrusher.GRIZZLY_BLOCK_ID.block())
				.add(BasaltCrusher.GRAVEL_MILL_BLOCK_ID.block());

		tag(BlockTags.MINEABLE_WITH_SHOVEL)
				.addOptional(BasaltCrusher.VOLCANIC_GRAVEL_BLOCK_ID.block());


		/*
		 * Conventional block tags
		 */
		tag(ConventionalBlockTags.GRAVELS)
				.add(BlockItemIds.GRAVEL.block())
				.addOptional(BasaltCrusher.VOLCANIC_GRAVEL_BLOCK_ID.block());
	}

	@Override
	public String getName() {
		return "Basalt Crusher Block Tags";
	}
}
