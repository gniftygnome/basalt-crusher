package net.gnomecraft.basaltcrusher.grizzly;

import com.mojang.blaze3d.systems.RenderSystem;
import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.gnomecraft.basaltcrusher.utils.TerrestriaIntegration;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GrizzlyScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("basalt-crusher", "textures/gui/container/grizzly_screen.png");
    GrizzlyScreenHandler screenHandler;

    public GrizzlyScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        screenHandler = (GrizzlyScreenHandler) handler;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);


        /*
         * Display stockpile levels using magnificent / horrifying hack.
         *
         * The fractional amount in the stockpile is represented via the damage bar.
         *
         * We use a Jaw Liner item because they can be damaged and because they are ours.
         * By design(?), renderGuiItemOverlay() renders damage bars over the top of item counts.
         * However, we can call it twice to stack the item count on top of the damage bar...
         */
        ItemStack damage = new ItemStack(BasaltCrusher.NETHERITE_JAW_LINER_ITEM);

        // If we are processing Terrestria volcanic materials at the moment, show those stockpiles instead.
        boolean black = TerrestriaIntegration.ENABLED && ((GrizzlyScreenHandler) this.handler).stockpileOf(Items.AIR) > 0.0f;

        // gravel: 62, 41 -  77, 56
        Item gravelType = black ? TerrestriaIntegration.BLACK_GRAVEL_ITEM : Items.GRAVEL;
        float gravel = ((GrizzlyScreenHandler) this.handler).stockpileOf(gravelType);
        damage.setDamage(Math.round(BasaltCrusher.NETHERITE_JAW_LINER_ITEM.getMaxDamage() * (1.0f - gravel % 1.0f)));
        this.itemRenderer.renderGuiItemIcon(gravelType.getDefaultStack(), x + 62, y + 41);
        this.itemRenderer.renderGuiItemOverlay(this.textRenderer, damage, x + 62, y + 41);
        this.itemRenderer.renderGuiItemOverlay(this.textRenderer, new ItemStack(gravelType), x + 62, y + 41,
                gravel < 100 ? "" : Integer.toString((int) (gravel)));

        // sand:   78, 57 -  93, 72
        Item sandType = black ? TerrestriaIntegration.BLACK_SAND_ITEM : Items.SAND;
        float sand = ((GrizzlyScreenHandler) this.handler).stockpileOf(sandType);
        damage.setDamage(Math.round(BasaltCrusher.NETHERITE_JAW_LINER_ITEM.getMaxDamage() * (1.0f - sand % 1.0f)));
        this.itemRenderer.renderGuiItemIcon(sandType.getDefaultStack(), x + 78, y + 57);
        this.itemRenderer.renderGuiItemOverlay(this.textRenderer, damage, x + 78, y + 57);
        this.itemRenderer.renderGuiItemOverlay(this.textRenderer, new ItemStack(sandType), x + 78, y + 57,
                sand < 100 ? "" : Integer.toString((int) (sand)));

        // dirt:   98, 57 - 113, 72
        float dirt = ((GrizzlyScreenHandler) this.handler).stockpileOf(Items.DIRT);
        damage.setDamage(Math.round(BasaltCrusher.NETHERITE_JAW_LINER_ITEM.getMaxDamage() * (1.0f - dirt % 1.0f)));
        this.itemRenderer.renderGuiItemIcon(Items.DIRT.getDefaultStack(), x + 98, y + 57);
        this.itemRenderer.renderGuiItemOverlay(this.textRenderer, damage, x + 98, y + 57);
        this.itemRenderer.renderGuiItemOverlay(this.textRenderer, new ItemStack(Items.DIRT), x + 98, y + 57,
                dirt < 100 ? "" : Integer.toString((int) (dirt)));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();

        // Left-justified to match the style of the Basalt Crusher.
        titleX = 6;
        playerInventoryTitleX = 6;
    }
}