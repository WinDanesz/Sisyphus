package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.util.PottedPlants;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Flower pots hold the small plants of other mods and of this mod.
 */
@Mixin(BlockFlowerPot.class)
public abstract class MixinBlockFlowerPot {

    @Inject(method = "canBePotted", at = @At("RETURN"), cancellable = true)
    private void sisyphus$potAnySmallPlant(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && PottedPlants.canPot(stack)) {
            cir.setReturnValue(true);
        }
    }
}
