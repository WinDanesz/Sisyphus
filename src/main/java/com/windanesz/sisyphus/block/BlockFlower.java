package com.windanesz.sisyphus.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockFlower extends BlockBush {

    public BlockFlower() {
        super(Material.PLANTS);
        this.setSoundType(SoundType.PLANT);
        this.setHardness(0.0F);
        this.setCreativeTab(CreativeTabs.DECORATIONS);
    }
}
