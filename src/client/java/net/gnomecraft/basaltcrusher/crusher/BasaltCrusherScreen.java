package net.gnomecraft.basaltcrusher.crusher;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BasaltCrusherScreen extends AbstractContainerScreen<AbstractContainerMenu> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(BasaltCrusher.MOD_ID, "textures/gui/container/basalt_crusher_screen.png");
    BasaltCrusherScreenHandler screenHandler;

    public BasaltCrusherScreen(AbstractContainerMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);

        this.screenHandler = (BasaltCrusherScreenHandler) handler;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics context, float delta, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        int progress24 = ((BasaltCrusherScreenHandler)this.menu).crushProgress24();
        context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 106, y + 34, 176, 0, progress24 + 1, 16, 256, 256);

        // Keep the title bar background clear (it's longer than I allowed for in some languages).
        if (font.width(title) > 80) {
            context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 88, y + 4, 4, 4, 16, 12, 256, 256);
        }
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

        // Left-justified so (the English version at least) just misses the piston shadows.
        titleLabelX = 6;
    }
}