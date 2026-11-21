package com.windanesz.sisyphus.tileentity;

import com.windanesz.sisyphus.worldgen.ArchaeologyLoot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;

public class TileEntityDecoratedPot extends TileEntity implements IInventory {

    public static final String SHERDS = "sherds";
    private static final int WOBBLE_EVENT = 1;
    public long wobbleStartedAtTick;
    @Nullable
    public WobbleStyle lastWobbleStyle;
    private Item[] decorations = plain();
    private ItemStack item = ItemStack.EMPTY;
    private boolean cracked;

    public static Item[] plain() {
        return new Item[]{Items.BRICK, Items.BRICK, Items.BRICK, Items.BRICK};
    }

    public static Item[] readDecorations(@Nullable NBTTagCompound compound) {
        Item[] decorations = plain();
        if (compound == null || !compound.hasKey(SHERDS, 9)) {
            return decorations;
        }
        NBTTagList list = compound.getTagList(SHERDS, 8);
        for (int i = 0; i < Math.min(4, list.tagCount()); i++) {
            Item item = Item.REGISTRY.getObject(new ResourceLocation(list.getStringTagAt(i)));
            if (item != null && item != Items.AIR) {
                decorations[i] = item;
            }
        }
        return decorations;
    }

    public static void writeDecorations(NBTTagCompound compound, Item[] decorations) {
        if (Arrays.equals(decorations, plain())) {
            return;
        }
        NBTTagList list = new NBTTagList();
        for (Item item : decorations) {
            list.appendTag(new NBTTagString(String.valueOf(item.getRegistryName())));
        }
        compound.setTag(SHERDS, list);
    }

    public static Item[] decorationsOf(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return readDecorations(tag != null && tag.hasKey("BlockEntityTag", 10) ? tag.getCompoundTag("BlockEntityTag") : null);
    }

    /**
     * A pot of bricks only has no tag, so it stacks with the one from the creative inventory.
     */
    public static ItemStack createPotItem(Item potItem, Item[] decorations) {
        ItemStack stack = new ItemStack(potItem);
        NBTTagCompound entityTag = new NBTTagCompound();
        writeDecorations(entityTag, decorations);
        if (!entityTag.isEmpty()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setTag("BlockEntityTag", entityTag);
            stack.setTagCompound(tag);
        }
        return stack;
    }

    public static boolean isAllSherds(Item[] decorations) {
        for (Item item : decorations) {
            if (!ArchaeologyLoot.isSherd(item)) {
                return false;
            }
        }
        return true;
    }

    public static boolean canStack(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && a.getMetadata() == b.getMetadata() && ItemStack.areItemStackTagsEqual(a, b);
    }

    /**
     * Back, left, right, front.
     */
    public Item[] getDecorations() {
        return this.decorations.clone();
    }

    public void setDecorations(Item[] decorations) {
        this.decorations = decorations.clone();
        this.markDirty();
    }

    public boolean isCracked() {
        return this.cracked;
    }

    // The sides, to and from NBT

    public void setCracked(boolean cracked) {
        this.cracked = cracked;
    }

    public ItemStack getTheItem() {
        return this.item;
    }

    public void setTheItem(ItemStack stack) {
        this.item = stack;
        this.markDirty();
    }

    public void wobble(WobbleStyle style) {
        if (this.world != null && !this.world.isRemote) {
            this.world.addBlockEvent(this.pos, this.getBlockType(), WOBBLE_EVENT, style.ordinal());
        }
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == WOBBLE_EVENT && this.world != null && type >= 0 && type < WobbleStyle.values().length) {
            this.wobbleStartedAtTick = this.world.getTotalWorldTime();
            this.lastWobbleStyle = WobbleStyle.values()[type];
            return true;
        }
        return super.receiveClientEvent(id, type);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.decorations = readDecorations(compound);
        this.item = compound.hasKey("item", 10) ? new ItemStack(compound.getCompoundTag("item")) : ItemStack.EMPTY;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        writeDecorations(compound, this.decorations);
        if (!this.item.isEmpty()) {
            compound.setTag("item", this.item.writeToNBT(new NBTTagCompound()));
        }
        return compound;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        // The client draws the sides, and does not need what is inside
        NBTTagCompound compound = super.writeToNBT(new NBTTagCompound());
        writeDecorations(compound, this.decorations);
        return compound;
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        this.decorations = readDecorations(packet.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.decorations = readDecorations(compound);
    }

    @Override
    public boolean onlyOpsCanSetNbt() {
        // The item of a pot carries its sides in the BlockEntityTag, which has to be applied when anyone places it
        return false;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    // The one slot, for hoppers

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.item.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return index == 0 ? this.item : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index != 0 || this.item.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack split = this.item.splitStack(count);
        if (this.item.isEmpty()) {
            this.item = ItemStack.EMPTY;
        }
        this.markDirty();
        return split;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (index != 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = this.item;
        this.item = ItemStack.EMPTY;
        this.markDirty();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == 0) {
            this.item = stack;
            this.markDirty();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == 0 && (this.item.isEmpty() || (canStack(this.item, stack) && this.item.getCount() < this.item.getMaxStackSize()));
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.item = ItemStack.EMPTY;
        this.markDirty();
    }

    @Override
    public String getName() {
        return "tile.sisyphus:decorated_pot.name";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(this.getName());
    }

    public enum WobbleStyle {
        POSITIVE(7),
        NEGATIVE(10);

        public final int duration;

        WobbleStyle(int duration) {
            this.duration = duration;
        }
    }
}
