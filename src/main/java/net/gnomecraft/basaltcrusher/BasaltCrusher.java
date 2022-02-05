package net.gnomecraft.basaltcrusher;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasaltCrusher implements ModInitializer {
    public static Block BASALT_CRUSHER_BLOCK;
    public static BlockItem BASALT_CRUSHER_ITEM;
    public static BlockEntityType<BasaltCrusherEntity> BASALT_CRUSHER_ENTITY;

    public static ToolItem IRON_JAW_LINER_ITEM;
    public static ToolItem DIAMOND_JAW_LINER_ITEM;
    public static ToolItem NETHERITE_JAW_LINER_ITEM;

    public static final String modId = "basalt-crusher";
    public static final Logger LOGGER = LoggerFactory.getLogger(modId);

    public static final ScreenHandlerType<BasaltCrusherScreenHandler> BASALT_CRUSHER_SCREEN_HANDLER;

    public static final Identifier BasaltCrusherBlockId = new Identifier(modId, "basalt_crusher");
    public static final Identifier IronJawLinerId = new Identifier(modId, "iron_jaw_liner");
    public static final Identifier DiamondJawLinerId = new Identifier(modId, "diamond_jaw_liner");
    public static final Identifier NetheriteJawLinerId = new Identifier(modId, "netherite_jaw_liner");

    public static final Tag<Item> BASALTS = TagFactory.ITEM.create(new Identifier("c", "basalt"));
    public static final Tag<Item> JAW_LINERS = TagFactory.ITEM.create(new Identifier("basalt-crusher", "jaw_liners"));

    @Override
    public void onInitialize() {
        LOGGER.info("Basalt Crusher block is hungry...");

        // Basalt Crusher block
        BASALT_CRUSHER_BLOCK = Registry.register(Registry.BLOCK, BasaltCrusherBlockId, new BasaltCrusherBlock(FabricBlockSettings.of(Material.METAL).hardness(4.0f)));
        BASALT_CRUSHER_ITEM = Registry.register(Registry.ITEM, BasaltCrusherBlockId, new BlockItem(BASALT_CRUSHER_BLOCK, new Item.Settings().group(ItemGroup.DECORATIONS)));
        BASALT_CRUSHER_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, BasaltCrusherBlockId, FabricBlockEntityTypeBuilder.create(BasaltCrusherEntity::new, BASALT_CRUSHER_BLOCK).build(null));

        // Basalt Crusher Jaw Liners
        IRON_JAW_LINER_ITEM      = Registry.register(Registry.ITEM, IronJawLinerId,      new ToolItem(ToolMaterials.IRON,      new Item.Settings().group(ItemGroup.MISC)));
        DIAMOND_JAW_LINER_ITEM   = Registry.register(Registry.ITEM, DiamondJawLinerId,   new ToolItem(ToolMaterials.DIAMOND,   new Item.Settings().group(ItemGroup.MISC)));
        NETHERITE_JAW_LINER_ITEM = Registry.register(Registry.ITEM, NetheriteJawLinerId, new ToolItem(ToolMaterials.NETHERITE, new Item.Settings().group(ItemGroup.MISC)));

        // Basalt Crusher gravel recipe
        Registry.register(Registry.RECIPE_SERIALIZER, BasaltCrusherRecipeSerializer.ID, BasaltCrusherRecipeSerializer.INSTANCE);
        Registry.register(Registry.RECIPE_TYPE, new Identifier(modId, BasaltCrusherRecipe.Type.ID), BasaltCrusherRecipe.Type.INSTANCE);
    }

    static {
        BASALT_CRUSHER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(BasaltCrusherBlockId, BasaltCrusherScreenHandler::new);
    }
}