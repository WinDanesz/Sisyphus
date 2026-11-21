package com.windanesz.sisyphus.tileentity;

import com.windanesz.sisyphus.block.BlockFurnaceVariant;
import com.windanesz.sisyphus.recipe.CookingRecipes;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class TileEntityFurnaceVariant extends TileEntityLockable implements ITickable, ISidedInventory {

    private static final int[] SLOTS_TOP = {0};
    private static final int[] SLOTS_BOTTOM = {2, 1};
    private static final int[] SLOTS_SIDES = {1};

    private NonNullList<ItemStack> stacks = NonNullList.withSize(3, ItemStack.EMPTY);
    private int burnTime;
    private int currentItemBurnTime;
    private int cookTime;
    private int totalCookTime;
    private String customName;

    /**
     * Such as {@code container.smoker}.
     */
    protected abstract String getBaseName();

    protected abstract CookingRecipes recipes();

    public int getCookTime() {
        return 100;
    }

    public boolean isBurning() {
        return this.burnTime > 0;
    }

    @Override
    public int getSizeInventory() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.stacks.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(this.stacks, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(this.stacks, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        ItemStack old = this.stacks.get(index);
        boolean same = !stack.isEmpty() && stack.isItemEqual(old) && ItemStack.areItemStackTagsEqual(stack, old);
        this.stacks.set(index, stack);
        if (stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
        if (index == 0 && !same) {
            this.totalCookTime = this.getCookTime();
            this.cookTime = 0;
            this.markDirty();
        }
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : this.getBaseName();
    }

    @Override
    public boolean hasCustomName() {
        return this.customName != null && !this.customName.isEmpty();
    }

    public void setCustomInventoryName(String name) {
        this.customName = name;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.stacks);
        this.burnTime = compound.getInteger("BurnTime");
        this.cookTime = compound.getInteger("CookTime");
        this.totalCookTime = compound.getInteger("CookTimeTotal");
        this.currentItemBurnTime = TileEntityFurnace.getItemBurnTime(this.stacks.get(1));
        if (compound.hasKey("CustomName", 8)) {
            this.customName = compound.getString("CustomName");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("BurnTime", this.burnTime);
        compound.setInteger("CookTime", this.cookTime);
        compound.setInteger("CookTimeTotal", this.totalCookTime);
        ItemStackHelper.saveAllItems(compound, this.stacks);
        if (this.hasCustomName()) {
            compound.setString("CustomName", this.customName);
        }
        return compound;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void update() {
        boolean wasBurning = this.isBurning();
        boolean dirty = false;

        if (this.isBurning()) {
            --this.burnTime;
        }

        if (!this.world.isRemote) {
            ItemStack fuel = this.stacks.get(1);

            if (this.isBurning() || !fuel.isEmpty() && !this.stacks.get(0).isEmpty()) {
                if (!this.isBurning() && this.canSmelt()) {
                    this.burnTime = TileEntityFurnace.getItemBurnTime(fuel);
                    this.currentItemBurnTime = this.burnTime;

                    if (this.isBurning()) {
                        dirty = true;
                        if (!fuel.isEmpty()) {
                            Item item = fuel.getItem();
                            fuel.shrink(1);
                            if (fuel.isEmpty()) {
                                this.stacks.set(1, item.getContainerItem(fuel));
                            }
                        }
                    }
                }

                if (this.isBurning() && this.canSmelt()) {
                    ++this.cookTime;
                    if (this.cookTime >= this.totalCookTime) {
                        this.cookTime = 0;
                        this.totalCookTime = this.getCookTime();
                        this.smeltItem();
                        dirty = true;
                    }
                } else {
                    this.cookTime = 0;
                }
            } else if (!this.isBurning() && this.cookTime > 0) {
                this.cookTime = MathHelper.clamp(this.cookTime - 2, 0, this.totalCookTime);
            }

            if (wasBurning != this.isBurning()) {
                dirty = true;
                BlockFurnaceVariant.setLit(this.isBurning(), this.world, this.pos);
            }
        }

        if (dirty) {
            this.markDirty();
        }
    }

    private boolean canSmelt() {
        ItemStack input = this.stacks.get(0);
        if (input.isEmpty()) {
            return false;
        }
        ItemStack result = this.recipes().result(input);
        if (result.isEmpty()) {
            return false;
        }
        ItemStack output = this.stacks.get(2);
        if (output.isEmpty()) {
            return true;
        }
        if (!output.isItemEqual(result) || !ItemStack.areItemStackTagsEqual(output, result)) {
            return false;
        }
        int total = output.getCount() + result.getCount();
        return total <= this.getInventoryStackLimit() && total <= output.getMaxStackSize();
    }

    private void smeltItem() {
        if (!this.canSmelt()) {
            return;
        }
        ItemStack input = this.stacks.get(0);
        ItemStack result = this.recipes().result(input);
        ItemStack output = this.stacks.get(2);
        if (output.isEmpty()) {
            this.stacks.set(2, result.copy());
        } else if (output.getItem() == result.getItem()) {
            output.grow(result.getCount());
        }
        input.shrink(1);
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return this.world.getTileEntity(this.pos) == this
                && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == 2) {
            return false;
        }
        if (index == 1) {
            ItemStack fuel = this.stacks.get(1);
            return TileEntityFurnace.isItemFuel(stack) || SlotFurnaceFuel.isBucket(stack) && fuel.getItem() != Items.BUCKET;
        }
        return true;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return side == EnumFacing.DOWN ? SLOTS_BOTTOM : (side == EnumFacing.UP ? SLOTS_TOP : SLOTS_SIDES);
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        return this.isItemValidForSlot(index, stack);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        if (direction == EnumFacing.DOWN && index == 1) {
            Item item = stack.getItem();
            return item == Items.WATER_BUCKET || item == Items.BUCKET;
        }
        return true;
    }

    @Override
    public String getGuiID() {
        return "minecraft:furnace";
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player) {
        return new ContainerFurnace(playerInventory, this);
    }

    @Override
    public int getField(int id) {
        switch (id) {
            case 0:
                return this.burnTime;
            case 1:
                return this.currentItemBurnTime;
            case 2:
                return this.cookTime;
            case 3:
                return this.totalCookTime;
            default:
                return 0;
        }
    }

    @Override
    public void setField(int id, int value) {
        switch (id) {
            case 0:
                this.burnTime = value;
                break;
            case 1:
                this.currentItemBurnTime = value;
                break;
            case 2:
                this.cookTime = value;
                break;
            case 3:
                this.totalCookTime = value;
                break;
            default:
                break;
        }
    }

    @Override
    public int getFieldCount() {
        return 4;
    }

    @Override
    public void clear() {
        this.stacks.clear();
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos,
                                 IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }
}
