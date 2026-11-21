package com.windanesz.sisyphus.inventory;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.block.BlockSmithingTable;
import com.windanesz.sisyphus.item.ArmorTrims;
import com.windanesz.sisyphus.item.ItemSmithingTemplate;
import com.windanesz.sisyphus.recipe.SmithingRecipes;
import com.windanesz.sisyphus.registry.ModSounds;
import com.windanesz.sisyphus.tileentity.TileEntitySmithingTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ContainerSmithing extends Container {

    public static final int TEMPLATE = 0;
    public static final int BASE = 1;
    public static final int ADDITION = 2;
    public static final int RESULT = 3;
    private static final int INVENTORY = 4;
    private static final int HOTBAR = INVENTORY + 27;
    private static final int END = HOTBAR + 9;

    /**
     * In milliseconds.
     */
    private static final long CYCLE = 1500L;

    private static final String[] TEMPLATES = {"smithing_template_armor_trim", "smithing_template_netherite_upgrade"};
    private static final String[] ARMOR = {"helmet", "chestplate", "leggings", "boots"};
    private static final String[] EQUIPMENT = {"helmet", "sword", "chestplate", "pickaxe", "leggings", "axe", "boots", "hoe", "shovel"};
    private static final String[] MATERIALS = {"ingot", "redstone_dust", "lapis_lazuli", "quartz", "diamond", "emerald", "amethyst_shard"};
    private static final String[] INGOT = {"ingot"};

    private final World world;
    private final BlockPos pos;
    private final IInventory result = new InventoryBasic("Smithing result", false, 1);
    private final InventoryBasic input;
    private final boolean keptInTable;
    private SmithingRecipes.Result current = SmithingRecipes.Result.NONE;
    private final IInventoryChangedListener listener = changed -> this.onCraftMatrixChanged(changed);

    public ContainerSmithing(InventoryPlayer playerInventory, World world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
        this.input = inputOf(world, pos);
        this.keptInTable = !world.isRemote && world.getTileEntity(pos) instanceof TileEntitySmithingTable;
        this.input.addInventoryChangeListener(this.listener);

        this.addSlotToContainer(new GhostSlot(this.input, TEMPLATE, 8, 48));
        this.addSlotToContainer(new GhostSlot(this.input, BASE, 26, 48));
        this.addSlotToContainer(new GhostSlot(this.input, ADDITION, 44, 48));
        this.addSlotToContainer(new Slot(this.result, 0, 98, 48) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public ItemStack onTake(EntityPlayer player, ItemStack stack) {
                if (ContainerSmithing.this.current.usesTemplate) {
                    shrink(TEMPLATE);
                }
                shrink(BASE);
                shrink(ADDITION);
                if (!world.isRemote) {
                    world.playSound(null, pos, ModSounds.BLOCK_SMITHING_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
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
        // what was left in the table
        this.onCraftMatrixChanged(this.input);
    }

    /**
     * On the server, the tile entity's inventory, shared by everyone viewing it. The client, and a table without its tile entity, use a
     * screen inventory instead.
     */
    private static InventoryBasic inputOf(World world, BlockPos pos) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntitySmithingTable) {
                return ((TileEntitySmithingTable) tile).getInventory();
            }
        }
        return new InventoryBasic("Smithing", false, TileEntitySmithingTable.SIZE);
    }

    private static boolean isTemplate(ItemStack stack) {
        return stack.getItem() instanceof ItemSmithingTemplate
                || SmithingRecipes.getAll().stream().anyMatch(recipe -> recipe.template != null && recipe.template.apply(stack));
    }

    @Nullable
    private String[] outlines(int slot) {
        ItemStack template = this.input.getStackInSlot(TEMPLATE);
        if (slot == TEMPLATE) {
            return TEMPLATES;
        }
        if (template.isEmpty()) {
            return null;
        }
        boolean trim = ArmorTrims.patternOf(template) != null;
        if (slot == BASE) {
            return trim ? ARMOR : EQUIPMENT;
        }
        return trim ? MATERIALS : INGOT;
    }

    private void shrink(int slot) {
        ItemStack stack = this.input.getStackInSlot(slot);
        if (!stack.isEmpty()) {
            stack.shrink(1);
            this.input.setInventorySlotContents(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
        }
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        super.onCraftMatrixChanged(inventory);
        if (inventory == this.input) {
            this.current = SmithingRecipes.match(this.input.getStackInSlot(TEMPLATE), this.input.getStackInSlot(BASE), this.input.getStackInSlot(ADDITION));
            this.result.setInventorySlotContents(0, this.current.output.copy());
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        this.input.removeInventoryChangeListener(this.listener);
        if (!this.world.isRemote && !this.keptInTable) {
            this.clearContainer(player, this.world, this.input);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.world.getBlockState(this.pos).getBlock() instanceof BlockSmithingTable
                && player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    private int slotFor(ItemStack stack) {
        if (isTemplate(stack)) {
            return TEMPLATE;
        }
        if (ArmorTrims.materialOf(stack) != null || SmithingRecipes.getAll().stream().anyMatch(recipe -> recipe.addition.apply(stack))) {
            return ADDITION;
        }
        return BASE;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            copy = stack.copy();
            if (index == RESULT) {
                if (!this.mergeItemStack(stack, INVENTORY, END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(stack, copy);
            } else if (index < RESULT) {
                if (!this.mergeItemStack(stack, INVENTORY, END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                int target = slotFor(stack);
                if (!this.mergeItemStack(stack, target, target + 1, false)) {
                    if (index < HOTBAR) {
                        if (!this.mergeItemStack(stack, HOTBAR, END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.mergeItemStack(stack, INVENTORY, HOTBAR, false)) {
                        return ItemStack.EMPTY;
                    }
                }
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

    private class GhostSlot extends Slot {

        GhostSlot(IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return this.getSlotIndex() != TEMPLATE || isTemplate(stack);
        }

        @Nullable
        @SideOnly(Side.CLIENT)
        @Override
        public String getSlotTexture() {
            String[] names = outlines(this.getSlotIndex());
            if (names == null) {
                return null;
            }
            return SisyphusBackport.MODID + ":gui/slot_" + names[(int) (System.currentTimeMillis() / CYCLE % names.length)];
        }
    }
}
