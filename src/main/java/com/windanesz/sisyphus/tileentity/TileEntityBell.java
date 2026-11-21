package com.windanesz.sisyphus.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class TileEntityBell extends TileEntity implements ITickable {

    public static final int SWING_TICKS = 50;

    public int ticks;
    public boolean shaking;
    public EnumFacing direction = EnumFacing.NORTH;
    private boolean wasPowered;

    /**
     * Server side; the clients learn of it through a block event.
     */
    public void onHit(EnumFacing direction) {
        this.direction = direction;
        this.ticks = 0;
        this.shaking = true;
        if (this.world != null && !this.world.isRemote) {
            this.world.addBlockEvent(this.pos, this.getBlockType(), 1, direction.getIndex());
        }
    }

    public boolean wasPowered() {
        return this.wasPowered;
    }

    public void setWasPowered(boolean powered) {
        this.wasPowered = powered;
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            this.direction = EnumFacing.byIndex(type);
            this.ticks = 0;
            this.shaking = true;
            return true;
        }
        return super.receiveClientEvent(id, type);
    }

    @Override
    public void update() {
        if (this.shaking) {
            ++this.ticks;
        }
        if (this.ticks >= SWING_TICKS) {
            this.shaking = false;
            this.ticks = 0;
        }
    }
}
