package com.windanesz.sisyphus.block;

import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockStonePlate extends BlockPressurePlate {

    public BlockStonePlate() {
        super(Material.ROCK, Sensitivity.MOBS);
        this.setHardness(0.5F);
        this.setSoundType(SoundType.STONE);
        this.setCreativeTab(CreativeTabs.REDSTONE);
    }
}
