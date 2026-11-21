package com.windanesz.sisyphus.item;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemScaffolding extends ItemBlock {

    public ItemScaffolding(Block block) {
        super(block);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        IBlockState clicked = world.getBlockState(pos);
        if (facing != EnumFacing.UP || clicked.getBlock() != this.block) {
            return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
        }

        // Find the first free spot above the column
        BlockPos target = pos.up();
        while (world.getBlockState(target).getBlock() == this.block && target.getY() < world.getHeight()) {
            target = target.up();
        }

        ItemStack stack = player.getHeldItem(hand);
        if (target.getY() >= world.getHeight() || !world.getBlockState(target).getBlock().isReplaceable(world, target)
                || !player.canPlayerEdit(target, facing, stack) || !this.block.canPlaceBlockAt(world, target)) {
            return EnumActionResult.FAIL;
        }

        if (!world.isRemote) {
            world.setBlockState(target, this.block.getStateForPlacement(world, target, facing, hitX, hitY, hitZ, 0, player), 11);
            SoundType sound = this.block.getSoundType();
            world.playSound(null, target, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
