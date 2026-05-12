package net.gnomecraft.basaltcrusher.grizzly;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.gnomecraft.basaltcrusher.utils.TerrestriaIntegration;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class GrizzlyScreen extends AbstractContainerScreen<AbstractContainerMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(BasaltCrusher.MOD_ID, "textures/gui/container/grizzly_screen.png");
    GrizzlyScreenHandler screenHandler;

    public GrizzlyScreen(AbstractContainerMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);

        screenHandler = (GrizzlyScreenHandler) handler;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        /*
         * Display stockpile levels using a custom damage bar.
         */

        // If we are processing Terrestria volcanic materials at the moment, show those stockpiles instead.
        boolean black = TerrestriaIntegration.ENABLED && ((GrizzlyScreenHandler) this.menu).stockpileOf(Items.AIR) > 0.0f;

        // gravel: 62, 41 -  77, 56
        Item gravelType = black ? TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM : Items.GRAVEL;
        float gravel = ((GrizzlyScreenHandler) this.menu).stockpileOf(gravelType);
        context.basaltCrusher$drawStockpile(this.font, gravelType.getDefaultInstance(), x + 62, y + 41, gravel);

        // sand:   78, 57 -  93, 72
        Item sandType = black ? TerrestriaIntegration.VOLCANIC_SAND_ITEM : Items.SAND;
        float sand = ((GrizzlyScreenHandler) this.menu).stockpileOf(sandType);
        context.basaltCrusher$drawStockpile(this.font, sandType.getDefaultInstance(), x + 78, y + 57, sand);

        // dirt:   98, 57 - 113, 72
        float dirt = ((GrizzlyScreenHandler) this.menu).stockpileOf(Items.DIRT);
        Item dirtType = Items.DIRT;
        context.basaltCrusher$drawStockpile(this.font, dirtType.getDefaultInstance(), x + 98, y + 57, dirt);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.renderTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();

        // Left-justified to match the style of the Basalt Crusher.
        titleLabelX = 6;
        inventoryLabelX = 6;
    }
}