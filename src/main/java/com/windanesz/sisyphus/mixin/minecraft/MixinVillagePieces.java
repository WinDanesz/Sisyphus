package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.worldgen.VillageWorkstations;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

/**
 * Puts the job sites in the village pieces once each is built, see {@link VillageWorkstations}.
 */
@Mixin({StructureVillagePieces.House1.class, StructureVillagePieces.House2.class, StructureVillagePieces.House3.class,
        StructureVillagePieces.House4Garden.class, StructureVillagePieces.Hall.class, StructureVillagePieces.WoodHut.class,
        StructureVillagePieces.Field1.class, StructureVillagePieces.Field2.class, StructureVillagePieces.Well.class})
public abstract class MixinVillagePieces extends StructureVillagePieces.Village {

    @Inject(method = "addComponentParts", at = @At("RETURN"))
    private void sisyphus$addWorkstations(World world, Random random, StructureBoundingBox box, CallbackInfoReturnable<Boolean> cir) {
        // The piece found no ground in this chunk and was not built
        if (this.averageGroundLvl < 0 || this.getCoordBaseMode() == null) {
            return;
        }
        for (VillageWorkstations.Spot spot : VillageWorkstations.spotsOf(this)) {
            BlockPos pos = new BlockPos(this.getXWithOffset(spot.x, spot.z), this.getYWithOffset(spot.y), this.getZWithOffset(spot.x, spot.z));
            if (!box.isVecInside(pos) || !world.isAirBlock(pos) || world.isAirBlock(spot.hanging ? pos.up() : pos.down())) {
                continue;
            }
            IBlockState state = VillageWorkstations.stateOf(spot, VillageWorkstations.toWorld(spot.facing, this.getCoordBaseMode()));
            if (state != null) {
                world.setBlockState(pos, state, 2);
            }
        }
    }
}
