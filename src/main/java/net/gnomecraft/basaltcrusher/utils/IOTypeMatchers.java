package net.gnomecraft.basaltcrusher.utils;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class IOTypeMatchers {
    public static boolean matchStoneGravel(ItemStack stone, ItemStack gravel) {
        return (!TerrestriaIntegration.ENABLED || gravel.isEmpty() || gravel.is(stone.getItem()) ||
                (gravel.is(Items.GRAVEL) && !stone.is(TerrestriaIntegration.TERRESTRIA_BASALTS)) ||
                (gravel.is(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM) && stone.is(TerrestriaIntegration.TERRESTRIA_BASALTS))
        );
    }

    public static boolean matchGravelSand(ItemStack gravel, ItemStack sand) {
        return (!TerrestriaIntegration.ENABLED || sand.isEmpty() || sand.is(gravel.getItem()) ||
                (sand.is(Items.SAND) && gravel.is(Items.GRAVEL)) ||
                (sand.is(TerrestriaIntegration.VOLCANIC_SAND_ITEM) && gravel.is(TerrestriaIntegration.VOLCANIC_GRAVEL_ITEM))
        );
    }
}
