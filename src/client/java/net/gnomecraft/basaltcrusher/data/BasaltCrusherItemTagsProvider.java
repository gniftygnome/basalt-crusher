package net.gnomecraft.basaltcrusher.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
public class BasaltCrusherItemTagsProvider extends FabricTagsProvider.ItemTagsProvider {
	protected BasaltCrusherItemTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture, BlockTagsProvider blockTagProvider) {
		super(output, registriesFuture, blockTagProvider);
	}

	@Override
	public void addTags(HolderLookup.Provider registries) {
		/*
		 * Vanilla item tags
		 */
		tag(ItemTags.DURABILITY_ENCHANTABLE)
				.addTag(BasaltCrusher.JAW_LINERS);

		tag(ItemTags.MINING_ENCHANTABLE)
				.addTag(BasaltCrusher.JAW_LINERS);


		/*
		 * Conventional item tags
		 */
		tag(BasaltCrusher.BASALTS)
				.add(BlockItemIds.BASALT.item())
				.add(BlockItemIds.POLISHED_BASALT.item())
				.add(BlockItemIds.SMOOTH_BASALT.item());

		copy(ConventionalBlockTags.GRAVELS, ConventionalItemTags.GRAVELS);


		/*
		 * Local item tags
		 */
		tag(BasaltCrusher.JAW_LINERS)
				.add(BasaltCrusher.IRON_JAW_LINER_ID)
				.add(BasaltCrusher.DIAMOND_JAW_LINER_ID)
				.add(BasaltCrusher.NETHERITE_JAW_LINER_ID);

		// this works around not wanting to wait for Terrestria to be available when updating
		builder(BasaltCrusher.TERRESTRIA_BASALTS)
				.addOptional(terrestriaResourceKeyOf("volcanic_rock"))
				.addOptional(terrestriaResourceKeyOf("volcanic_rock_bricks"))
				.addOptional(terrestriaResourceKeyOf("volcanic_cobblestone"))
				.addOptional(terrestriaResourceKeyOf("chiseled_volcanic_rock_bricks"))
				.addOptional(terrestriaResourceKeyOf("cracked_volcanic_rock_bricks"))
				.addOptional(terrestriaResourceKeyOf("mossy_volcanic_rock_bricks"))
				.addOptional(terrestriaResourceKeyOf("mossy_volcanic_cobblestone"))
				.addOptional(terrestriaResourceKeyOf("smooth_volcanic_rock"));
	}

	private static ResourceKey<Item> terrestriaResourceKeyOf(String path) {
		return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("terrestria", path));
	}

	@Override
	public String getName() {
		return "Basalt Crusher Item Tags";
	}
}
