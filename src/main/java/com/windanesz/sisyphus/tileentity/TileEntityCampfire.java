package com.windanesz.sisyphus.tileentity;

import com.windanesz.sisyphus.block.BlockCampfire;
import com.windanesz.sisyphus.client.particle.CampfireParticles;
import com.windanesz.sisyphus.recipe.CookingRecipes;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

public class TileEntityCampfire extends TileEntity implements ITickable {

    public static final int COOK_TIME = 600;

    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private final int[] cookingTimes = new int[4];

    public static ItemStack getResult(ItemStack stack) {
        return CookingRecipes.CAMPFIRE.result(stack);
    }

    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public boolean addItem(ItemStack stack) {
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i).isEmpty()) {
                ItemStack single = stack.copy();
                single.setCount(1);
                this.items.set(i, single);
                this.cookingTimes[i] = 0;
                this.sync();
                return true;
            }
        }
        return false;
    }

    public void dropItems() {
        if (this.world == null || this.world.isRemote) {
            return;
        }
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack stack = this.items.get(i);
            if (!stack.isEmpty()) {
                this.world.spawnEntity(new EntityItem(this.world, this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D, stack));
                this.items.set(i, ItemStack.EMPTY);
                this.cookingTimes[i] = 0;
            }
        }
        this.sync();
    }

    @SideOnly(Side.CLIENT)
    private void spawnSmoke() {
        IBlockState state = this.world.getBlockState(this.pos);
        if (!(state.getBlock() instanceof BlockCampfire) || !state.getValue(BlockCampfire.LIT) || !CampfireParticles.isSignalFire(this.world, this.pos)) {
            return;
        }
        Random rand = this.world.rand;
        if (rand.nextFloat() < 0.11F) {
            for (int i = 0; i < rand.nextInt(2) + 2; i++) {
                CampfireParticles.makeSmoke(this.world, this.pos);
            }
        }
    }

    @Override
    public void update() {
        if (this.world == null) {
            return;
        }
        if (this.world.isRemote) {
            this.spawnSmoke();
            return;
        }
        IBlockState state = this.world.getBlockState(this.pos);
        boolean lit = state.getBlock() instanceof BlockCampfire && state.getValue(BlockCampfire.LIT);
        boolean changed = false;

        for (int i = 0; i < this.items.size(); i++) {
            ItemStack stack = this.items.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (lit) {
                if (++this.cookingTimes[i] >= COOK_TIME) {
                    ItemStack result = getResult(stack);
                    this.items.set(i, ItemStack.EMPTY);
                    this.cookingTimes[i] = 0;
                    if (!result.isEmpty()) {
                        this.world.spawnEntity(new EntityItem(this.world, this.pos.getX() + 0.5D, this.pos.getY() + 1.0D, this.pos.getZ() + 0.5D, result.copy()));
                    }
                    changed = true;
                }
            } else if (this.cookingTimes[i] > 0) {
                this.cookingTimes[i] = MathHelper.clamp(this.cookingTimes[i] - 2, 0, COOK_TIME);
            }
        }

        if (changed) {
            this.sync();
        }
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
        this.items.clear();
        ItemStackHelper.loadAllItems(compound, this.items);
        int[] times = compound.getIntArray("CookingTimes");
        for (int i = 0; i < this.cookingTimes.length; i++) {
            this.cookingTimes[i] = i < times.length ? times[i] : 0;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        ItemStackHelper.saveAllItems(compound, this.items, true);
        compound.setIntArray("CookingTimes", this.cookingTimes);
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
