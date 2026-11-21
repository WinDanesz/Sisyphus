package com.windanesz.sisyphus.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;

public class TileEntityHangingSign extends TileEntitySign {

    public static final int MAX_LINE_WIDTH = 60;
    public static final int LINE_HEIGHT = 9;

    private boolean attached;

    public boolean isAttached() {
        return this.attached;
    }

    public void setAttached(boolean attached) {
        this.attached = attached;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("Attached", this.attached);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.attached = compound.getBoolean("Attached");
    }
}
