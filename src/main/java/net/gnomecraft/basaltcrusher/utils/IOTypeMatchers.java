package net.gnomecraft.basaltcrusher.utils;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class IOTypeMatchers {
    public static boolean matchStoneGravel(ItemStack stone, ItemStack gravel) {
        return (!BasaltCrusher.extendTerrestria || gravel.isEmpty() || gravel.is(stone.getItem()) ||
                (gravel.is(Items.GRAVEL) && !stone.is(BasaltCrusher.TERRESTRIA_BASALTS)) ||
                (gravel.is(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM) && stone.is(BasaltCrusher.TERRESTRIA_BASALTS))
        );
    }

    public static boolean matchGravelSand(ItemStack gravel, ItemStack sand) {
        return (!BasaltCrusher.extendTerrestria || sand.isEmpty() || sand.is(gravel.getItem()) ||
                (sand.is(Items.SAND) && gravel.is(Items.GRAVEL)) ||
                (sand.is(TerrestriaIntegration.VOLCANIC_SAND_ITEM) && gravel.is(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM))
        );
    }
}
