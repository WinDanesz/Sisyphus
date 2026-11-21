package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSweetBerries extends ItemFood {

    public ItemSweetBerries() {
        super(2, 0.4F, false);
        this.setCreativeTab(CreativeTabs.FOOD);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        BlockPos target = world.getBlockState(pos).getBlock().isReplaceable(world, pos) ? pos : pos.offset(facing);
        Block bush = ModBlocks.get("sweet_berry_bush");

        if (!player.canPlayerEdit(target, facing, stack) || !bush.canPlaceBlockAt(world, target)) {
            // Not a place for a bush: fall through to eating
            return EnumActionResult.PASS;
        }

        if (!world.isRemote) {
            world.setBlockState(target, bush.getDefaultState(), 11);
            SoundType sound = bush.getSoundType();
            world.playSound(null, target, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
