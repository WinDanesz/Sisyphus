package com.windanesz.sisyphus.inventory;

import com.windanesz.sisyphus.block.BlockStonecutter;
import com.windanesz.sisyphus.recipe.StonecutterRecipes;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class ContainerStonecutter extends Container {

    private final World world;
    private final BlockPos pos;
    private final IInventory result = new InventoryBasic("Stonecutter result", false, 1);
    private List<ItemStack> results = Collections.emptyList();    private final IInventory input = new InventoryBasic("Stonecutter", false, 1) {
        @Override
        public void markDirty() {
            super.markDirty();
            ContainerStonecutter.this.onCraftMatrixChanged(this);
        }
    };
    private ItemStack lastInput = ItemStack.EMPTY;
    private int selected = -1;
    private int sentSelected = -2;
    public ContainerStonecutter(InventoryPlayer playerInventory, World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;

        this.addSlotToContainer(new Slot(this.input, 0, 20, 33));
        this.addSlotToContainer(new Slot(this.result, 0, 143, 33) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public ItemStack onTake(EntityPlayer player, ItemStack stack) {
                input.decrStackSize(0, 1);
                updateResult();
                if (!world.isRemote) {
                    world.playSound(null, pos, ModSounds.UI_STONECUTTER_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
                return stack;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlotToContainer(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            this.addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    public List<ItemStack> getResults() {
        return this.results;
    }

    public int getSelected() {
        return this.selected;
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        super.onCraftMatrixChanged(inventory);
        if (inventory != this.input) {
            return;
        }
        ItemStack current = this.input.getStackInSlot(0);
        // A different kind of block means a different list of choices, so the pick starts over
        if (current.isEmpty() || this.lastInput.isEmpty() || current.getItem() != this.lastInput.getItem()
                || current.getMetadata() != this.lastInput.getMetadata()) {
            this.selected = -1;
        }
        this.lastInput = current.copy();
        this.results = StonecutterRecipes.getResults(current);
        this.updateResult();
    }

    private void updateResult() {
        if (this.selected >= 0 && this.selected < this.results.size() && !this.input.getStackInSlot(0).isEmpty()) {
            this.result.setInventorySlotContents(0, this.results.get(this.selected).copy());
        } else {
            this.result.setInventorySlotContents(0, ItemStack.EMPTY);
        }
        this.detectAndSendChanges();
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int id) {
        if (id >= 0 && id < this.results.size()) {
            this.selected = id;
            this.updateResult();
            return true;
        }
        return false;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.sentSelected != this.selected) {
            this.sentSelected = this.selected;
            for (IContainerListener listener : this.listeners) {
                listener.sendWindowProperty(this, 0, this.selected);
            }
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, this.selected);
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id == 0) {
            this.selected = data;
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!this.world.isRemote) {
            this.clearContainer(player, this.world, this.input);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.world.getBlockState(this.pos).getBlock() instanceof BlockStonecutter
                && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            copy = stack.copy();

            if (index == 1) {
                if (!this.mergeItemStack(stack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(stack, copy);
            } else if (index == 0) {
                if (!this.mergeItemStack(stack, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!StonecutterRecipes.getResults(stack).isEmpty()) {
                if (!this.mergeItemStack(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 29) {
                if (!this.mergeItemStack(stack, 29, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(stack, 2, 29, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            }
            slot.onSlotChanged();
            if (stack.getCount() == copy.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return copy;
    }


}
