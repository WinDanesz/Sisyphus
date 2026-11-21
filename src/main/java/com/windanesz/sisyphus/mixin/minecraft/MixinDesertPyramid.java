package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.worldgen.ArchaeologySites;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockStainedHardenedClay;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.ComponentScatteredFeaturePieces;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The hidden room under the desert pyramid.
 */
@Mixin(ComponentScatteredFeaturePieces.DesertPyramid.class)
public abstract class MixinDesertPyramid extends StructureComponent {

    @Inject(method = "addComponentParts", at = @At("RETURN"))
    private void sisyphus$addCellar(World world, Random random, StructureBoundingBox box, CallbackInfoReturnable<Boolean> cir) {
        if (!ArchaeologySites.desertEnabled()) {
            return;
        }
        // The middle of the room, as in the game
        int x = 16;
        int y = -4;
        int z = 13;
        List<BlockPos> sand = new ArrayList<>();
        this.sisyphus$addCellarStairs(world, random, box, x, y, z);
        this.sisyphus$addCellarRoom(world, random, box, x, y, z, sand);

        Random shared = new Random(world.getSeed() ^ (this.boundingBox.minX * 341873128712L + this.boundingBox.minZ * 132897987541L));
        // One block of the collapsed roof
        int roofX = x - 2 + shared.nextInt(5);
        int roofZ = z - 2 + shared.nextInt(5);
        ArchaeologySites.placeSuspiciousSand(world, box, this.sisyphus$world(roofX, y + 4, roofZ), "desert_pyramid");

        Collections.shuffle(sand, shared);
        int suspicious = Math.min(sand.size(), 5 + shared.nextInt(3));
        for (int i = 0; i < sand.size(); i++) {
            BlockPos pos = sand.get(i);
            if (i < suspicious) {
                ArchaeologySites.placeSuspiciousSand(world, box, pos, "desert_pyramid");
            } else if (box.isVecInside(pos)) {
                world.setBlockState(pos, Blocks.SAND.getDefaultState(), 2);
            }
        }
    }

    private BlockPos sisyphus$world(int x, int y, int z) {
        return new BlockPos(this.getXWithOffset(x, z), this.getYWithOffset(y), this.getZWithOffset(x, z));
    }

    private void sisyphus$addCellarStairs(World world, Random random, StructureBoundingBox box, int x, int y, int z) {
        IBlockState stairs = Blocks.SANDSTONE_STAIRS.getDefaultState().withRotation(Rotation.COUNTERCLOCKWISE_90);
        this.setBlockState(world, stairs, 13, -1, 17, box);
        this.setBlockState(world, stairs, 14, -2, 17, box);
        this.setBlockState(world, stairs, 15, -3, 17, box);
        IBlockState sand = Blocks.SAND.getDefaultState();
        IBlockState sandstone = Blocks.SANDSTONE.getDefaultState();
        boolean flag = random.nextBoolean();
        for (int dx = -4; dx <= 0; dx++) {
            this.setBlockState(world, sand, x + dx, y + 4, z + 4, box);
        }
        this.setBlockState(world, sand, x - 2, y + 3, z + 4, box);
        this.setBlockState(world, flag ? sand : sandstone, x - 1, y + 3, z + 4, box);
        this.setBlockState(world, !flag ? sand : sandstone, x, y + 3, z + 4, box);
        this.setBlockState(world, sand, x - 1, y + 2, z + 4, box);
        this.setBlockState(world, sandstone, x, y + 2, z + 4, box);
        this.setBlockState(world, sand, x, y + 1, z + 4, box);
    }

    private void sisyphus$addCellarRoom(World world, Random random, StructureBoundingBox box, int x, int y, int z, List<BlockPos> sand) {
        IBlockState cut = Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.SMOOTH);
        IBlockState chiseled = Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.CHISELED);
        this.fillWithBlocks(world, box, x - 3, y + 1, z - 3, x - 3, y + 1, z + 2, cut, cut, true);
        this.fillWithBlocks(world, box, x + 3, y + 1, z - 3, x + 3, y + 1, z + 2, cut, cut, true);
        this.fillWithBlocks(world, box, x - 3, y + 1, z - 3, x + 3, y + 1, z - 2, cut, cut, true);
        this.fillWithBlocks(world, box, x - 3, y + 1, z + 3, x + 3, y + 1, z + 3, cut, cut, true);
        this.fillWithBlocks(world, box, x - 3, y + 2, z - 3, x - 3, y + 2, z + 2, chiseled, chiseled, true);
        this.fillWithBlocks(world, box, x + 3, y + 2, z - 3, x + 3, y + 2, z + 2, chiseled, chiseled, true);
        this.fillWithBlocks(world, box, x - 3, y + 2, z - 3, x + 3, y + 2, z - 2, chiseled, chiseled, true);
        this.fillWithBlocks(world, box, x - 3, y + 2, z + 3, x + 3, y + 2, z + 3, chiseled, chiseled, true);
        this.fillWithBlocks(world, box, x - 3, -1, z - 3, x - 3, -1, z + 2, cut, cut, true);
        this.fillWithBlocks(world, box, x + 3, -1, z - 3, x + 3, -1, z + 2, cut, cut, true);
        this.fillWithBlocks(world, box, x - 3, -1, z - 3, x + 3, -1, z - 2, cut, cut, true);
        this.fillWithBlocks(world, box, x - 3, -1, z + 3, x + 3, -1, z + 3, cut, cut, true);
        for (int sy = y + 1; sy <= y + 3; sy++) {
            for (int sx = x - 2; sx <= x + 2; sx++) {
                for (int sz = z - 2; sz <= z + 2; sz++) {
                    sand.add(this.sisyphus$world(sx, sy, sz));
                }
            }
        }
        // The collapsed roof: sand, and a third of it sandstone
        for (int rx = x - 2; rx <= x + 2; rx++) {
            for (int rz = z - 2; rz <= z + 2; rz++) {
                this.setBlockState(world, random.nextFloat() < 0.33F ? Blocks.SANDSTONE.getDefaultState() : Blocks.SAND.getDefaultState(), rx, y + 4, rz, box);
            }
        }
        IBlockState orange = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.ORANGE);
        IBlockState blue = Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.BLUE);
        this.setBlockState(world, blue, x, y, z, box);
        this.setBlockState(world, orange, x + 1, y, z - 1, box);
        this.setBlockState(world, orange, x + 1, y, z + 1, box);
        this.setBlockState(world, orange, x - 1, y, z - 1, box);
        this.setBlockState(world, orange, x - 1, y, z + 1, box);
        this.setBlockState(world, orange, x + 2, y, z, box);
        this.setBlockState(world, orange, x - 2, y, z, box);
        this.setBlockState(world, orange, x, y, z + 2, box);
        this.setBlockState(world, orange, x, y, z - 2, box);
        this.setBlockState(world, orange, x + 3, y, z, box);
        sand.add(this.sisyphus$world(x + 3, y + 1, z));
        sand.add(this.sisyphus$world(x + 3, y + 2, z));
        this.setBlockState(world, cut, x + 4, y + 1, z, box);
        this.setBlockState(world, chiseled, x + 4, y + 2, z, box);
        this.setBlockState(world, orange, x - 3, y, z, box);
        sand.add(this.sisyphus$world(x - 3, y + 1, z));
        sand.add(this.sisyphus$world(x - 3, y + 2, z));
        this.setBlockState(world, cut, x - 4, y + 1, z, box);
        this.setBlockState(world, chiseled, x - 4, y + 2, z, box);
        this.setBlockState(world, orange, x, y, z + 3, box);
        sand.add(this.sisyphus$world(x, y + 1, z + 3));
        sand.add(this.sisyphus$world(x, y + 2, z + 3));
        this.setBlockState(world, orange, x, y, z - 3, box);
        sand.add(this.sisyphus$world(x, y + 1, z - 3));
        sand.add(this.sisyphus$world(x, y + 2, z - 3));
        this.setBlockState(world, cut, x, y + 1, z - 4, box);
        this.setBlockState(world, chiseled, x, -2, z - 4, box);
    }
}
