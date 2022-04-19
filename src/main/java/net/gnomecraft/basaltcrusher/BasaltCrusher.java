package net.gnomecraft.basaltcrusher;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.gnomecraft.basaltcrusher.crusher.*;
import net.gnomecraft.basaltcrusher.grizzly.*;
import net.gnomecraft.basaltcrusher.mill.*;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("UnstableApiUsage")
public class BasaltCrusher implements ModInitializer {
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

    public static final String modId = "basalt-crusher";
    public static final Logger LOGGER = LoggerFactory.getLogger(modId);

    public static final ScreenHandlerType<BasaltCrusherScreenHandler> BASALT_CRUSHER_SCREEN_HANDLER;
    public static final ScreenHandlerType<GrizzlyScreenHandler> GRIZZLY_SCREEN_HANDLER;
    public static final ScreenHandlerType<GravelMillScreenHandler> GRAVEL_MILL_SCREEN_HANDLER;

    public static final Identifier BasaltCrusherBlockId = new Identifier(modId, "basalt_crusher");
    public static final Identifier GrizzlyBlockId = new Identifier(modId, "grizzly");
    public static final Identifier GravelMillBlockId = new Identifier(modId, "gravel_mill");
    public static final Identifier IronJawLinerId = new Identifier(modId, "iron_jaw_liner");
    public static final Identifier DiamondJawLinerId = new Identifier(modId, "diamond_jaw_liner");
    public static final Identifier NetheriteJawLinerId = new Identifier(modId, "netherite_jaw_liner");
    public static final Identifier MillRodChargeId = new Identifier(modId, "mill_rod_charge");

    public static final TagKey<Item> BASALTS = TagKey.of(Registry.ITEM_KEY, new Identifier("c", "basalt"));
    public static final TagKey<Item> JAW_LINERS = TagKey.of(Registry.ITEM_KEY, new Identifier("basalt-crusher", "jaw_liners"));

    public static final Identifier BASALT_CRUSHER_SOUND_ID = new Identifier("basalt-crusher:basalt_crusher_sound");
    public static final Identifier GRAVEL_MILL_SOUND_ID = new Identifier("basalt-crusher:gravel_mill_sound");
    public static SoundEvent BASALT_CRUSHER_SOUND_EVENT = new SoundEvent(BASALT_CRUSHER_SOUND_ID);
    public static SoundEvent GRAVEL_MILL_SOUND_EVENT = new SoundEvent(GRAVEL_MILL_SOUND_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Basalt Crusher block is hungry...");

        // Basalt Crusher block
        BASALT_CRUSHER_BLOCK = Registry.register(Registry.BLOCK, BasaltCrusherBlockId, new BasaltCrusherBlock(FabricBlockSettings.of(Material.METAL).hardness(4.0f)));
        BASALT_CRUSHER_ITEM = Registry.register(Registry.ITEM, BasaltCrusherBlockId, new BlockItem(BASALT_CRUSHER_BLOCK, new Item.Settings().group(ItemGroup.DECORATIONS)));
        BASALT_CRUSHER_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, BasaltCrusherBlockId, FabricBlockEntityTypeBuilder.create(BasaltCrusherEntity::new, BASALT_CRUSHER_BLOCK).build(null));

        // Grizzly block
        GRIZZLY_BLOCK = Registry.register(Registry.BLOCK, GrizzlyBlockId, new GrizzlyBlock(FabricBlockSettings.of(Material.METAL).hardness(4.0f).nonOpaque()));
        GRIZZLY_ITEM = Registry.register(Registry.ITEM, GrizzlyBlockId, new BlockItem(GRIZZLY_BLOCK, new Item.Settings().group(ItemGroup.DECORATIONS)));
        GRIZZLY_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, GrizzlyBlockId, FabricBlockEntityTypeBuilder.create(GrizzlyEntity::new, GRIZZLY_BLOCK).build(null));

        // Gravel Mill block
        GRAVEL_MILL_BLOCK = Registry.register(Registry.BLOCK, GravelMillBlockId, new GravelMillBlock(FabricBlockSettings.of(Material.METAL).hardness(4.0f)));
        GRAVEL_MILL_ITEM = Registry.register(Registry.ITEM, GravelMillBlockId, new BlockItem(GRAVEL_MILL_BLOCK, new Item.Settings().group(ItemGroup.DECORATIONS)));
        GRAVEL_MILL_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, GravelMillBlockId, FabricBlockEntityTypeBuilder.create(GravelMillEntity::new, GRAVEL_MILL_BLOCK).build(null));

        // Basalt Crusher Jaw Liners
        IRON_JAW_LINER_ITEM      = Registry.register(Registry.ITEM, IronJawLinerId,      new ToolItem(ToolMaterials.IRON,      new Item.Settings().group(ItemGroup.MISC)));
        DIAMOND_JAW_LINER_ITEM   = Registry.register(Registry.ITEM, DiamondJawLinerId,   new ToolItem(ToolMaterials.DIAMOND,   new Item.Settings().group(ItemGroup.MISC)));
        NETHERITE_JAW_LINER_ITEM = Registry.register(Registry.ITEM, NetheriteJawLinerId, new ToolItem(ToolMaterials.NETHERITE, new Item.Settings().group(ItemGroup.MISC)));

        // Gravel Mill Rod Charge
        MILL_ROD_CHARGE_ITEM = Registry.register(Registry.ITEM, MillRodChargeId, new ToolItem(ToolMaterials.IRON, new Item.Settings().group(ItemGroup.MISC)));

        // Basalt Crusher gravel recipe
        Registry.register(Registry.RECIPE_SERIALIZER, BasaltCrusherRecipeSerializer.ID, BasaltCrusherRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_TYPE, new Identifier(modId, BasaltCrusherRecipe.Type.ID), BasaltCrusherRecipe.Type.INSTANCE);

        // Basalt Crusher Storage
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> blockEntity instanceof BasaltCrusherEntity ? ((BasaltCrusherEntity) blockEntity).getSidedStorage(direction) : null, BASALT_CRUSHER_BLOCK);
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> blockEntity instanceof GrizzlyEntity ? ((GrizzlyEntity) blockEntity).getSidedStorage(direction) : null, GRIZZLY_BLOCK);
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, direction) -> blockEntity instanceof GravelMillEntity ? ((GravelMillEntity) blockEntity).getSidedStorage(direction) : null, GRAVEL_MILL_BLOCK);

        Registry.register(Registry.SOUND_EVENT, BasaltCrusher.BASALT_CRUSHER_SOUND_ID, BASALT_CRUSHER_SOUND_EVENT);
        Registry.register(Registry.SOUND_EVENT, BasaltCrusher.GRAVEL_MILL_SOUND_ID,    GRAVEL_MILL_SOUND_EVENT);
    }

    static {
        BASALT_CRUSHER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(BasaltCrusherBlockId, BasaltCrusherScreenHandler::new);
        GRIZZLY_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(GrizzlyBlockId, GrizzlyScreenHandler::new);
        GRAVEL_MILL_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(GravelMillBlockId, GravelMillScreenHandler::new);
    }
}