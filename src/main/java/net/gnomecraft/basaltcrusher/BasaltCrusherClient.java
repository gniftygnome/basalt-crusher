package net.gnomecraft.basaltcrusher;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.gnomecraft.basaltcrusher.crusher.BasaltCrusherScreen;
import net.gnomecraft.basaltcrusher.grizzly.GrizzlyScreen;
import net.gnomecraft.basaltcrusher.mill.GravelMillScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class BasaltCrusherClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(BasaltCrusher.GRIZZLY_BLOCK, RenderLayer.getCutout());

        HandledScreens.register(BasaltCrusher.BASALT_CRUSHER_SCREEN_HANDLER, BasaltCrusherScreen::new);
        HandledScreens.register(BasaltCrusher.GRIZZLY_SCREEN_HANDLER, GrizzlyScreen::new);
        HandledScreens.register(BasaltCrusher.GRAVEL_MILL_SCREEN_HANDLER, GravelMillScreen::new);
    }
}