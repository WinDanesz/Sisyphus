package com.windanesz.sisyphus.inventory;

import com.windanesz.sisyphus.block.BlockCartographyTable;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

public class ContainerCartography extends Container {

    private final World world;
    private final BlockPos pos;
    private final IInventory result = new InventoryBasic("Cartography result", false, 1);
    public ContainerCartography(InventoryPlayer playerInventory, World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;

        this.addSlotToContainer(new Slot(this.input, 0, 15, 15) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() == Items.FILLED_MAP;
            }
        });
        this.addSlotToContainer(new Slot(this.input, 1, 15, 52) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() == Items.PAPER || stack.getItem() == Items.MAP;
            }
        });
        this.addSlotToContainer(new Slot(this.result, 0, 143, 39) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public ItemStack onTake(EntityPlayer player, ItemStack stack) {
                boolean zoom = stack.hasTagCompound() && stack.getTagCompound().getBoolean("map_is_scaling");
                input.decrStackSize(0, 1);
                input.decrStackSize(1, 1);
                if (zoom) {
                    // Makes the new, bigger map out of the old one
                    stack.getItem().onCreated(stack, world, player);
                }
                if (!world.isRemote) {
                    world.playSound(null, pos, ModSounds.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, 1.0F);
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
    }    private final IInventory input = new InventoryBasic("Cartography", false, 2) {
        @Override
        public void markDirty() {
            super.markDirty();
            ContainerCartography.this.onCraftMatrixChanged(this);
        }
    };

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        super.onCraftMatrixChanged(inventory);
        if (inventory == this.input) {
            this.updateResult();
        }
    }

    private void updateResult() {
        ItemStack map = this.input.getStackInSlot(0);
        ItemStack extra = this.input.getStackInSlot(1);
        ItemStack out = ItemStack.EMPTY;

        if (!map.isEmpty() && !extra.isEmpty()) {
            if (extra.getItem() == Items.PAPER) {
                MapData data = Items.FILLED_MAP.getMapData(map, this.world);
                if (data != null && data.scale < 4) {
                    out = new ItemStack(Items.FILLED_MAP, 1, map.getMetadata());
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setBoolean("map_is_scaling", true);
                    out.setTagCompound(tag);
                    if (map.hasDisplayName()) {
                        out.setStackDisplayName(map.getDisplayName());
                    }
                }
            } else if (extra.getItem() == Items.MAP) {
                out = new ItemStack(Items.FILLED_MAP, 2, map.getMetadata());
                if (map.hasDisplayName()) {
                    out.setStackDisplayName(map.getDisplayName());
                }
            }
        }
        this.result.setInventorySlotContents(0, out);
        this.detectAndSendChanges();
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
        return this.world.getBlockState(this.pos).getBlock() instanceof BlockCartographyTable
                && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            copy = stack.copy();

            if (index == 2) {
                if (!this.mergeItemStack(stack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(stack, copy);
            } else if (index < 2) {
                if (!this.mergeItemStack(stack, 3, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.getItem() == Items.FILLED_MAP) {
                if (!this.mergeItemStack(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.getItem() == Items.PAPER || stack.getItem() == Items.MAP) {
                if (!this.mergeItemStack(stack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 30) {
                if (!this.mergeItemStack(stack, 30, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(stack, 3, 30, false)) {
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
