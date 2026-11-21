package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.client.SplashTexts;
import net.minecraft.client.gui.GuiMainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * Adds our splash texts just before the title screen picks one, so each is as likely as any vanilla splash.
 */
@Mixin(GuiMainMenu.class)
public abstract class MixinGuiMainMenu {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    private boolean sisyphus$addSplashes(List<String> splashes) {
        SplashTexts.addTo(splashes);
        return splashes.isEmpty();
    }
}
