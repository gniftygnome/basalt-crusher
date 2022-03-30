package net.gnomecraft.basaltcrusher.mixin;

import net.gnomecraft.basaltcrusher.BasaltCrusher;
import net.minecraft.enchantment.EfficiencyEnchantment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EfficiencyEnchantment.class)
public abstract class EfficiencyEnchantmentMixin extends Enchantment {
    protected EfficiencyEnchantmentMixin(Rarity weight, EnchantmentTarget type, EquipmentSlot[] slotTypes) {
        super(weight, type, slotTypes);
    }

    @Inject(at = @At(value = "HEAD"), method = "isAcceptableItem(Lnet/minecraft/item/ItemStack;)Z", cancellable = true)
    public void injectJawLiners(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // Insert Jaw Liners as valid targets just like Vanilla does for Shears.
        if (stack.isIn(BasaltCrusher.JAW_LINERS)) {
            cir.setReturnValue(true);
        }
    }
}
