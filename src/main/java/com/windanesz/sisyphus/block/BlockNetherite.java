package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockNetherite extends Block {

    public BlockNetherite() {
        super(Material.IRON, MapColor.BLACK);
        this.setHardness(50.0F);
        this.setResistance(1200.0F);
        this.setSoundType(ModSounds.type("netherite_block"));
        this.setHarvestLevel("pickaxe", 3);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beacon) {
        return true;
    }
}
