package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

import java.util.Random;

public class BlockCobbledDeepslate extends Block {

    public BlockCobbledDeepslate() {
        super(Material.ROCK);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setSoundType(ModSounds.type("deepslate"));
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(ModBlocks.cobbled_deepslate);
    }
}
