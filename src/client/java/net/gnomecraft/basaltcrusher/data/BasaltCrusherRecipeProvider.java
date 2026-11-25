package net.gnomecraft.basaltcrusher.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
public class BasaltCrusherRecipeProvider extends FabricRecipeProvider {
	protected BasaltCrusherRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	public RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
		return new RecipeProvider(registryLookup, exporter) {
			@Override
			public void buildRecipes() {
				// Terrestria integration blocks/items are always registered when running datagen
				assert BasaltCrusher.OBSIDIAN_SHARD_ITEM != null;
				assert BasaltCrusher.OBSIDIAN_PILE_ITEM != null;

				// outputs recipes and advancements which only load when Terrestria is present
				RecipeOutput outputWithTerrestria = withConditions(output, ResourceConditions.allModsLoaded("terrestria"));


				// machine blocks
				shaped(RecipeCategory.DECORATIONS, BasaltCrusher.BASALT_CRUSHER_ITEM)
						.pattern("iri")
						.pattern("ipi")
						.pattern("ipi")
						.define('i', Items.IRON_INGOT)
						.define('p', Items.PISTON)
						.define('r', Items.REDSTONE)
						.unlockedBy("has_iron", has(Items.IRON_INGOT))
						.save(output);

				shaped(RecipeCategory.DECORATIONS, BasaltCrusher.GRIZZLY_ITEM)
						.pattern(" i ")
						.pattern("ibi")
						.pattern(" i ")
						.define('b', Items.IRON_BARS)
						.define('i', Items.IRON_INGOT)
						.unlockedBy("has_iron", has(Items.IRON_INGOT))
						.save(output);

				shaped(RecipeCategory.DECORATIONS, BasaltCrusher.GRAVEL_MILL_ITEM)
						.pattern("iii")
						.pattern("i i")
						.pattern("iri")
						.define('i', Items.IRON_INGOT)
						.define('r', Items.REDSTONE)
						.unlockedBy("has_iron", has(Items.IRON_INGOT))
						.save(output);


				// consumable machine inputs
				shaped(RecipeCategory.TOOLS, BasaltCrusher.IRON_JAW_LINER_ITEM)
						.pattern("iii")
						.pattern("III")
						.pattern(" i ")
						.define('i', Items.IRON_INGOT)
						.define('I', Items.IRON_BLOCK)
						.unlockedBy("has_iron", has(Items.IRON_INGOT))
						.save(output);

				shaped(RecipeCategory.TOOLS, BasaltCrusher.DIAMOND_JAW_LINER_ITEM)
						.pattern("ddd")
						.pattern("III")
						.pattern(" i ")
						.define('d', Items.DIAMOND)
						.define('I', Items.IRON_BLOCK)
						.define('i', Items.IRON_INGOT)
						.unlockedBy("has_diamonds", has(Items.DIAMOND))
						.save(output);

				shaped(RecipeCategory.TOOLS, BasaltCrusher.NETHERITE_JAW_LINER_ITEM)
						.pattern("nnn")
						.pattern("III")
						.pattern(" i ")
						.define('n', Items.NETHERITE_INGOT)
						.define('I', Items.IRON_BLOCK)
						.define('i', Items.IRON_INGOT)
						.unlockedBy("has_netherite", has(Items.NETHERITE_INGOT))
						.save(output);

				netheriteSmithing(BasaltCrusher.DIAMOND_JAW_LINER_ITEM, RecipeCategory.TOOLS, BasaltCrusher.NETHERITE_JAW_LINER_ITEM);

				shaped(RecipeCategory.TOOLS, BasaltCrusher.MILL_ROD_CHARGE_ITEM)
						.pattern(" i ")
						.pattern(" i ")
						.pattern(" i ")
						.define('i', Items.IRON_INGOT)
						.unlockedBy("has_iron", has(Items.IRON_INGOT))
						.save(output);


				// misc. obsidian items
				shaped(RecipeCategory.COMBAT, Items.ARROW)
						.pattern("O")
						.pattern("S")
						.pattern("F")
						.define('O', BasaltCrusher.OBSIDIAN_SHARD_ITEM)
						.define('S', Items.STICK)
						.define('F', Items.FEATHER)
						.unlockedBy("has_shards", has(BasaltCrusher.OBSIDIAN_SHARD_ITEM))
						.save(outputWithTerrestria);

				shapeless(RecipeCategory.TOOLS, Items.FLINT_AND_STEEL)
						.requires(BasaltCrusher.OBSIDIAN_SHARD_ITEM)
						.requires(Items.IRON_INGOT)
						.unlockedBy("has_shards", has(BasaltCrusher.OBSIDIAN_SHARD_ITEM))
						.save(outputWithTerrestria);


				// packing/unpacking obsidian shards to/from obsidian pile
				// (have to do this the hard way to set a resource condition)
				shapeless(RecipeCategory.MISC, BasaltCrusher.OBSIDIAN_SHARD_ITEM, 9)
						.requires(BasaltCrusher.OBSIDIAN_PILE_ITEM)
						.group(null)
						.unlockedBy(getHasName(BasaltCrusher.OBSIDIAN_PILE_ITEM), has(BasaltCrusher.OBSIDIAN_PILE_ITEM))
						.save(outputWithTerrestria, ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(BasaltCrusher.OBSIDIAN_SHARD_ITEM)));

				shaped(RecipeCategory.MISC, BasaltCrusher.OBSIDIAN_PILE_ITEM)
						.pattern("sss")
						.pattern("sss")
						.pattern("sss")
						.define('s', BasaltCrusher.OBSIDIAN_SHARD_ITEM)
						.group(null)
						.unlockedBy(getHasName(BasaltCrusher.OBSIDIAN_SHARD_ITEM), has(BasaltCrusher.OBSIDIAN_SHARD_ITEM))
						.save(outputWithTerrestria, ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(BasaltCrusher.OBSIDIAN_PILE_ITEM)));


				// blasting obsidian pile into obsidian block
				// (have to do this the hard way to set a resource condition)
				SimpleCookingRecipeBuilder.generic(
						Ingredient.of(BasaltCrusher.OBSIDIAN_PILE_ITEM),
						RecipeCategory.BUILDING_BLOCKS,
						CookingBookCategory.BLOCKS,
						Items.OBSIDIAN,
						1,
						200,
						BlastingRecipe::new)
						.group(null)
						.unlockedBy(getHasName(BasaltCrusher.OBSIDIAN_SHARD_ITEM), has(BasaltCrusher.OBSIDIAN_SHARD_ITEM))
						.save(outputWithTerrestria, getItemName(Items.OBSIDIAN) + "_from_blasting_" + getItemName(BasaltCrusher.OBSIDIAN_SHARD_ITEM));

			}
		};
	}

	@Override
	public String getName() {
		return "Basalt Crusher Recipes";
	}

	@Override
	protected Identifier getRecipeIdentifier(Identifier identifier) {
		return Identifier.fromNamespaceAndPath(BasaltCrusher.MOD_ID, identifier.getPath());
	}
}
