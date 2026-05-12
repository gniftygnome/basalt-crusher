package net.gnomecraft.basaltcrusher.crusher;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
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

    public BasaltCrusherScreen(AbstractContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);

        this.screenHandler = (BasaltCrusherScreenHandler) menu;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

        int progress24 = ((BasaltCrusherScreenHandler)this.menu).crushProgress24();
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 106, y + 34, 176, 0, progress24 + 1, 16, 256, 256);

        // Keep the title bar background clear (it's longer than I allowed for in some languages).
        if (font.width(title) > 80) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 88, y + 4, 4, 4, 16, 12, 256, 256);
        }
    }

    @Override
    protected void init() {
        super.init();

        // Left-justified so (the English version at least) just misses the piston shadows.
        titleLabelX = 6;
    }
}