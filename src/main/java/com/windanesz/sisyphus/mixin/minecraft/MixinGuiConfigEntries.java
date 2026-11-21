package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.client.gui.ConfigScreenLayout;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fits the columns of Forge config screens into the screen at large GUI scales.
 */
@Mixin(value = GuiConfigEntries.class, remap = false)
public abstract class MixinGuiConfigEntries {

    @Inject(method = "initGui", at = @At("TAIL"))
    private void sisyphus$fitColumns(CallbackInfo ci) {
        ConfigScreenLayout.apply((GuiConfigEntries) (Object) this);
    }
}
