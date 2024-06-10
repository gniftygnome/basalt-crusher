package net.gnomecraft.basaltcrusher;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.gnomecraft.basaltcrusher.crusher.*;
import net.gnomecraft.basaltcrusher.grizzly.*;
import net.gnomecraft.basaltcrusher.mill.*;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ColorCode;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasaltCrusher implements ModInitializer {
    public static final String MOD_ID = "basalt-crusher";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier BlackGravelBlockId = Identifier.of(MOD_ID, "black_gravel");
    public static final Identifier ObsidianPileId = Identifier.of(MOD_ID, "obsidian_pile");
    public static final Identifier ObsidianShardId = Identifier.of(MOD_ID, "obsidian_shard");

    public static final Identifier BasaltCrusherBlockId = Identifier.of(MOD_ID, "basalt_crusher");
    public static final Identifier GrizzlyBlockId = Identifier.of(MOD_ID, "grizzly");
    public static final Identifier GravelMillBlockId = Identifier.of(MOD_ID, "gravel_mill");
    public static final Identifier IronJawLinerId = Identifier.of(MOD_ID, "iron_jaw_liner");
    public static final Identifier DiamondJawLinerId = Identifier.of(MOD_ID, "diamond_jaw_liner");
    public static final Identifier NetheriteJawLinerId = Identifier.of(MOD_ID, "netherite_jaw_liner");
    public static final Identifier MillRodChargeId = Identifier.of(MOD_ID, "mill_rod_charge");

    public static final TagKey<Item> BASALTS = TagKey.of(RegistryKeys.ITEM, Identifier.of("c", "basalts"));
    public static final TagKey<Item> JAW_LINERS = TagKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "jaw_liners"));

    public static final Identifier BASALT_CRUSHER_SOUND_ID = Identifier.of(MOD_ID, "basalt_crusher_sound");
    public static final Identifier GRAVEL_MILL_SOUND_ID = Identifier.of(MOD_ID,"gravel_mill_sound");
    public static final SoundEvent BASALT_CRUSHER_SOUND_EVENT = SoundEvent.of(BASALT_CRUSHER_SOUND_ID);
    public static final SoundEvent GRAVEL_MILL_SOUND_EVENT = SoundEvent.of(GRAVEL_MILL_SOUND_ID);

    public static boolean extendTerrestria = false;

    public static Block BLACK_GRAVEL_BLOCK;
    public static BlockItem BLACK_GRAVEL_ITEM;
    public static Item OBSIDIAN_PILE_ITEM;
    public static Item OBSIDIAN_SHARD_ITEM;

    public static Block BASALT_CRUSHER_BLOCK;
    public static BlockItem BASALT_CRUSHER_ITEM;
    public static BlockEntityType<BasaltCrusherEntity> BASALT_CRUSHER_ENTITY;

    public static Block GRIZZLY_BLOCK;
    public static BlockItem GRIZZLY_ITEM;
    public static BlockEntityType<GrizzlyEntity> GRIZZLY_ENTITY;

    public static Block GRAVEL_MILL_BLOCK;
    public static BlockItem GRAVEL_MILL_ITEM;
    public static BlockEntityType<GravelMillEntity> GRAVEL_MILL_ENTITY;

    public static ToolItem IRON_JAW_LINER_ITEM;
    public static ToolItem DIAMOND_JAW_LINER_ITEM;
    public static ToolItem NETHERITE_JAW_LINER_ITEM;

    public static ToolItem MILL_ROD_CHARGE_ITEM;

    public static ScreenHandlerType<BasaltCrusherScreenHandler> BASALT_CRUSHER_SCREEN_HANDLER;
    public static ScreenHandlerType<GrizzlyScreenHandler> GRIZZLY_SCREEN_HANDLER;
    public static ScreenHandlerType<GravelMillScreenHandler> GRAVEL_MILL_SCREEN_HANDLER;

    @Override
    public void onInitialize() {
        LOGGER.info("Basalt Crusher block is hungry...");

        if (FabricLoader.getInstance().isModLoaded("terrestria")) {
            LOGGER.debug("Enabling Terrestria integration...");
            extendTerrestria = true;

            BLACK_GRAVEL_BLOCK = Registry.register(Registries.BLOCK, BlackGravelBlockId, new ColoredFallingBlock(new ColorCode(0x202020), AbstractBlock.Settings.copy(Blocks.GRAVEL).mapColor(MapColor.DEEPSLATE_GRAY)));
            BLACK_GRAVEL_ITEM = Registry.register(Registries.ITEM, BlackGravelBlockId, new BlockItem(BLACK_GRAVEL_BLOCK, new Item.Settings()));

            OBSIDIAN_PILE_ITEM = Registry.register(Registries.ITEM, ObsidianPileId, new Item(new Item.Settings()));
            OBSIDIAN_SHARD_ITEM = Registry.register(Registries.ITEM, ObsidianShardId, new Item(new Item.Settings()));

            // Register Terrestria extension items for Item Groups
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
                    .register(content -> content.addAfter(Items.GRAVEL, BLACK_GRAVEL_ITEM));
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                    .register(content -> content.addAfter(Items.FLINT, OBSIDIAN_SHARD_ITEM, OBSIDIAN_PILE_ITEM));
        }

        // Basalt Crusher block
        BASALT_CRUSHER_BLOCK = Registry.register(Registries.BLOCK, BasaltCrusherBlockId, new BasaltCrusherBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(3.f, 4.8f).sounds(BlockSoundGroup.METAL)));
        BASALT_CRUSHER_ITEM = Registry.register(Registries.ITEM, BasaltCrusherBlockId, new BlockItem(BASALT_CRUSHER_BLOCK, new Item.Settings()));
        BASALT_CRUSHER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, BasaltCrusherBlockId, BlockEntityType.Builder.create(BasaltCrusherEntity::new, BASALT_CRUSHER_BLOCK).build(null));
        BASALT_CRUSHER_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, BasaltCrusherBlockId, new ScreenHandlerType<>(BasaltCrusherScreenHandler::new, FeatureSet.empty()));

        // Grizzly block
        GRIZZLY_BLOCK = Registry.register(Registries.BLOCK, GrizzlyBlockId, new GrizzlyBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(3.0f, 4.8f).sounds(BlockSoundGroup.METAL).nonOpaque()));
        GRIZZLY_ITEM = Registry.register(Registries.ITEM, GrizzlyBlockId, new BlockItem(GRIZZLY_BLOCK, new Item.Settings()));
        GRIZZLY_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, GrizzlyBlockId, BlockEntityType.Builder.create(GrizzlyEntity::new, GRIZZLY_BLOCK).build(null));
        GRIZZLY_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, GrizzlyBlockId, new ScreenHandlerType<>(GrizzlyScreenHandler::new, FeatureSet.empty()));

        // Gravel Mill block
        GRAVEL_MILL_BLOCK = Registry.register(Registries.BLOCK, GravelMillBlockId, new GravelMillBlock(AbstractBlock.Settings.create().mapColor(MapColor.IRON_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresTool().strength(3.0f, 4.8f).sounds(BlockSoundGroup.METAL)));
        GRAVEL_MILL_ITEM = Registry.register(Registries.ITEM, GravelMillBlockId, new BlockItem(GRAVEL_MILL_BLOCK, new Item.Settings()));
        GRAVEL_MILL_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, GravelMillBlockId, BlockEntityType.Builder.create(GravelMillEntity::new, GRAVEL_MILL_BLOCK).build(null));
        GRAVEL_MILL_SCREEN_HANDLER = Registry.register(Registries.SCREEN_HANDLER, GravelMillBlockId, new ScreenHandlerType<>(GravelMillScreenHandler::new, FeatureSet.empty()));

        // Basalt Crusher Jaw Liners
        IRON_JAW_LINER_ITEM      = Registry.register(Registries.ITEM, IronJawLinerId,      new ToolItem(ToolMaterials.IRON,      new Item.Settings()));
        DIAMOND_JAW_LINER_ITEM   = Registry.register(Registries.ITEM, DiamondJawLinerId,   new ToolItem(ToolMaterials.DIAMOND,   new Item.Settings()));
        NETHERITE_JAW_LINER_ITEM = Registry.register(Registries.ITEM, NetheriteJawLinerId, new ToolItem(ToolMaterials.NETHERITE, new Item.Settings()));

        // Gravel Mill Rod Charge
        MILL_ROD_CHARGE_ITEM = Registry.register(Registries.ITEM, MillRodChargeId, new ToolItem(ToolMaterials.IRON, new Item.Settings()));

        // Register standard items for Item Groups
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register(content -> content.addAfter(Items.BLAST_FURNACE, BASALT_CRUSHER_ITEM, GRAVEL_MILL_ITEM, GRIZZLY_ITEM));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register(content -> content.addBefore(Items.WHITE_DYE, IRON_JAW_LINER_ITEM, DIAMOND_JAW_LINER_ITEM, NETHERITE_JAW_LINER_ITEM, MILL_ROD_CHARGE_ITEM));

        // Basalt Crusher Storage
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> blockEntity instanceof BasaltCrusherEntity ? ((BasaltCrusherEntity) blockEntity).getSidedStorage(direction) : null, BASALT_CRUSHER_BLOCK);
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> blockEntity instanceof GrizzlyEntity ? ((GrizzlyEntity) blockEntity).getSidedStorage(direction) : null, GRIZZLY_BLOCK);
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> blockEntity instanceof GravelMillEntity ? ((GravelMillEntity) blockEntity).getSidedStorage(direction) : null, GRAVEL_MILL_BLOCK);

        Registry.register(Registries.SOUND_EVENT, BasaltCrusher.BASALT_CRUSHER_SOUND_ID, BASALT_CRUSHER_SOUND_EVENT);
        Registry.register(Registries.SOUND_EVENT, BasaltCrusher.GRAVEL_MILL_SOUND_ID,    GRAVEL_MILL_SOUND_EVENT);
    }
}