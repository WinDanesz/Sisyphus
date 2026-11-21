package com.windanesz.sisyphus.worldgen;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.registry.ModBlocks;
import com.windanesz.sisyphus.tileentity.TileEntityBrushable;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nullable;

public final class ArchaeologySites {

    private ArchaeologySites() {
    }

    /**
     * Read when they are built, as the mixins are loaded before the config.
     */
    public static boolean desertEnabled() {
        return Settings.archaeology.enableArchaeology && Settings.worldGeneration.enableDesertArchaeology;
    }

    public static boolean isGenerated(String table) {
        if (table.startsWith("desert_")) {
            return desertEnabled();
        }
        return Settings.archaeology.enableArchaeology && (table.startsWith("ocean_ruin_") ? Settings.worldGeneration.oceanRuins.enabled : Settings.worldGeneration.trailRuins.enabled);
    }

    public static void placeSuspiciousSand(World world, @Nullable StructureBoundingBox box, BlockPos pos, String table) {
        placeSuspicious(world, box, pos, ModBlocks.get("suspicious_sand"), table);
    }

    public static void placeSuspicious(World world, @Nullable StructureBoundingBox box, BlockPos pos, Block block, String table) {
        if (block == Blocks.AIR || (box != null && !box.isVecInside(pos))) {
            return;
        }
        world.setBlockState(pos, block.getDefaultState(), 2);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityBrushable) {
            // The seed of the loot is the position, as in the game
            ((TileEntityBrushable) tile).setLootTable(ArchaeologyLoot.lootTable(table), pos.toLong());
        }
    }
}
