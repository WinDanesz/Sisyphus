package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

import java.util.Random;

public class BlockMoss extends Block implements IGrowable {

    private static final int SPREAD_RADIUS = 3;

    public BlockMoss() {
        super(Material.GRASS);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setSoundType(SoundType.PLANT);
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return worldIn.isAirBlock(pos.up());
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        for (int dx = -SPREAD_RADIUS; dx <= SPREAD_RADIUS; dx++) {
            for (int dz = -SPREAD_RADIUS; dz <= SPREAD_RADIUS; dz++) {
                if (dx * dx + dz * dz > SPREAD_RADIUS * SPREAD_RADIUS + 1) continue;
                for (int dy = 1; dy >= -1; dy--) {
                    BlockPos target = pos.add(dx, dy, dz);
                    IBlockState targetState = worldIn.getBlockState(target);
                    Material material = targetState.getMaterial();
                    boolean replaceable = targetState.getBlock() != this && targetState.isFullCube()
                            && (material == Material.GROUND || material == Material.GRASS || material == Material.ROCK || material == Material.SAND);
                    if (!replaceable || !worldIn.isAirBlock(target.up())) continue;

                    if (rand.nextFloat() < 0.75F) {
                        worldIn.setBlockState(target, this.getDefaultState(), 3);
                        if (rand.nextFloat() < 0.6F) {
                            worldIn.setBlockState(target.up(), ModBlocks.moss_carpet.getDefaultState(), 3);
                        } else if (rand.nextFloat() < 0.12F) {
                            Block azalea = ModBlocks.get(rand.nextInt(3) == 0 ? "flowering_azalea" : "azalea");
                            if (azalea != Blocks.AIR) {
                                worldIn.setBlockState(target.up(), azalea.getDefaultState(), 3);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(ModBlocks.moss_block);
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction,
                                   IPlantable plantable) {
        return plantable.getPlantType(world, pos.offset(direction)) == EnumPlantType.Plains
                || super.canSustainPlant(state, world, pos, direction, plantable);
    }

    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return MapColor.getBlockColor(EnumDyeColor.CYAN);
    }
}
