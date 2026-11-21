package com.windanesz.sisyphus.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TileEntityLectern extends TileEntity implements ITickable {

    private static final double MAP_RANGE_SQ = 64.0D * 64.0D;
    private final Set<UUID> mapViewers = new HashSet<>();
    private ItemStack book = ItemStack.EMPTY;
    private int page;

    public static String getPageText(ItemStack book, int page) {
        if (book.isEmpty() || !book.hasTagCompound()) {
            return "";
        }
        NBTTagList pages = book.getTagCompound().getTagList("pages", 8);
        if (page < 0 || page >= pages.tagCount()) {
            return "";
        }
        String text = pages.getStringTagAt(page);
        if (book.getItem() == Items.WRITTEN_BOOK) {
            try {
                return ITextComponent.Serializer.fromJsonLenient(text).getFormattedText();
            } catch (Exception e) {
                return text;
            }
        }
        return text;
    }

    public ItemStack getBook() {
        return this.book;
    }

    public void setBook(ItemStack book) {
        this.book = book;
        this.page = 0;
        this.mapViewers.clear();
        this.sync();
    }

    public boolean hasBook() {
        return !this.book.isEmpty();
    }

    public boolean hasReadableBook() {
        return this.book.getItem() == Items.WRITTEN_BOOK || this.book.getItem() == Items.WRITABLE_BOOK;
    }

    public boolean hasMap() {
        return this.book.getItem() == Items.FILLED_MAP;
    }

    @Override
    public void update() {
        if (this.world == null || this.world.isRemote) {
            return;
        }
        if (!this.hasMap()) {
            this.mapViewers.clear();
            return;
        }
        if (this.world.getTotalWorldTime() % 10L != 0L) {
            return;
        }
        MapData data = Items.FILLED_MAP.getMapData(this.book, this.world);
        if (data == null) {
            return;
        }
        Set<UUID> inRange = new HashSet<>();
        for (EntityPlayer player : this.world.playerEntities) {
            if (!(player instanceof EntityPlayerMP)
                    || player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) > MAP_RANGE_SQ) {
                continue;
            }
            inRange.add(player.getUniqueID());
            Packet<?> packet;
            if (this.mapViewers.contains(player.getUniqueID())) {
                // Only what changed on the map since
                packet = data.getMapPacket(this.book, this.world, player);
            } else {
                packet = new SPacketMaps(this.book.getMetadata(), data.scale, data.trackingPosition, data.mapDecorations.values(),
                        data.colors, 0, 0, 128, 128);
            }
            if (packet != null) {
                ((EntityPlayerMP) player).connection.sendPacket(packet);
            }
        }
        this.mapViewers.retainAll(inRange);
        this.mapViewers.addAll(inRange);
    }

    public int getPage() {
        return this.page;
    }

    public void updateBook(ItemStack book) {
        if (!ItemStack.areItemStacksEqual(this.book, book)) {
            this.book = book;
            this.sync();
        }
    }

    public ItemStack removeBook() {
        ItemStack old = this.book;
        this.book = ItemStack.EMPTY;
        this.page = 0;
        this.mapViewers.clear();
        this.sync();
        return old;
    }

    public int getPageCount() {
        if (this.book.isEmpty() || !this.book.hasTagCompound()) {
            return 0;
        }
        return this.book.getTagCompound().getTagList("pages", 8).tagCount();
    }

    /**
     * False if it was on that page already.
     */
    public boolean setPage(int page) {
        int clamped = Math.max(0, Math.min(page, Math.max(0, this.getPageCount() - 1)));
        if (clamped == this.page) {
            return false;
        }
        this.page = clamped;
        this.sync();
        return true;
    }

    /**
     * 1 to 15, by how far into the book the open page is. A map is a single page.
     */
    public int getRedstoneSignal() {
        int pages = this.getPageCount();
        if (!this.hasBook()) {
            return 0;
        }
        return pages > 1 ? (int) Math.floor((float) this.page / (float) (pages - 1) * 14.0F) + 1 : 1;
    }

    private void sync() {
        this.markDirty();
        if (this.world != null) {
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.book = compound.hasKey("Book", 10) ? new ItemStack(compound.getCompoundTag("Book")) : ItemStack.EMPTY;
        this.page = compound.getInteger("Page");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (!this.book.isEmpty()) {
            compound.setTag("Book", this.book.writeToNBT(new NBTTagCompound()));
        }
        compound.setInteger("Page", this.page);
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
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
