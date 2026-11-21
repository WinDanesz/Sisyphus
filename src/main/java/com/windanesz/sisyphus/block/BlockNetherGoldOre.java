package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockNetherGoldOre extends Block {

    private static final Random RANDOM = new Random();

    public BlockNetherGoldOre() {
        super(Material.ROCK, MapColor.NETHERRACK);
        this.setHardness(3.0F);
        this.setResistance(3.0F);
        this.setSoundType(ModSounds.type("nether_gold_ore"));
        this.setHarvestLevel("pickaxe", 0);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.GOLD_NUGGET;
    }

    @Override
    public int quantityDropped(Random random) {
        return 2 + random.nextInt(5);
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        int count = this.quantityDropped(random);
        if (fortune > 0) {
            int bonus = random.nextInt(fortune + 2) - 1;
            count *= Math.max(bonus, 0) + 1;
        }
        return count;
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        Random rand = world instanceof World ? ((World) world).rand : RANDOM;
        return rand.nextInt(2);
    }
}
