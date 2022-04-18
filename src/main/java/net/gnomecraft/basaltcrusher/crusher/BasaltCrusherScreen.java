package net.gnomecraft.basaltcrusher.crusher;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BasaltCrusherScreen extends HandledScreen<ScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("basalt-crusher", "textures/gui/container/basalt_crusher_screen.png");
    BasaltCrusherScreenHandler screenHandler;

    public BasaltCrusherScreen(ScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        screenHandler = (BasaltCrusherScreenHandler) handler;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);

        int progress24 = ((BasaltCrusherScreenHandler)this.handler).crushProgress24();
        this.drawTexture(matrices, x + 106, y + 34, 176, 0, progress24 + 1, 16);
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

        // Left-justified so (the English version at least) just misses the piston shadows.
        titleX = 6;
    }
}