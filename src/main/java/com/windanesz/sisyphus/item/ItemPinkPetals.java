package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.block.BlockPinkPetals;
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

public class ItemPinkPetals extends ItemBlock {

    public ItemPinkPetals(Block block) {
        super(block);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) {
            BlockPos target = this.canAddTo(world.getBlockState(pos)) ? pos : pos.offset(facing);
            IBlockState state = world.getBlockState(target);
            if (this.canAddTo(state)) {
                ItemStack stack = player.getHeldItem(hand);
                if (!player.canPlayerEdit(target, facing, stack)) {
                    return EnumActionResult.FAIL;
                }
                if (!world.isRemote) {
                    world.setBlockState(target, state.withProperty(BlockPinkPetals.AMOUNT, state.getValue(BlockPinkPetals.AMOUNT) + 1), 3);
                    SoundType sound = this.block.getSoundType(state, world, target, player);
                    world.playSound(null, target, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                    if (!player.capabilities.isCreativeMode) {
                        stack.shrink(1);
                    }
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    private boolean canAddTo(IBlockState state) {
        return state.getBlock() == this.block && state.getValue(BlockPinkPetals.AMOUNT) < BlockPinkPetals.MAX;
    }
}
