package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.worldgen.ArchaeologySites;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenDesertWells;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

/**
 * The desert well with its suspicious sand.
 */
@Mixin(WorldGenDesertWells.class)
public abstract class MixinWorldGenDesertWells {

    @Inject(method = "generate", at = @At("RETURN"))
    private void sisyphus$addSuspiciousSand(World world, Random random, BlockPos position, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !ArchaeologySites.desertEnabled() || world.getBlockState(position).getBlock() != Blocks.WATER) {
            return;
        }
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                world.setBlockState(position.add(x, -2, z), Blocks.SANDSTONE.getDefaultState(), 2);
            }
        }
        BlockPos below = position.down();
        world.setBlockState(below, Blocks.SAND.getDefaultState(), 2);
        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
            world.setBlockState(below.offset(facing), Blocks.SAND.getDefaultState(), 2);
        }
        BlockPos[] water = {position, position.east(), position.south(), position.west(), position.north()};
        ArchaeologySites.placeSuspiciousSand(world, null, water[random.nextInt(water.length)].down(1), "desert_well");
        ArchaeologySites.placeSuspiciousSand(world, null, water[random.nextInt(water.length)].down(2), "desert_well");
    }
}
