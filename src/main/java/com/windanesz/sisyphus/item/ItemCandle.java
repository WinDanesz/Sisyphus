package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.block.BlockCandle;
import com.windanesz.sisyphus.tileentity.TileEntityCandle;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemCandle extends ItemBlock {

    public ItemCandle(Block block) {
        super(block);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        // Either the candle that was clicked, or the one in the block that would be placed into
        for (BlockPos at : new BlockPos[]{pos, pos.offset(facing)}) {
            IBlockState state = world.getBlockState(at);
            if (state.getBlock() instanceof BlockCandle && state.getValue(BlockCandle.CANDLES) < 4 && player.canPlayerEdit(at, facing, stack)) {
                if (!world.isRemote) {
                    int candles = state.getValue(BlockCandle.CANDLES);
                    TileEntity tile = world.getTileEntity(at);
                    if (tile instanceof TileEntityCandle) {
                        TileEntityCandle colors = (TileEntityCandle) tile;
                        List<String> names = colors.candlesOf(state.getBlock(), candles);
                        names.add(String.valueOf(this.block.getRegistryName()));
                        colors.setCandles(state.getBlock(), names);
                    } else if (state.getBlock() != this.block) {
                        return EnumActionResult.FAIL;
                    }
                    world.setBlockState(at, state.withProperty(BlockCandle.CANDLES, candles + 1), 3);
                    SoundType sound = this.block.getSoundType(state, world, at, player);
                    world.playSound(null, at, sound.getPlaceSound(), SoundCategory.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
                    if (!player.capabilities.isCreativeMode) {
                        stack.shrink(1);
                    }
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }
}
