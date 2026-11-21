package com.windanesz.sisyphus.tileentity;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

/**
 * Keeps the inputs when the screen is closed, shared by everyone who opens it. The result is not kept, as it follows from the inputs.
 */
public class TileEntitySmithingTable extends TileEntity {

    public static final int SIZE = 3;

    private final InventoryBasic inventory = new InventoryBasic("Smithing", false, SIZE);

    public TileEntitySmithingTable() {
        this.inventory.addInventoryChangeListener(changed -> this.markDirty());
    }

    /**
     * Shared by every screen open on the table.
     */
    public InventoryBasic getInventory() {
        return this.inventory;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.inventory.clear();
        NBTTagList items = compound.getTagList("Items", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound tag = items.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot < SIZE) {
                this.inventory.setInventorySlotContents(slot, new ItemStack(tag));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList items = new NBTTagList();
        for (int slot = 0; slot < SIZE; slot++) {
            ItemStack stack = this.inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) slot);
                stack.writeToNBT(tag);
                items.appendTag(tag);
            }
        }
        compound.setTag("Items", items);
        return compound;
    }
}
