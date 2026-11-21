package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockBuddingAmethyst extends Block {

    public BlockBuddingAmethyst() {
        super(Material.ROCK, MapColor.PURPLE);
        this.setHardness(1.5F);
        this.setResistance(1.5F);
        this.setSoundType(ModSounds.type("amethyst_block"));
        this.setHarvestLevel("pickaxe", 0);
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    public static boolean canGrowAt(IBlockState state) {
        return state.getMaterial() == Material.AIR || state.getBlock() == Blocks.WATER && state.getValue(BlockLiquid.LEVEL) == 0;
    }

    private static Block next(IBlockState state, EnumFacing side) {
        if (canGrowAt(state)) {
            return ModBlocks.get("small_amethyst_bud");
        }
        if (state.getBlock() instanceof BlockAmethystCluster && state.getValue(BlockAmethystCluster.FACING) == side) {
            String name = state.getBlock().getRegistryName().getPath();
            switch (name) {
                case "small_amethyst_bud":
                    return ModBlocks.get("medium_amethyst_bud");
                case "medium_amethyst_bud":
                    return ModBlocks.get("large_amethyst_bud");
                case "large_amethyst_bud":
                    return ModBlocks.get("amethyst_cluster");
                default:
                    return null;
            }
        }
        return null;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote || rand.nextInt(5) != 0) {
            return;
        }
        EnumFacing side = EnumFacing.byIndex(rand.nextInt(6));
        BlockPos at = pos.offset(side);
        Block grown = next(world.getBlockState(at), side);
        if (grown != null && grown != Blocks.AIR) {
            world.setBlockState(at, grown.getDefaultState().withProperty(BlockAmethystCluster.FACING, side), 3);
        }
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return false;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    }

    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }
}
