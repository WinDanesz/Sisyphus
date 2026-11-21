package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class BlockDeepslateOreVariant extends Block {

    private final Block original;

    public BlockDeepslateOreVariant(Block original) {
        super(Material.ROCK, MapColor.GRAY);
        this.original = original;
        this.setSoundType(ModSounds.type("deepslate"));
        this.setHardness(hardnessOf(original) * 1.5F);
        this.setResistance(original.getExplosionResistance(null) * 5.0F / 3.0F);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    private static float hardnessOf(Block block) {
        try {
            return block.getBlockHardness(block.getDefaultState(), null, BlockPos.ORIGIN);
        } catch (RuntimeException e) {
            return 3.0F;
        }
    }

    public Block getOriginal() {
        return original;
    }

    @Nullable
    @Override
    public String getHarvestTool(IBlockState state) {
        String tool = original.getHarvestTool(original.getDefaultState());
        return tool == null ? "pickaxe" : tool;
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        return original.getHarvestLevel(original.getDefaultState());
    }

    @Override
    public int getLightValue(IBlockState state) {
        return original.getLightValue(original.getDefaultState());
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        return original.getExpDrop(original.getDefaultState(), world, pos, fortune);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        NonNullList<ItemStack> inherited = NonNullList.create();
        original.getDrops(inherited, world, pos, original.getDefaultState(), fortune);
        Item itself = Item.getItemFromBlock(original);
        for (ItemStack drop : inherited) {
            if (drop.getItem() == itself) {
                drops.add(new ItemStack(this, drop.getCount()));
            } else {
                drops.add(drop);
            }
        }
    }
}
