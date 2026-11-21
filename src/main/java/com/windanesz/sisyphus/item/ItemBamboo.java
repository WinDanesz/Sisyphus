package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.block.BlockBamboo;
import com.windanesz.sisyphus.block.BlockBambooSapling;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemBamboo extends Item {

    public ItemBamboo() {
        this.setCreativeTab(CreativeTabs.MATERIALS);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        BlockPos target = world.getBlockState(pos).getBlock().isReplaceable(world, pos) ? pos : pos.offset(facing);
        if (!player.canPlayerEdit(target, facing, stack) || !world.getBlockState(target).getBlock().isReplaceable(world, target)) {
            return EnumActionResult.FAIL;
        }

        IBlockState below = world.getBlockState(target.down());
        Block bamboo = ModBlocks.get("bamboo");
        IBlockState place;
        if (below.getBlock() instanceof BlockBambooSapling) {
            place = bamboo.getDefaultState();
        } else if (below.getBlock() instanceof BlockBamboo) {
            place = bamboo.getDefaultState().withProperty(BlockBamboo.AGE, below.getValue(BlockBamboo.AGE) > 0 ? 1 : 0);
        } else if (BlockBamboo.canPlantOn(below)) {
            IBlockState above = world.getBlockState(target.up());
            place = above.getBlock() instanceof BlockBamboo
                    ? bamboo.getDefaultState().withProperty(BlockBamboo.AGE, above.getValue(BlockBamboo.AGE))
                    : ModBlocks.get("bamboo_sapling").getDefaultState();
        } else {
            return EnumActionResult.FAIL;
        }

        if (!world.isRemote) {
            world.setBlockState(target, place, 11);
            SoundType sound = place.getBlock().getSoundType();
            world.playSound(null, target, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public int getItemBurnTime(ItemStack stack) {
        return 50;
    }
}
