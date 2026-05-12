package net.gnomecraft.basaltcrusher;

import net.fabricmc.api.ClientModInitializer;
import net.gnomecraft.basaltcrusher.crusher.BasaltCrusherScreen;
import net.gnomecraft.basaltcrusher.grizzly.GrizzlyScreen;
import net.gnomecraft.basaltcrusher.mill.GravelMillScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class BasaltCrusherClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(BasaltCrusher.BASALT_CRUSHER_SCREEN_HANDLER, BasaltCrusherScreen::new);
        MenuScreens.register(BasaltCrusher.GRIZZLY_SCREEN_HANDLER, GrizzlyScreen::new);
        MenuScreens.register(BasaltCrusher.GRAVEL_MILL_SCREEN_HANDLER, GravelMillScreen::new);
    }
}