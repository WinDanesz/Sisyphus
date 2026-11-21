package com.windanesz.sisyphus.inventory;

import com.windanesz.sisyphus.block.BlockGrindstone;
import com.windanesz.sisyphus.registry.ModSounds;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.LinkedHashMap;
import java.util.Map;

public class ContainerGrindstone extends Container {

    private final World world;
    private final BlockPos pos;
    private final IInventory result = new InventoryBasic("Grindstone result", false, 1);
    public ContainerGrindstone(InventoryPlayer playerInventory, World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;

        this.addSlotToContainer(new Slot(this.input, 0, 49, 19) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return canGrind(stack);
            }
        });
        this.addSlotToContainer(new Slot(this.input, 1, 49, 40) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return canGrind(stack);
            }
        });
        this.addSlotToContainer(new Slot(this.result, 0, 129, 34) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public ItemStack onTake(EntityPlayer player, ItemStack stack) {
                if (!world.isRemote) {
                    int experience = getExperience();
                    while (experience > 0) {
                        int amount = EntityXPOrb.getXPSplit(experience);
                        experience -= amount;
                        world.spawnEntity(new EntityXPOrb(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, amount));
                    }
                    world.playSound(null, pos, ModSounds.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
                input.setInventorySlotContents(0, ItemStack.EMPTY);
                input.setInventorySlotContents(1, ItemStack.EMPTY);
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
    }    private final IInventory input = new InventoryBasic("Grindstone", false, 2) {
        @Override
        public void markDirty() {
            super.markDirty();
            ContainerGrindstone.this.onCraftMatrixChanged(this);
        }
    };

    private static boolean canGrind(ItemStack stack) {
        return stack.isItemStackDamageable() || stack.getItem() == Items.ENCHANTED_BOOK || stack.isItemEnchanted();
    }

    private static Map<Enchantment, Integer> enchantmentsOf(ItemStack stack) {
        return EnchantmentHelper.getEnchantments(stack);
    }

    private int getExperience() {
        int total = 0;
        for (int i = 0; i < 2; i++) {
            for (Map.Entry<Enchantment, Integer> entry : enchantmentsOf(this.input.getStackInSlot(i)).entrySet()) {
                if (!entry.getKey().isCurse()) {
                    total += entry.getKey().getMinEnchantability(entry.getValue());
                }
            }
        }
        if (total <= 0) {
            return 0;
        }
        int half = (int) Math.ceil(total / 2.0D);
        return half + this.world.rand.nextInt(half);
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        super.onCraftMatrixChanged(inventory);
        if (inventory == this.input) {
            this.updateResult();
        }
    }

    private void updateResult() {
        ItemStack first = this.input.getStackInSlot(0);
        ItemStack second = this.input.getStackInSlot(1);
        this.result.setInventorySlotContents(0, ItemStack.EMPTY);

        if (first.isEmpty() && second.isEmpty()) {
            return;
        }
        if (first.getCount() > 1 || second.getCount() > 1) {
            return;
        }

        boolean both = !first.isEmpty() && !second.isEmpty();
        if (both && first.getItem() != second.getItem()) {
            return;
        }

        ItemStack out = (first.isEmpty() ? second : first).copy();
        boolean repaired = false;
        if (both && out.isItemStackDamageable()) {
            // Combining two items repairs the result by what is left in both, plus a bonus of 5 percent
            int max = out.getMaxDamage();
            int left = (max - first.getItemDamage()) + (max - second.getItemDamage()) + max * 5 / 100;
            int damage = Math.max(max - left, 0);
            repaired = damage != out.getItemDamage();
            out.setItemDamage(damage);
        }

        // Strip everything except curses
        Map<Enchantment, Integer> kept = new LinkedHashMap<>();
        boolean removed = false;
        for (Map.Entry<Enchantment, Integer> entry : enchantmentsOf(out).entrySet()) {
            if (entry.getKey().isCurse()) {
                kept.put(entry.getKey(), entry.getValue());
            } else {
                removed = true;
            }
        }

        if (!removed && !repaired) {
            return;
        }

        if (out.getItem() == Items.ENCHANTED_BOOK) {
            ItemStack book = new ItemStack(Items.BOOK);
            if (!kept.isEmpty()) {
                book = new ItemStack(Items.ENCHANTED_BOOK);
                for (Map.Entry<Enchantment, Integer> entry : kept.entrySet()) {
                    ItemEnchantedBook.addEnchantment(book, new EnchantmentData(entry.getKey(), entry.getValue()));
                }
            }
            out = book;
        } else {
            EnchantmentHelper.setEnchantments(kept, out);
        }
        out.setRepairCost(0);
        this.result.setInventorySlotContents(0, out);
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
        return this.world.getBlockState(this.pos).getBlock() instanceof BlockGrindstone
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
            } else if (canGrind(stack)) {
                if (!this.mergeItemStack(stack, 0, 2, false)) {
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
            } else {
                slot.onSlotChanged();
            }
            if (stack.getCount() == copy.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return copy;
    }


}
