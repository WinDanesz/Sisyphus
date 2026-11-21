package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.block.BlockSmallDripleaf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
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

public class ItemSmallDripleaf extends ItemBlock {

    public ItemSmallDripleaf(Block block) {
        super(block);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        BlockPos lower = world.getBlockState(pos).getBlock().isReplaceable(world, pos) ? pos : pos.offset(facing);
        BlockPos upper = lower.up();
        if (!player.canPlayerEdit(lower, facing, stack) || !player.canPlayerEdit(upper, facing, stack) || !BlockSmallDripleaf.canGrowOn(world.getBlockState(lower.down()))
                || !world.getBlockState(lower).getBlock().isReplaceable(world, lower) || !world.getBlockState(upper).getBlock().isReplaceable(world, upper)
                || upper.getY() >= world.getHeight()) {
            return EnumActionResult.FAIL;
        }
        if (!world.isRemote) {
            EnumFacing direction = player.getHorizontalFacing().getOpposite();
            IBlockState base = this.block.getDefaultState().withProperty(BlockSmallDripleaf.FACING, direction);
            world.setBlockState(lower, base.withProperty(BlockSmallDripleaf.HALF, BlockDoublePlant.EnumBlockHalf.LOWER), 2);
            world.setBlockState(upper, base.withProperty(BlockSmallDripleaf.HALF, BlockDoublePlant.EnumBlockHalf.UPPER), 3);
            SoundType sound = this.block.getSoundType();
            world.playSound(null, lower, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            if (!player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
