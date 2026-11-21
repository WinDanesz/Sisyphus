package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockGildedBlackstone extends Block {

    private static final double[] CHANCES = {0.1D, 1.0D / 7.0D, 0.25D, 1.0D};
    private static final Random RANDOM = new Random();

    public BlockGildedBlackstone() {
        super(Material.ROCK, MapColor.BLACK);
        this.setHardness(2.0F);
        this.setResistance(6.0F);
        this.setSoundType(ModSounds.type("gilded_blackstone"));
        this.setHarvestLevel("pickaxe", 0);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Random rand = world instanceof World ? ((World) world).rand : RANDOM;
        double chance = CHANCES[Math.max(0, Math.min(fortune, CHANCES.length - 1))];
        if (rand.nextDouble() < chance) {
            drops.add(new ItemStack(Items.GOLD_NUGGET, 2 + rand.nextInt(4)));
        } else {
            drops.add(new ItemStack(this));
        }
    }
}
