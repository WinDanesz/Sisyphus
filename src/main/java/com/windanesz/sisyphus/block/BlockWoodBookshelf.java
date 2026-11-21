package com.windanesz.sisyphus.block;

import net.minecraft.block.BlockBookshelf;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockWoodBookshelf extends BlockBookshelf {

    private final MapColor color;

    public BlockWoodBookshelf(MapColor color, SoundType sound) {
        this.color = color;
        this.setHardness(1.5F);
        this.setSoundType(sound);
    }

    @Override
    public float getEnchantPowerBonus(World world, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
        return color;
    }
}
