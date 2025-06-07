package net.gnomecraft.basaltcrusher.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.gnomecraft.basaltcrusher.utils.InterfaceStockpile;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DrawContext.class)
public abstract class MixinDrawContext implements InterfaceStockpile {
    @Shadow
    @Final
    private Matrix3x2fStack matrices;

    @Shadow
    public abstract void drawItem(ItemStack item, int x, int y);

    @Shadow
    public abstract void drawText(TextRenderer textRenderer, @Nullable String text, int x, int y, int color, boolean shadow);

    @Shadow
    public abstract void fill(RenderPipeline pipeline, int x1, int y1, int x2, int y2, int color);

    // This is based on the new merged item renderer in 23w16a's "new" DrawContext class.
    // Drawing the ItemStack and drawing the damage are no longer two separate methods, so...
    public void basaltCrusher$drawStockpile(TextRenderer textRenderer, ItemStack stack, int x, int y, float quantity) {
        if (stack.isEmpty()) {
            return;
        }

        // render item
        this.drawItem(stack, x, y);

        // layer metadata on top
        this.matrices.pushMatrix();

        // render stockpile fill level bar
        int k = Math.round(13.0f * (quantity % 1.0f));
        this.fill(RenderPipelines.GUI, x + 2, y + 13, x + 15, y + 15, 0xFF000000);
        this.fill(RenderPipelines.GUI, x + 2, y + 13, x + 2 + k, y + 14, 0xFF877BAE);

        // render item stack count
        if (quantity > 1.0f) {
            String count = String.valueOf((int) quantity);
            this.drawText(textRenderer, count, x + 17 - textRenderer.getWidth(count), y + 9, 0xFFFFFFFF, true);
        }

        this.matrices.popMatrix();
    }
}
