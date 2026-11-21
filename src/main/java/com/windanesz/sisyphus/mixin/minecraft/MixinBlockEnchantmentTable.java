package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;

/**
 * The enchanting table gives light.
 */
@Mixin(BlockEnchantmentTable.class)
public abstract class MixinBlockEnchantmentTable extends Block {

    private MixinBlockEnchantmentTable(Material material) {
        super(material);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return Settings.gameplay.enableEnchantingTableLight ? 7 : super.getLightValue(state, world, pos);
    }
}
