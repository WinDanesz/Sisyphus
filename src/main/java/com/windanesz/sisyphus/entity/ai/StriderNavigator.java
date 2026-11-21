package com.windanesz.sisyphus.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class StriderNavigator extends PathNavigateGround {

    public StriderNavigator(EntityLiving entity, World world) {
        super(entity, world);
    }

    @Override
    protected PathFinder getPathFinder() {
        this.nodeProcessor = new LavaNodeProcessor();
        this.nodeProcessor.setCanEnterDoors(true);
        return new PathFinder(this.nodeProcessor);
    }

    @Override
    public boolean canEntityStandOnPos(BlockPos pos) {
        return this.world.getBlockState(pos).getMaterial() == Material.LAVA || super.canEntityStandOnPos(pos);
    }

    private static final class LavaNodeProcessor extends WalkNodeProcessor {

        @Override
        protected PathNodeType getPathNodeTypeRaw(IBlockAccess world, int x, int y, int z) {
            PathNodeType type = super.getPathNodeTypeRaw(world, x, y, z);
            if (type == PathNodeType.LAVA && world.getBlockState(new BlockPos(x, y + 1, z)).getMaterial() != Material.LAVA) {
                return PathNodeType.WALKABLE;
            }
            return type;
        }
    }
}
