package com.windanesz.sisyphus.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockWoodTrapDoor extends BlockTrapDoor {

    private final WoodSounds sounds;

    public BlockWoodTrapDoor(WoodSounds sounds) {
        super(Material.WOOD);
        this.sounds = sounds;
        this.setHardness(3.0F);
        this.setSoundType(sounds.type);
        this.disableStats();
    }

    private void click(World world, BlockPos pos, boolean open, @Nullable EntityPlayer player) {
        world.playSound(player, pos, open ? this.sounds.trapdoorOpen : this.sounds.trapdoorClose, SoundCategory.BLOCKS, 1.0F,
                world.rand.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX,
                                    float hitY, float hitZ) {
        IBlockState toggled = state.cycleProperty(OPEN);
        world.setBlockState(pos, toggled, 2);
        this.click(world, pos, toggled.getValue(OPEN), player);
        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if (world.isRemote) {
            return;
        }
        boolean powered = world.isBlockPowered(pos);
        if ((powered || block.getDefaultState().canProvidePower()) && state.getValue(OPEN) != powered) {
            world.setBlockState(pos, state.withProperty(OPEN, powered), 2);
            this.click(world, pos, powered, null);
        }
    }
}
