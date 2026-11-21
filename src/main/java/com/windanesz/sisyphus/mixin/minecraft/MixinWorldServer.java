package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.worldgen.camps.WorldGenAbandonedCamps;
import com.windanesz.sisyphus.worldgen.ruins.WorldGenRuins;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets /locate, and anything else asking for the nearest structure, find our structures.
 */
@Mixin(WorldServer.class)
public abstract class MixinWorldServer {

    @Inject(method = "findNearestStructure", at = @At("HEAD"), cancellable = true)
    private void sisyphus$locateRuins(String name, BlockPos pos, boolean findUnexplored, CallbackInfoReturnable<BlockPos> cir) {
        BlockPos found = WorldGenRuins.locate((WorldServer) (Object) this, name, pos, findUnexplored);
        if (found == null) {
            found = WorldGenAbandonedCamps.locate((WorldServer) (Object) this, name, pos);
        }
        if (found != null) {
            cir.setReturnValue(found);
        }
    }
}
