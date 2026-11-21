package com.windanesz.sisyphus.item;

import com.windanesz.sisyphus.tileentity.TileEntityDecoratedPot;
import com.windanesz.sisyphus.util.AdvancementHelper;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class ItemDecoratedPot extends ItemBlock {

    public ItemDecoratedPot(Block block) {
        super(block);
        this.setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        Item[] decorations = TileEntityDecoratedPot.decorationsOf(stack);
        if (Arrays.equals(decorations, TileEntityDecoratedPot.plain())) {
            return;
        }
        tooltip.add("");
        // back, left, right, front, listed as front, left, right, back
        for (int i : new int[]{3, 1, 2, 0}) {
            tooltip.add(TextFormatting.GRAY + new ItemStack(decorations[i]).getDisplayName());
        }
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player) {
        if (player instanceof EntityPlayerMP && TileEntityDecoratedPot.isAllSherds(TileEntityDecoratedPot.decorationsOf(stack))) {
            AdvancementHelper.grant((EntityPlayerMP) player, "careful_restoration");
        }
    }
}
