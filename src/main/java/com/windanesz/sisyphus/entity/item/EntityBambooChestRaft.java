package com.windanesz.sisyphus.entity.item;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.living.EntityPiglin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;

public class EntityBambooChestRaft extends EntityBambooRaft implements IInventory {

    private static final int SIZE = 27;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    /**
     * False while it changes dimension.
     */
    private boolean dropContentsWhenDead = true;

    public EntityBambooChestRaft(World world) {
        super(world);
    }

    public EntityBambooChestRaft(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public Item getItemBoat() {
        return Item.REGISTRY.getObject(new ResourceLocation(SisyphusBackport.MODID, "bamboo_chest_raft"));
    }

    @Override
    protected boolean canFitPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);
        if (this.isPassenger(passenger)) {
            Vec3d forward = new Vec3d(0.15D, 0.0D, 0.0D).rotateYaw(-this.rotationYaw * 0.017453292F - ((float) Math.PI / 2F));
            passenger.setPosition(passenger.posX + forward.x, passenger.posY, passenger.posZ + forward.z);
        }
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (!player.isSneaking() && this.canFitPassenger(player)) {
            return super.processInitialInteract(player, hand);
        }
        if (!this.world.isRemote) {
            player.displayGUIChest(this);
            if (Settings.mobs.enablePiglins) {
                EntityPiglin.angerNearby(player, true);
            }
        }
        return true;
    }

    @Override
    public void setDead() {
        if (!this.world.isRemote && this.dropContentsWhenDead) {
            InventoryHelper.dropInventoryItems(this.world, this, this);
        }
        super.setDead();
    }

    @Nullable
    @Override
    public Entity changeDimension(int dimension, ITeleporter teleporter) {
        this.dropContentsWhenDead = false;
        return super.changeDimension(dimension, teleporter);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        ItemStackHelper.saveAllItems(compound, this.items);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.items.clear();
        ItemStackHelper.loadAllItems(compound, this.items);
    }

    @Override
    public int getSizeInventory() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.items.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(this.items, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.items, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.items.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return !this.isDead && player.getDistanceSq(this) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
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
        this.items.clear();
    }
}
