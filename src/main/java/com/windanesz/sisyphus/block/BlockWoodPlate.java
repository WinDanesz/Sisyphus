package com.windanesz.sisyphus.block;

import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockWoodPlate extends BlockPressurePlate {

    private final WoodSounds sounds;

    public BlockWoodPlate(WoodSounds sounds) {
        super(Material.WOOD, Sensitivity.EVERYTHING);
        this.sounds = sounds;
        this.setHardness(0.5F);
        this.setSoundType(sounds.type);
        this.setCreativeTab(CreativeTabs.REDSTONE);
    }

    @Override
    protected void playClickOnSound(World world, BlockPos pos) {
        world.playSound(null, pos, this.sounds.plateOn, SoundCategory.BLOCKS, 0.3F, 0.8F);
    }

    @Override
    protected void playClickOffSound(World world, BlockPos pos) {
        world.playSound(null, pos, this.sounds.plateOff, SoundCategory.BLOCKS, 0.3F, 0.7F);
    }
}
