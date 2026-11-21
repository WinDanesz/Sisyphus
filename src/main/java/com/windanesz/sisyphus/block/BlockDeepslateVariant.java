package com.windanesz.sisyphus.block;

import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;

public class BlockDeepslateVariant extends Block {

    public BlockDeepslateVariant() {
        this(false);
    }

    public BlockDeepslateVariant(boolean isBricks) {
        this(isBricks ? ModSounds.type("deepslate_bricks") : ModSounds.type("deepslate"));
    }

    public BlockDeepslateVariant(SoundType soundType) {
        super(Material.ROCK);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setSoundType(soundType);
    }

    @Override
    public EnumPushReaction getPushReaction(IBlockState state) {
        return state.getBlock() == ModBlocks.reinforced_deepslate ? EnumPushReaction.IGNORE : super.getPushReaction(state);
    }
}
