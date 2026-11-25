package net.gnomecraft.basaltcrusher;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.gnomecraft.basaltcrusher.crusher.BasaltCrusherBlock;
import net.gnomecraft.basaltcrusher.crusher.BasaltCrusherEntity;
import net.gnomecraft.basaltcrusher.crusher.BasaltCrusherScreenHandler;
import net.gnomecraft.basaltcrusher.grizzly.GrizzlyBlock;
import net.gnomecraft.basaltcrusher.grizzly.GrizzlyEntity;
import net.gnomecraft.basaltcrusher.grizzly.GrizzlyScreenHandler;
import net.gnomecraft.basaltcrusher.mill.GravelMillBlock;
import net.gnomecraft.basaltcrusher.mill.GravelMillEntity;
import net.gnomecraft.basaltcrusher.mill.GravelMillScreenHandler;
import net.gnomecraft.basaltcrusher.utils.BasaltCrusherRegistryAliases;
import net.minecraft.references.BlockItemId;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ColorRGBA;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.MapColor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class BasaltCrusher implements ModInitializer {
    public static final String MOD_ID = "basalt_crusher";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ResourceKey<Item> OBSIDIAN_PILE_ID = itemId("obsidian_pile");
    public static final ResourceKey<Item> OBSIDIAN_SHARD_ID = itemId("obsidian_shard");
    public static final BlockItemId VOLCANIC_GRAVEL_BLOCK_ID = blockItemId("volcanic_gravel");

    public static final BlockItemId BASALT_CRUSHER_BLOCK_ID = blockItemId("basalt_crusher");
    public static final BlockItemId GRIZZLY_BLOCK_ID = blockItemId("grizzly");
    public static final BlockItemId GRAVEL_MILL_BLOCK_ID = blockItemId("gravel_mill");
    public static final ResourceKey<Item> IRON_JAW_LINER_ID = itemId("iron_jaw_liner");
    public static final ResourceKey<Item> DIAMOND_JAW_LINER_ID = itemId("diamond_jaw_liner");
    public static final ResourceKey<Item> NETHERITE_JAW_LINER_ID = itemId("netherite_jaw_liner");
    public static final ResourceKey<Item> MILL_ROD_CHARGE_ID = itemId("mill_rod_charge");

    public static final TagKey<Item> BASALTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "basalts"));
    public static final TagKey<Item> JAW_LINERS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "jaw_liners"));
    public static final TagKey<Item> TERRESTRIA_BASALTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "terrestria_basalts"));

    public static final Identifier BASALT_CRUSHER_SOUND_ID = Identifier.fromNamespaceAndPath(MOD_ID, "basalt_crusher_sound");
    public static final Identifier GRAVEL_MILL_SOUND_ID = Identifier.fromNamespaceAndPath(MOD_ID,"gravel_mill_sound");
    public static final SoundEvent BASALT_CRUSHER_SOUND_EVENT = SoundEvent.createVariableRangeEvent(BASALT_CRUSHER_SOUND_ID);
    public static final SoundEvent GRAVEL_MILL_SOUND_EVENT = SoundEvent.createVariableRangeEvent(GRAVEL_MILL_SOUND_ID);

    // Basalt Crusher block
    public static final Block BASALT_CRUSHER_BLOCK = Registry.register(BuiltInRegistries.BLOCK, BASALT_CRUSHER_BLOCK_ID.block(), new BasaltCrusherBlock(BlockBehaviour.Properties.of().setId(BASALT_CRUSHER_BLOCK_ID.block()).mapColor(MapColor.METAL).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.f, 4.8f).sound(SoundType.METAL)));
    public static final BlockItem BASALT_CRUSHER_ITEM = Registry.register(BuiltInRegistries.ITEM, BASALT_CRUSHER_BLOCK_ID.item(), new BlockItem(BASALT_CRUSHER_BLOCK, new net.minecraft.world.item.Item.Properties().setId(BASALT_CRUSHER_BLOCK_ID.item()).useBlockDescriptionPrefix()));
    public static final BlockEntityType<BasaltCrusherEntity> BASALT_CRUSHER_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, BASALT_CRUSHER_BLOCK_ID.block().identifier(), FabricBlockEntityTypeBuilder.create(BasaltCrusherEntity::new, BASALT_CRUSHER_BLOCK).build());
    public static final MenuType<BasaltCrusherScreenHandler> BASALT_CRUSHER_SCREEN_HANDLER = Registry.register(BuiltInRegistries.MENU, BASALT_CRUSHER_BLOCK_ID.block().identifier(), new MenuType<>(BasaltCrusherScreenHandler::new, FeatureFlagSet.of()));

    // Grizzly block
    public static final Block GRIZZLY_BLOCK = Registry.register(BuiltInRegistries.BLOCK, GRIZZLY_BLOCK_ID.block(), new GrizzlyBlock(BlockBehaviour.Properties.of().setId(GRIZZLY_BLOCK_ID.block()).mapColor(MapColor.METAL).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0f, 4.8f).sound(SoundType.METAL).noOcclusion()));
    public static final BlockItem GRIZZLY_ITEM = Registry.register(BuiltInRegistries.ITEM, GRIZZLY_BLOCK_ID.item(), new BlockItem(GRIZZLY_BLOCK, new net.minecraft.world.item.Item.Properties().setId(GRIZZLY_BLOCK_ID.item()).useBlockDescriptionPrefix()));
    public static final BlockEntityType<GrizzlyEntity> GRIZZLY_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, GRIZZLY_BLOCK_ID.block().identifier(), FabricBlockEntityTypeBuilder.create(GrizzlyEntity::new, GRIZZLY_BLOCK).build());
    public static final MenuType<GrizzlyScreenHandler> GRIZZLY_SCREEN_HANDLER = Registry.register(BuiltInRegistries.MENU, GRIZZLY_BLOCK_ID.block().identifier(), new MenuType<>(GrizzlyScreenHandler::new, FeatureFlagSet.of()));

    // Gravel Mill block
    public static final Block GRAVEL_MILL_BLOCK = Registry.register(BuiltInRegistries.BLOCK, GRAVEL_MILL_BLOCK_ID.block(), new GravelMillBlock(BlockBehaviour.Properties.of().setId(GRAVEL_MILL_BLOCK_ID.block()).mapColor(MapColor.METAL).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0f, 4.8f).sound(SoundType.METAL)));
    public static final BlockItem GRAVEL_MILL_ITEM = Registry.register(BuiltInRegistries.ITEM, GRAVEL_MILL_BLOCK_ID.item(), new BlockItem(GRAVEL_MILL_BLOCK, new net.minecraft.world.item.Item.Properties().setId(GRAVEL_MILL_BLOCK_ID.item()).useBlockDescriptionPrefix()));
    public static final BlockEntityType<GravelMillEntity> GRAVEL_MILL_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, GRAVEL_MILL_BLOCK_ID.block().identifier(), FabricBlockEntityTypeBuilder.create(GravelMillEntity::new, GRAVEL_MILL_BLOCK).build());
    public static final MenuType<GravelMillScreenHandler> GRAVEL_MILL_SCREEN_HANDLER = Registry.register(BuiltInRegistries.MENU, GRAVEL_MILL_BLOCK_ID.block().identifier(), new MenuType<>(GravelMillScreenHandler::new, FeatureFlagSet.of()));

    // Basalt Crusher Jaw Liners
    public static final Item IRON_JAW_LINER_ITEM      = Registry.register(BuiltInRegistries.ITEM, IRON_JAW_LINER_ID,      new Item(ToolMaterial.IRON     .applyToolProperties(new net.minecraft.world.item.Item.Properties().setId(IRON_JAW_LINER_ID),      BlockTags.AIR, 0.0f, -3.0f, 0.0f)));
    public static final Item DIAMOND_JAW_LINER_ITEM   = Registry.register(BuiltInRegistries.ITEM, DIAMOND_JAW_LINER_ID,   new Item(ToolMaterial.DIAMOND  .applyToolProperties(new net.minecraft.world.item.Item.Properties().setId(DIAMOND_JAW_LINER_ID),   BlockTags.AIR, 0.0f, -3.0f, 0.0f)));
    public static final Item NETHERITE_JAW_LINER_ITEM = Registry.register(BuiltInRegistries.ITEM, NETHERITE_JAW_LINER_ID, new Item(ToolMaterial.NETHERITE.applyToolProperties(new net.minecraft.world.item.Item.Properties().setId(NETHERITE_JAW_LINER_ID), BlockTags.AIR, 0.0f, -3.0f, 0.0f)));

    // Gravel Mill Rod Charge
    public static final Item MILL_ROD_CHARGE_ITEM = Registry.register(BuiltInRegistries.ITEM, MILL_ROD_CHARGE_ID, new Item(ToolMaterial.IRON.applyToolProperties(new net.minecraft.world.item.Item.Properties().setId(MILL_ROD_CHARGE_ID), BlockTags.AIR, 0.0f, -3.0f, 0.0f)));

    /*
     * These variables are initialized when Terrestria is present (extendTerrestria == true).
     */
    public static boolean extendTerrestria = false;

    public static @Nullable Item OBSIDIAN_PILE_ITEM;
    public static @Nullable Item OBSIDIAN_SHARD_ITEM;
    public static @Nullable Block VOLCANIC_GRAVEL_BLOCK;
    public static @Nullable BlockItem VOLCANIC_GRAVEL_ITEM;

    @Override
    public void onInitialize() {
        LOGGER.info("Basalt Crusher block is hungry...");

        // Extend Terrestria if it's present
        if (FabricLoader.getInstance().isModLoaded("terrestria")) {
            LOGGER.debug("Enabling Terrestria integration...");
            extendTerrestria = true;
        }

        // Register Terrestria integrations if we're extending Terrestria OR running datagen
        if (extendTerrestria || System.getProperty("fabric-api.datagen") != null) {
            VOLCANIC_GRAVEL_BLOCK = Registry.register(BuiltInRegistries.BLOCK, VOLCANIC_GRAVEL_BLOCK_ID.block(), new ColoredFallingBlock(new ColorRGBA(0x202020), BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL).setId(VOLCANIC_GRAVEL_BLOCK_ID.block()).mapColor(MapColor.DEEPSLATE)));
            VOLCANIC_GRAVEL_ITEM = Registry.register(BuiltInRegistries.ITEM, VOLCANIC_GRAVEL_BLOCK_ID.item(), new BlockItem(VOLCANIC_GRAVEL_BLOCK, new net.minecraft.world.item.Item.Properties().setId(VOLCANIC_GRAVEL_BLOCK_ID.item()).useBlockDescriptionPrefix()));

            OBSIDIAN_PILE_ITEM = Registry.register(BuiltInRegistries.ITEM, OBSIDIAN_PILE_ID, new Item(new net.minecraft.world.item.Item.Properties().setId(OBSIDIAN_PILE_ID)));
            OBSIDIAN_SHARD_ITEM = Registry.register(BuiltInRegistries.ITEM, OBSIDIAN_SHARD_ID, new Item(new net.minecraft.world.item.Item.Properties().setId(OBSIDIAN_SHARD_ID)));

            // Register Terrestria extension items for Item Groups
            CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS)
                    .register(content -> content.insertAfter(Items.GRAVEL, VOLCANIC_GRAVEL_ITEM));
            CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                    .register(content -> content.insertAfter(Items.FLINT, OBSIDIAN_SHARD_ITEM, OBSIDIAN_PILE_ITEM));
        }

        // Register standard items for Item Groups
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(content -> content.insertAfter(Items.BLAST_FURNACE, BASALT_CRUSHER_ITEM, GRAVEL_MILL_ITEM, GRIZZLY_ITEM));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                .register(content -> content.insertBefore(Items.DYE.white(), IRON_JAW_LINER_ITEM, DIAMOND_JAW_LINER_ITEM, NETHERITE_JAW_LINER_ITEM, MILL_ROD_CHARGE_ITEM));

        // Basalt Crusher Storage
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> direction != null && blockEntity instanceof BasaltCrusherEntity ? ((BasaltCrusherEntity) blockEntity).getSidedStorage(direction) : null, BASALT_CRUSHER_BLOCK);
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> direction != null && blockEntity instanceof GrizzlyEntity ? ((GrizzlyEntity) blockEntity).getSidedStorage(direction) : null, GRIZZLY_BLOCK);
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> direction != null && blockEntity instanceof GravelMillEntity ? ((GravelMillEntity) blockEntity).getSidedStorage(direction) : null, GRAVEL_MILL_BLOCK);

        Registry.register(BuiltInRegistries.SOUND_EVENT, BasaltCrusher.BASALT_CRUSHER_SOUND_ID, BASALT_CRUSHER_SOUND_EVENT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, BasaltCrusher.GRAVEL_MILL_SOUND_ID,    GRAVEL_MILL_SOUND_EVENT);

        // Take care of blocks and items needing s/basalt-crusher/basalt_crusher/ or a change of name
        BasaltCrusherRegistryAliases.init();
    }

    private static BlockItemId blockItemId(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(MOD_ID, path);
        return BlockItemId.create(id, id);
    }

    private static ResourceKey<Item> itemId(String path) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, path));
    }
}