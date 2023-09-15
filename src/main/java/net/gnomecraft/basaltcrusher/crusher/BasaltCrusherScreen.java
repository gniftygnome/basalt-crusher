package net.gnomecraft.basaltcrusher.crusher;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BasaltCrusherScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(BasaltCrusher.MOD_ID, "textures/gui/container/basalt_crusher_screen.png");
    BasaltCrusherScreenHandler screenHandler;

    public BasaltCrusherScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);

        this.screenHandler = (BasaltCrusherScreenHandler) handler;
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        int progress24 = ((BasaltCrusherScreenHandler)this.handler).crushProgress24();
        context.drawTexture(TEXTURE, x + 106, y + 34, 176, 0, progress24 + 1, 16);

        // Keep the title bar background clear (it's longer than I allowed for in some languages).
        if (textRenderer.getWidth(title) > 80) {
            context.drawTexture(TEXTURE, x + 88, y + 4, 4, 4, 16, 12);
        }
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

        // Left-justified so (the English version at least) just misses the piston shadows.
        titleX = 6;
    }
}