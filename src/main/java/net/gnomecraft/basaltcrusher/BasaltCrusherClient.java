package net.gnomecraft.basaltcrusher;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.gnomecraft.basaltcrusher.crusher.BasaltCrusherScreen;
import net.gnomecraft.basaltcrusher.grizzly.GrizzlyScreen;
import net.minecraft.client.render.RenderLayer;

@Environment(EnvType.CLIENT)
public class BasaltCrusherClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(BasaltCrusher.GRIZZLY_BLOCK, RenderLayer.getCutout());

        ScreenRegistry.register(BasaltCrusher.BASALT_CRUSHER_SCREEN_HANDLER, BasaltCrusherScreen::new);
        ScreenRegistry.register(BasaltCrusher.GRIZZLY_SCREEN_HANDLER, GrizzlyScreen::new);
    }
}