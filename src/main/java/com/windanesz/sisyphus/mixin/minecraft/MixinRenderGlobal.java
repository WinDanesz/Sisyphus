package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tells the cherry leaves whether graphics are fancy whenever the world is rebuilt, as the game does for its own leaves.
 */
@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Inject(method = "loadRenderers", at = @At("HEAD"))
    private void sisyphus$setLeavesGraphics(CallbackInfo info) {
        Block leaves = ModBlocks.get("cherry_leaves");
        if (leaves instanceof BlockLeaves) {
            ((BlockLeaves) leaves).setGraphicsLevel(Minecraft.getMinecraft().gameSettings.fancyGraphics);
        }
    }
}
