package net.gnomecraft.basaltcrusher;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

@Environment(EnvType.CLIENT)
public class BasaltCrusherClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(BasaltCrusher.BASALT_CRUSHER_SCREEN_HANDLER, BasaltCrusherScreen::new);
    }
}