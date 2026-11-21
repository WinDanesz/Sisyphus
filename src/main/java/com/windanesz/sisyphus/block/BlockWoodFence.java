package com.windanesz.sisyphus.block;

import net.minecraft.block.BlockFence;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockWoodFence extends BlockFence {

    public BlockWoodFence(MapColor color, WoodSounds sounds) {
        super(Material.WOOD, color);
        this.setHardness(2.0F);
        this.setResistance(3.0F);
        this.setSoundType(sounds.type);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }
}
