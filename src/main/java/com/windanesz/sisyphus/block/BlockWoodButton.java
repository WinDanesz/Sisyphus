package com.windanesz.sisyphus.block;

import net.minecraft.block.BlockButtonWood;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockWoodButton extends BlockButtonWood {

    private final WoodSounds sounds;

    public BlockWoodButton(WoodSounds sounds) {
        super();
        this.sounds = sounds;
        this.setHardness(0.5F);
        this.setSoundType(sounds.type);
    }

    @Override
    protected void playClickSound(@Nullable EntityPlayer player, World world, BlockPos pos) {
        world.playSound(player, pos, this.sounds.buttonOn, SoundCategory.BLOCKS, 0.3F, 0.6F);
    }

    @Override
    protected void playReleaseSound(World world, BlockPos pos) {
        world.playSound(null, pos, this.sounds.buttonOff, SoundCategory.BLOCKS, 0.3F, 0.5F);
    }
}
