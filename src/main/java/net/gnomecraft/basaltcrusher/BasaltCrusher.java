package net.gnomecraft.basaltcrusher;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
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

    public static final Identifier ObsidianPileId = Identifier.fromNamespaceAndPath(MOD_ID, "obsidian_pile");
    public static final Identifier ObsidianShardId = Identifier.fromNamespaceAndPath(MOD_ID, "obsidian_shard");
    public static final Identifier VolcanicGravelBlockId = Identifier.fromNamespaceAndPath(MOD_ID, "volcanic_gravel");

    public static final Identifier BasaltCrusherBlockId = Identifier.fromNamespaceAndPath(MOD_ID, "basalt_crusher");
    public static final Identifier GrizzlyBlockId = Identifier.fromNamespaceAndPath(MOD_ID, "grizzly");
    public static final Identifier GravelMillBlockId = Identifier.fromNamespaceAndPath(MOD_ID, "gravel_mill");
    public static final Identifier IronJawLinerId = Identifier.fromNamespaceAndPath(MOD_ID, "iron_jaw_liner");
    public static final Identifier DiamondJawLinerId = Identifier.fromNamespaceAndPath(MOD_ID, "diamond_jaw_liner");
    public static final Identifier NetheriteJawLinerId = Identifier.fromNamespaceAndPath(MOD_ID, "netherite_jaw_liner");
    public static final Identifier MillRodChargeId = Identifier.fromNamespaceAndPath(MOD_ID, "mill_rod_charge");

    public static final TagKey<Item> BASALTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "basalts"));
    public static final TagKey<Item> JAW_LINERS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "jaw_liners"));

    public static final Identifier BASALT_CRUSHER_SOUND_ID = Identifier.fromNamespaceAndPath(MOD_ID, "basalt_crusher_sound");
    public static final Identifier GRAVEL_MILL_SOUND_ID = Identifier.fromNamespaceAndPath(MOD_ID,"gravel_mill_sound");
    public static final SoundEvent BASALT_CRUSHER_SOUND_EVENT = SoundEvent.createVariableRangeEvent(BASALT_CRUSHER_SOUND_ID);
    public static final SoundEvent GRAVEL_MILL_SOUND_EVENT = SoundEvent.createVariableRangeEvent(GRAVEL_MILL_SOUND_ID);

    // Basalt Crusher block
    public static final Block BASALT_CRUSHER_BLOCK = Registry.register(BuiltInRegistries.BLOCK, BasaltCrusherBlockId, new BasaltCrusherBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, BasaltCrusherBlockId)).mapColor(MapColor.METAL).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.f, 4.8f).sound(SoundType.METAL)));
    public static final BlockItem BASALT_CRUSHER_ITEM = Registry.register(BuiltInRegistries.ITEM, BasaltCrusherBlockId, new BlockItem(BASALT_CRUSHER_BLOCK, new net.minecraft.world.item.Item.Properties().setId(ResourceKey.create(Registries.ITEM, BasaltCrusherBlockId)).useBlockDescriptionPrefix()));
    public static final BlockEntityType<BasaltCrusherEntity> BASALT_CRUSHER_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, BasaltCrusherBlockId, FabricBlockEntityTypeBuilder.create(BasaltCrusherEntity::new, BASALT_CRUSHER_BLOCK).build());
    public static final MenuType<BasaltCrusherScreenHandler> BASALT_CRUSHER_SCREEN_HANDLER = Registry.register(BuiltInRegistries.MENU, BasaltCrusherBlockId, new MenuType<>(BasaltCrusherScreenHandler::new, FeatureFlagSet.of()));

    // Grizzly block
    public static final Block GRIZZLY_BLOCK = Registry.register(BuiltInRegistries.BLOCK, GrizzlyBlockId, new GrizzlyBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, GrizzlyBlockId)).mapColor(MapColor.METAL).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0f, 4.8f).sound(SoundType.METAL).noOcclusion()));
    public static final BlockItem GRIZZLY_ITEM = Registry.register(BuiltInRegistries.ITEM, GrizzlyBlockId, new BlockItem(GRIZZLY_BLOCK, new net.minecraft.world.item.Item.Properties().setId(ResourceKey.create(Registries.ITEM, GrizzlyBlockId)).useBlockDescriptionPrefix()));
    public static final BlockEntityType<GrizzlyEntity> GRIZZLY_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, GrizzlyBlockId, FabricBlockEntityTypeBuilder.create(GrizzlyEntity::new, GRIZZLY_BLOCK).build());
    public static final MenuType<GrizzlyScreenHandler> GRIZZLY_SCREEN_HANDLER = Registry.register(BuiltInRegistries.MENU, GrizzlyBlockId, new MenuType<>(GrizzlyScreenHandler::new, FeatureFlagSet.of()));

    // Gravel Mill block
    public static final Block GRAVEL_MILL_BLOCK = Registry.register(BuiltInRegistries.BLOCK, GravelMillBlockId, new GravelMillBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, GravelMillBlockId)).mapColor(MapColor.METAL).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0f, 4.8f).sound(SoundType.METAL)));
    public static final BlockItem GRAVEL_MILL_ITEM = Registry.register(BuiltInRegistries.ITEM, GravelMillBlockId, new BlockItem(GRAVEL_MILL_BLOCK, new net.minecraft.world.item.Item.Properties().setId(ResourceKey.create(Registries.ITEM, GravelMillBlockId)).useBlockDescriptionPrefix()));
    public static final BlockEntityType<GravelMillEntity> GRAVEL_MILL_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, GravelMillBlockId, FabricBlockEntityTypeBuilder.create(GravelMillEntity::new, GRAVEL_MILL_BLOCK).build());
    public static final MenuType<GravelMillScreenHandler> GRAVEL_MILL_SCREEN_HANDLER = Registry.register(BuiltInRegistries.MENU, GravelMillBlockId, new MenuType<>(GravelMillScreenHandler::new, FeatureFlagSet.of()));

    // Basalt Crusher Jaw Liners
    public static final Item IRON_JAW_LINER_ITEM      = Registry.register(BuiltInRegistries.ITEM, IronJawLinerId,      new Item(ToolMaterial.IRON     .applyToolProperties(new net.minecraft.world.item.Item.Properties().setId(ResourceKey.create(Registries.ITEM, IronJawLinerId)),      BlockTags.AIR, 0.0f, -3.0f, 0.0f)));
    public static final Item DIAMOND_JAW_LINER_ITEM   = Registry.register(BuiltInRegistries.ITEM, DiamondJawLinerId,   new Item(ToolMaterial.DIAMOND  .applyToolProperties(new net.minecraft.world.item.Item.Properties().setId(ResourceKey.create(Registries.ITEM, DiamondJawLinerId)),   BlockTags.AIR, 0.0f, -3.0f, 0.0f)));
    public static final Item NETHERITE_JAW_LINER_ITEM = Registry.register(BuiltInRegistries.ITEM, NetheriteJawLinerId, new Item(ToolMaterial.NETHERITE.applyToolProperties(new net.minecraft.world.item.Item.Properties().setId(ResourceKey.create(Registries.ITEM, NetheriteJawLinerId)), BlockTags.AIR, 0.0f, -3.0f, 0.0f)));

    // Gravel Mill Rod Charge
    public static final Item MILL_ROD_CHARGE_ITEM = Registry.register(BuiltInRegistries.ITEM, MillRodChargeId, new Item(ToolMaterial.IRON.applyToolProperties(new net.minecraft.world.item.Item.Properties().setId(ResourceKey.create(Registries.ITEM, MillRodChargeId)), BlockTags.AIR, 0.0f, -3.0f, 0.0f)));

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

        if (FabricLoader.getInstance().isModLoaded("terrestria")) {
            LOGGER.debug("Enabling Terrestria integration...");
            extendTerrestria = true;

            VOLCANIC_GRAVEL_BLOCK = Registry.register(BuiltInRegistries.BLOCK, VolcanicGravelBlockId, new ColoredFallingBlock(new ColorRGBA(0x202020), BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL).setId(ResourceKey.create(Registries.BLOCK, VolcanicGravelBlockId)).mapColor(MapColor.DEEPSLATE)));
            VOLCANIC_GRAVEL_ITEM = Registry.register(BuiltInRegistries.ITEM, VolcanicGravelBlockId, new BlockItem(VOLCANIC_GRAVEL_BLOCK, new net.minecraft.world.item.Item.Properties().setId(ResourceKey.create(Registries.ITEM, VolcanicGravelBlockId)).useBlockDescriptionPrefix()));

            OBSIDIAN_PILE_ITEM = Registry.register(BuiltInRegistries.ITEM, ObsidianPileId, new Item(new net.minecraft.world.item.Item.Properties().setId(ResourceKey.create(Registries.ITEM, ObsidianPileId))));
            OBSIDIAN_SHARD_ITEM = Registry.register(BuiltInRegistries.ITEM, ObsidianShardId, new Item(new net.minecraft.world.item.Item.Properties().setId(ResourceKey.create(Registries.ITEM, ObsidianShardId))));

            // Register aliases for Volcanic Gravel (renamed from Black Gravel)
            BuiltInRegistries.BLOCK.addAlias(Identifier.fromNamespaceAndPath(MOD_ID, "black_gravel"), Identifier.fromNamespaceAndPath(MOD_ID, "volcanic_gravel"));
            BuiltInRegistries.ITEM.addAlias(Identifier.fromNamespaceAndPath(MOD_ID, "black_gravel"), Identifier.fromNamespaceAndPath(MOD_ID, "volcanic_gravel"));

            // Register Terrestria extension items for Item Groups
            ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS)
                    .register(content -> content.addAfter(Items.GRAVEL, VOLCANIC_GRAVEL_ITEM));
            ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                    .register(content -> content.addAfter(Items.FLINT, OBSIDIAN_SHARD_ITEM, OBSIDIAN_PILE_ITEM));
        }

        // Register standard items for Item Groups
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(content -> content.addAfter(Items.BLAST_FURNACE, BASALT_CRUSHER_ITEM, GRAVEL_MILL_ITEM, GRIZZLY_ITEM));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(content -> content.addBefore(Items.WHITE_DYE, IRON_JAW_LINER_ITEM, DIAMOND_JAW_LINER_ITEM, NETHERITE_JAW_LINER_ITEM, MILL_ROD_CHARGE_ITEM));

        // Basalt Crusher Storage
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> direction != null && blockEntity instanceof BasaltCrusherEntity ? ((BasaltCrusherEntity) blockEntity).getSidedStorage(direction) : null, BASALT_CRUSHER_BLOCK);
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> direction != null && blockEntity instanceof GrizzlyEntity ? ((GrizzlyEntity) blockEntity).getSidedStorage(direction) : null, GRIZZLY_BLOCK);
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> direction != null && blockEntity instanceof GravelMillEntity ? ((GravelMillEntity) blockEntity).getSidedStorage(direction) : null, GRAVEL_MILL_BLOCK);

        Registry.register(BuiltInRegistries.SOUND_EVENT, BasaltCrusher.BASALT_CRUSHER_SOUND_ID, BASALT_CRUSHER_SOUND_EVENT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, BasaltCrusher.GRAVEL_MILL_SOUND_ID,    GRAVEL_MILL_SOUND_EVENT);

        // Take care of blocks and items needing s/basalt-crusher/basalt_crusher/
        BasaltCrusherRegistryAliases.init();
    }
}