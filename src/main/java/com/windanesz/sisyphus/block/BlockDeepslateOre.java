package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.BlockOre;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockDeepslateOre extends BlockOre {

    public BlockDeepslateOre(MapColor color) {
        super(color);
        this.setSoundType(ModSounds.type("deepslate"));
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (this == ModBlocks.deepslate_coal_ore) {
            return Items.COAL;
        } else if (this == ModBlocks.deepslate_diamond_ore) {
            return Items.DIAMOND;
        } else if (this == ModBlocks.deepslate_lapis_ore) {
            return Items.DYE;
        } else if (this == ModBlocks.deepslate_emerald_ore) {
            return Items.EMERALD;
        } else {
            return Item.getItemFromBlock(this);
        }
    }

    public int quantityDropped(Random random) {
        return this == ModBlocks.deepslate_lapis_ore ? 4 + random.nextInt(5) : 1;
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        Random rand = world instanceof World ? ((World) world).rand : new Random();
        if (this.getItemDropped(state, rand, fortune) != Item.getItemFromBlock(this)) {
            int i = 0;

            if (this == ModBlocks.deepslate_coal_ore) {
                i = MathHelper.getInt(rand, 0, 2);
            } else if (this == ModBlocks.deepslate_diamond_ore) {
                i = MathHelper.getInt(rand, 3, 7);
            } else if (this == ModBlocks.deepslate_emerald_ore) {
                i = MathHelper.getInt(rand, 3, 7);
            } else if (this == ModBlocks.deepslate_lapis_ore) {
                i = MathHelper.getInt(rand, 2, 5);
            }

            return i;
        }
        return 0;
    }

    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(this);
    }

    public int damageDropped(IBlockState state) {
        return this == ModBlocks.deepslate_lapis_ore ? EnumDyeColor.BLUE.getDyeDamage() : 0;
    }
}
