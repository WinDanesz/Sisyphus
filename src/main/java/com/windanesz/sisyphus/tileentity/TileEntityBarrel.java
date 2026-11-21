package com.windanesz.sisyphus.tileentity;

import com.windanesz.sisyphus.block.BlockBarrel;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;

public class TileEntityBarrel extends TileEntityLockableLoot {

    private NonNullList<ItemStack> contents = NonNullList.withSize(27, ItemStack.EMPTY);
    private int playersUsing;

    @Override
    public int getSizeInventory() {
        return 27;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.contents) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : "container.barrel";
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.contents;
    }

    @Override
    public String getGuiID() {
        return "minecraft:chest";
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player) {
        this.fillWithLoot(player);
        return new ContainerChest(playerInventory, this, player);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.contents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        if (!this.checkLootAndRead(compound)) {
            ItemStackHelper.loadAllItems(compound, this.contents);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (!this.checkLootAndWrite(compound)) {
            ItemStackHelper.saveAllItems(compound, this.contents);
        }
        return compound;
    }

    @Override
    public void openInventory(EntityPlayer player) {
        if (player.isSpectator()) {
            return;
        }
        if (this.playersUsing < 0) {
            this.playersUsing = 0;
        }
        this.playersUsing++;
        if (this.playersUsing == 1) {
            this.setOpen(true);
            this.playSound(true);
        }
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (player.isSpectator()) {
            return;
        }
        this.playersUsing--;
        if (this.playersUsing <= 0) {
            this.playersUsing = 0;
            this.setOpen(false);
            this.playSound(false);
        }
    }

    private void playSound(boolean open) {
        if (this.world == null || this.world.isRemote) {
            return;
        }
        this.world.playSound(null, this.pos, open ? ModSounds.BLOCK_BARREL_OPEN : ModSounds.BLOCK_BARREL_CLOSE,
                SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.9F);
    }

    private void setOpen(boolean open) {
        if (this.world == null) {
            return;
        }
        IBlockState state = this.world.getBlockState(this.pos);
        if (state.getBlock() instanceof BlockBarrel) {
            this.world.setBlockState(this.pos, state.withProperty(BlockBarrel.OPEN, open), 3);
        }
    }
}
