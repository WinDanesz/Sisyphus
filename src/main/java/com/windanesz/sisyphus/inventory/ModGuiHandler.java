package com.windanesz.sisyphus.inventory;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.gui.*;
import com.windanesz.sisyphus.tileentity.TileEntityFurnaceVariant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ModGuiHandler implements IGuiHandler {

    public static final int BLAST_FURNACE = 0;
    public static final int SMOKER = 1;
    public static final int GRINDSTONE = 2;
    public static final int STONECUTTER = 3;
    public static final int LOOM = 4;
    public static final int CARTOGRAPHY_TABLE = 5;
    public static final int LECTERN = 6;
    public static final int SMITHING_TABLE = 7;

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (id == GRINDSTONE) {
            return new ContainerGrindstone(player.inventory, world, pos);
        }
        if (id == STONECUTTER) {
            return new ContainerStonecutter(player.inventory, world, pos);
        }
        if (id == LOOM) {
            return new ContainerLoom(player.inventory, world, pos);
        }
        if (id == CARTOGRAPHY_TABLE) {
            return new ContainerCartography(player.inventory, world, pos);
        }
        if (id == LECTERN) {
            return new ContainerLectern(world, pos);
        }
        if (id == SMITHING_TABLE) {
            return new ContainerSmithing(player.inventory, world, pos);
        }
        TileEntity tile = world.getTileEntity(pos);
        if ((id == BLAST_FURNACE || id == SMOKER) && tile instanceof TileEntityFurnaceVariant) {
            return new ContainerFurnace(player.inventory, (TileEntityFurnaceVariant) tile);
        }
        return null;
    }

    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (id == GRINDSTONE) {
            return new GuiGrindstone(player.inventory, world, pos);
        }
        if (id == STONECUTTER) {
            return new GuiStonecutter(player.inventory, world, pos);
        }
        if (id == LOOM) {
            return new GuiLoom(player.inventory, world, pos);
        }
        if (id == CARTOGRAPHY_TABLE) {
            return new GuiCartography(player.inventory, world, pos);
        }
        if (id == LECTERN) {
            return new GuiLectern(world, pos);
        }
        if (id == SMITHING_TABLE) {
            return new GuiSmithing(player.inventory, world, pos);
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityFurnaceVariant) {
            String name = id == BLAST_FURNACE ? "blast_furnace" : "smoker";
            return new GuiFurnaceVariant(player.inventory, (TileEntityFurnaceVariant) tile,
                    new ResourceLocation(SisyphusBackport.MODID, "textures/gui/container/" + name + ".png"));
        }
        return null;
    }
}
