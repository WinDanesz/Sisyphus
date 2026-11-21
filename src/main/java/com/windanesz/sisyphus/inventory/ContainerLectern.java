package com.windanesz.sisyphus.inventory;

import com.windanesz.sisyphus.block.BlockLectern;
import com.windanesz.sisyphus.tileentity.TileEntityLectern;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerLectern extends Container {

    public static final int TAKE_BOOK = 1000;

    private final World world;
    private final BlockPos pos;

    public ContainerLectern(World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int id) {
        TileEntity tile = this.world.getTileEntity(this.pos);
        IBlockState state = this.world.getBlockState(this.pos);
        if (!(tile instanceof TileEntityLectern) || !(state.getBlock() instanceof BlockLectern)) {
            return false;
        }
        TileEntityLectern lectern = (TileEntityLectern) tile;
        BlockLectern block = (BlockLectern) state.getBlock();

        if (id == TAKE_BOOK) {
            if (lectern.hasBook()) {
                block.giveBook(this.world, this.pos, state, lectern, player);
            }
            player.closeScreen();
            return true;
        }
        if (lectern.setPage(id)) {
            block.pulse(this.world, this.pos, this.world.getBlockState(this.pos));
        }
        return true;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        TileEntity tile = this.world.getTileEntity(this.pos);
        return tile instanceof TileEntityLectern && ((TileEntityLectern) tile).hasBook()
                && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return ItemStack.EMPTY;
    }
}
