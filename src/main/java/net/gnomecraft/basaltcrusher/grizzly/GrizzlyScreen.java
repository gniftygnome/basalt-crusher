package net.gnomecraft.basaltcrusher.grizzly;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.gnomecraft.basaltcrusher.utils.TerrestriaIntegration;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GrizzlyScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(BasaltCrusher.MOD_ID, "textures/gui/container/grizzly_screen.png");
    GrizzlyScreenHandler screenHandler;

    public GrizzlyScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        screenHandler = (GrizzlyScreenHandler) handler;
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        /*
         * Display stockpile levels using a custom damage bar.
         */

        // If we are processing Terrestria volcanic materials at the moment, show those stockpiles instead.
        boolean black = TerrestriaIntegration.ENABLED && ((GrizzlyScreenHandler) this.handler).stockpileOf(Items.AIR) > 0.0f;

        // gravel: 62, 41 -  77, 56
        Item gravelType = black ? TerrestriaIntegration.BLACK_GRAVEL_ITEM : Items.GRAVEL;
        float gravel = ((GrizzlyScreenHandler) this.handler).stockpileOf(gravelType);
        context.basaltCrusher$drawStockpile(this.textRenderer, gravelType.getDefaultStack(), x + 62, y + 41, gravel);

        // sand:   78, 57 -  93, 72
        Item sandType = black ? TerrestriaIntegration.BLACK_SAND_ITEM : Items.SAND;
        float sand = ((GrizzlyScreenHandler) this.handler).stockpileOf(sandType);
        context.basaltCrusher$drawStockpile(this.textRenderer, sandType.getDefaultStack(), x + 78, y + 57, sand);

        // dirt:   98, 57 - 113, 72
        float dirt = ((GrizzlyScreenHandler) this.handler).stockpileOf(Items.DIRT);
        Item dirtType = Items.DIRT;
        context.basaltCrusher$drawStockpile(this.textRenderer, dirtType.getDefaultStack(), x + 98, y + 57, dirt);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();

        // Left-justified to match the style of the Basalt Crusher.
        titleX = 6;
        playerInventoryTitleX = 6;
    }
}