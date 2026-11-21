package com.windanesz.sisyphus.inventory;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.block.BlockLoom;
import com.windanesz.sisyphus.item.ItemBannerPattern;
import com.windanesz.sisyphus.item.ItemFlowerDye;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class ContainerLoom extends Container {

    private final World world;
    private final BlockPos pos;
    private final IInventory result = new InventoryBasic("Loom result", false, 1);
    private int selected = -1;    private final IInventory input = new InventoryBasic("Loom", false, 3) {
        @Override
        public void markDirty() {
            super.markDirty();
            ContainerLoom.this.onCraftMatrixChanged(this);
        }
    };
    private int sentSelected = -2;
    public ContainerLoom(InventoryPlayer playerInventory, World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;

        this.addSlotToContainer(ghost(new Slot(this.input, 0, 13, 26) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof ItemBanner;
            }
        }, "banner"));
        this.addSlotToContainer(ghost(new Slot(this.input, 1, 33, 26) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return ItemFlowerDye.colorOf(stack) != null;
            }
        }, "dye"));
        this.addSlotToContainer(ghost(new Slot(this.input, 2, 23, 45) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return ItemBannerPattern.patternOf(stack) != null;
            }
        }, "pattern"));
        this.addSlotToContainer(new Slot(this.result, 0, 143, 57) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public ItemStack onTake(EntityPlayer player, ItemStack stack) {
                input.decrStackSize(0, 1);
                input.decrStackSize(1, 1);
                if (!world.isRemote) {
                    world.playSound(null, pos, ModSounds.UI_LOOM_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
                updateResult();
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

    private static Slot ghost(Slot slot, String item) {
        slot.setBackgroundName(SisyphusBackport.MODID + ":gui/slot_" + item);
        return slot;
    }

    /**
     * Worked out from the slots on both sides.
     */
    public static List<BannerPattern> getAvailable(ItemStack banner, ItemStack patternItem) {
        if (banner.isEmpty() || TileEntityBanner.getPatterns(banner) >= 6) {
            return Collections.emptyList();
        }
        BannerPattern special = ItemBannerPattern.patternOf(patternItem);
        if (special != null) {
            return Collections.singletonList(special);
        }
        return ItemBannerPattern.genericPatterns();
    }

    public static ItemStack withPattern(ItemStack banner, BannerPattern pattern, EnumDyeColor color) {
        ItemStack out = banner.copy();
        out.setCount(1);
        NBTTagCompound tag = out.getOrCreateSubCompound("BlockEntityTag");
        NBTTagList patterns = tag.getTagList("Patterns", 10);
        NBTTagCompound entry = new NBTTagCompound();
        entry.setString("Pattern", pattern.getHashname());
        entry.setInteger("Color", color.getDyeDamage());
        patterns.appendTag(entry);
        tag.setTag("Patterns", patterns);
        return out;
    }

    public int getSelected() {
        return this.selected;
    }

    public List<BannerPattern> getAvailable() {
        return getAvailable(this.input.getStackInSlot(0), this.input.getStackInSlot(2));
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        super.onCraftMatrixChanged(inventory);
        if (inventory != this.input) {
            return;
        }
        if (this.selected >= this.getAvailable().size()) {
            this.selected = -1;
        }
        this.updateResult();
    }

    private void updateResult() {
        List<BannerPattern> available = this.getAvailable();
        EnumDyeColor color = ItemFlowerDye.colorOf(this.input.getStackInSlot(1));
        if (this.selected >= 0 && this.selected < available.size() && color != null) {
            this.result.setInventorySlotContents(0, withPattern(this.input.getStackInSlot(0), available.get(this.selected), color));
        } else {
            this.result.setInventorySlotContents(0, ItemStack.EMPTY);
        }
        this.detectAndSendChanges();
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int id) {
        if (id >= 0 && id < this.getAvailable().size()) {
            this.selected = id;
            if (!this.world.isRemote) {
                this.world.playSound(null, this.pos, ModSounds.UI_LOOM_SELECT_PATTERN, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
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
        return this.world.getBlockState(this.pos).getBlock() instanceof BlockLoom
                && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            copy = stack.copy();

            if (index == 3) {
                if (!this.mergeItemStack(stack, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(stack, copy);
            } else if (index < 3) {
                if (!this.mergeItemStack(stack, 4, 40, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (stack.getItem() instanceof ItemBanner) {
                if (!this.mergeItemStack(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (ItemFlowerDye.colorOf(stack) != null) {
                if (!this.mergeItemStack(stack, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (ItemBannerPattern.patternOf(stack) != null) {
                if (!this.mergeItemStack(stack, 2, 3, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 31) {
                if (!this.mergeItemStack(stack, 31, 40, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(stack, 4, 31, false)) {
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
