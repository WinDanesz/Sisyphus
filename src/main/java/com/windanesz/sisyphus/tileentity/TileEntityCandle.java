package com.windanesz.sisyphus.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TileEntityCandle extends TileEntity {

    private final List<String> candles = new ArrayList<>();

    /**
     * Empty when all of them are the color of the block.
     */
    public List<String> getCandles() {
        return Collections.unmodifiableList(this.candles);
    }

    /**
     * The client is told when the block is set again.
     */
    public void setCandles(Block own, List<String> candles) {
        ResourceLocation name = own.getRegistryName();
        this.candles.clear();
        if (name != null && !candles.stream().allMatch(name.toString()::equals)) {
            this.candles.addAll(candles);
        }
        this.markDirty();
    }

    public List<String> candlesOf(Block own, int count) {
        List<String> names = new ArrayList<>(count);
        ResourceLocation name = own.getRegistryName();
        for (int i = 0; i < count; i++) {
            names.add(i < this.candles.size() ? this.candles.get(i) : String.valueOf(name));
        }
        return names;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.candles.clear();
        NBTTagList list = compound.getTagList("Candles", 8);
        for (int i = 0; i < list.tagCount(); i++) {
            this.candles.add(list.getStringTagAt(i));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (!this.candles.isEmpty()) {
            NBTTagList list = new NBTTagList();
            for (String candle : this.candles) {
                list.appendTag(new NBTTagString(candle));
            }
            compound.setTag("Candles", list);
        }
        return compound;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        this.readFromNBT(packet.getNbtCompound());
        if (this.world != null) {
            this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
