package net.gnomecraft.basaltcrusher.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class IOTypeMatchers {
    public static boolean matchStoneGravel(ItemStack stone, ItemStack gravel) {
        return (!TerrestriaIntegration.ENABLED || gravel.isEmpty() || gravel.isOf(stone.getItem()) ||
                (gravel.isOf(Items.GRAVEL) && !stone.isIn(TerrestriaIntegration.TERRESTRIA_BASALTS)) ||
                (gravel.isOf(TerrestriaIntegration.BLACK_GRAVEL_ITEM) && stone.isIn(TerrestriaIntegration.TERRESTRIA_BASALTS))
        );
    }

    public static boolean matchGravelSand(ItemStack gravel, ItemStack sand) {
        return (!TerrestriaIntegration.ENABLED || sand.isEmpty() || sand.isOf(gravel.getItem()) ||
                (sand.isOf(Items.SAND) && gravel.isOf(Items.GRAVEL)) ||
                (sand.isOf(TerrestriaIntegration.BLACK_SAND_ITEM) && gravel.isOf(TerrestriaIntegration.BLACK_GRAVEL_ITEM))
        );
    }
}
