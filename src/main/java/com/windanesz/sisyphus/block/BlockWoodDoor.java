package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockWoodDoor extends BlockDoor {

    private final WoodSounds sounds;

    public BlockWoodDoor(WoodSounds sounds) {
        super(Material.WOOD);
        this.sounds = sounds;
        this.setHardness(3.0F);
        this.setSoundType(sounds.type);
        this.disableStats();
    }

    private Item doorItem() {
        return Item.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, this.getRegistryName().getPath()));
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(HALF) == EnumDoorHalf.UPPER ? Items.AIR : this.doorItem();
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(this.doorItem());
    }

    private void click(World world, BlockPos pos, boolean open, EntityPlayer player) {
        world.playSound(player, pos, open ? this.sounds.doorOpen : this.sounds.doorClose, SoundCategory.BLOCKS, 1.0F,
                world.rand.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX,
                                    float hitY, float hitZ) {
        BlockPos lower = state.getValue(HALF) == EnumDoorHalf.LOWER ? pos : pos.down();
        IBlockState lowerState = pos.equals(lower) ? state : world.getBlockState(lower);
        if (lowerState.getBlock() != this) {
            return false;
        }
        IBlockState toggled = lowerState.cycleProperty(OPEN);
        world.setBlockState(lower, toggled, 10);
        world.markBlockRangeForRenderUpdate(lower, pos);
        this.click(world, pos, toggled.getValue(OPEN), player);
        return true;
    }

    @Override
    public void toggleDoor(World world, BlockPos pos, boolean open) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != this) {
            return;
        }
        BlockPos lower = state.getValue(HALF) == EnumDoorHalf.LOWER ? pos : pos.down();
        IBlockState lowerState = pos.equals(lower) ? state : world.getBlockState(lower);
        if (lowerState.getBlock() == this && lowerState.getValue(OPEN) != open) {
            world.setBlockState(lower, lowerState.withProperty(OPEN, open), 10);
            world.markBlockRangeForRenderUpdate(lower, pos);
            this.click(world, pos, open, null);
        }
    }
}
