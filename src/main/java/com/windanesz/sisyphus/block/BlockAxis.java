package com.windanesz.sisyphus.block;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockAxis extends BlockRotatedPillar {

    public BlockAxis(Material material, MapColor color, SoundType sound, float hardness, float resistance) {
        super(material, color);
        this.setHardness(hardness);
        this.setResistance(resistance);
        this.setSoundType(sound);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }
}
