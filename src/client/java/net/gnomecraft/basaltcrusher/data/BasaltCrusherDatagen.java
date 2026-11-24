package net.gnomecraft.basaltcrusher.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BasaltCrusherDatagen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
		FabricDataGenerator.Pack pack = dataGenerator.createPack();

		pack.addProvider(BasaltCrusherBlockLootSubProvider::new);
		BasaltCrusherBlockTagsProvider blockTagProvider = pack.addProvider(BasaltCrusherBlockTagsProvider::new);
		pack.addProvider((output, registries) -> new BasaltCrusherItemTagsProvider(output, registries, blockTagProvider));
		pack.addProvider(BasaltCrusherRecipeProvider::new);
	}
}
