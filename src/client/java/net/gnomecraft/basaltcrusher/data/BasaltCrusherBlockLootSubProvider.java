package net.gnomecraft.basaltcrusher.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
public class BasaltCrusherBlockLootSubProvider extends FabricBlockLootSubProvider {
	protected BasaltCrusherBlockLootSubProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	public void generate() {
		// Terrestria integration is always registered when running datagen
		assert BasaltCrusher.OBSIDIAN_SHARD_ITEM != null;
		assert BasaltCrusher.VOLCANIC_GRAVEL_BLOCK != null;

		HolderLookup.RegistryLookup<Enchantment> enchantments = this.registries.lookupOrThrow(Registries.ENCHANTMENT);

		// outputs block loot tables which only load when Terrestria is present
		BlockLootSubProvider outputWithTerrestria = withConditions(ResourceConditions.allModsLoaded("terrestria"));

		// simple blocks
		dropSelf(BasaltCrusher.BASALT_CRUSHER_BLOCK);
		dropSelf(BasaltCrusher.GRIZZLY_BLOCK);
		dropSelf(BasaltCrusher.GRAVEL_MILL_BLOCK);

		// Terrestria integration Black Gravel
		outputWithTerrestria.add(
				BasaltCrusher.VOLCANIC_GRAVEL_BLOCK, block ->
						this.createSilkTouchDispatchTable(
								block,
								this.applyExplosionCondition(
										block,
										LootItem.lootTableItem(BasaltCrusher.OBSIDIAN_SHARD_ITEM)
												.when(BonusLevelTableCondition.bonusLevelFlatChance(
														enchantments.getOrThrow(Enchantments.FORTUNE),
														0.1F, 0.14285715F, 0.25F, 1.0F))
												.otherwise(LootItem.lootTableItem(block))
								)
						)
		);
	}

	@Override
	public String getName() {
		return "Basalt Crusher Block Loot Tables";
	}
}
