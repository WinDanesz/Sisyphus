package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.integration.futuremc.SBFutureMCIntegration;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Translates Future MC items in every stack read from a tag, before the item is looked up by name.
 */
@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Inject(method = "<init>(Lnet/minecraft/nbt/NBTTagCompound;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;hasKey(Ljava/lang/String;)Z", ordinal = 0))
    private void sisyphus$translateFutureMCItem(NBTTagCompound compound, CallbackInfo ci) {
        SBFutureMCIntegration.fixItem(compound);
    }
}
