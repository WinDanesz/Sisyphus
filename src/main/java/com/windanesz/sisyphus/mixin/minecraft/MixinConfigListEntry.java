package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.client.gui.ConfigScreenLayout;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Cuts a config label short with "..." instead of drawing it under its button.
 */
@Mixin(value = GuiConfigEntries.ListEntryBase.class, remap = false)
public abstract class MixinConfigListEntry {

    @Shadow
    @Final
    protected GuiConfigEntries owningEntryList;

    @Shadow
    @Final
    protected Minecraft mc;

    // drawEntry is a method of the game that this class of Forge overrides, which the mappings do not know in it, so both of its names are
    // given: the one of the development environment and the one of a built game.
    @ModifyArg(method = {"drawEntry", "func_192634_a"}, remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I", remap = true), index = 0)
    private String sisyphus$trimLabel(String label) {
        return ConfigScreenLayout.trimLabel(mc.fontRenderer, label, owningEntryList);
    }
}
