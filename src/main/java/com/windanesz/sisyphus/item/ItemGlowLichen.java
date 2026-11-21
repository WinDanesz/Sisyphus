package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.block.BlockGlowLichen;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemGlowLichen extends ItemBlock {

    public ItemGlowLichen(Block block) {
        super(block);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        BlockPos target = pos.offset(facing);
        if (!player.canPlayerEdit(target, facing, stack)) {
            return EnumActionResult.FAIL;
        }
        // The lichen is on the side of the new block that touches the block that was clicked
        if (!BlockGlowLichen.addFace(world, target, facing.getOpposite())) {
            return EnumActionResult.FAIL;
        }
        SoundType sound = this.block.getSoundType();
        world.playSound(player, target, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }
}
