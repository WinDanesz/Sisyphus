package com.windanesz.sisyphus.client.gui;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.inventory.ContainerGrindstone;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiGrindstone extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(SisyphusBackport.MODID, "textures/gui/container/grindstone.png");

    public GuiGrindstone(InventoryPlayer playerInventory, World world, BlockPos pos) {
        super(new ContainerGrindstone(playerInventory, world, pos));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("container.grindstone_title"), 8, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int left = (this.width - this.xSize) / 2;
        int top = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(left, top, 0, 0, this.xSize, this.ySize);

        // A cross shows that the items in the slots cannot be ground to anything
        boolean hasInput = this.inventorySlots.getSlot(0).getHasStack() || this.inventorySlots.getSlot(1).getHasStack();
        if (hasInput && !this.inventorySlots.getSlot(2).getHasStack()) {
            this.drawTexturedModalRect(left + 92, top + 31, this.xSize, 0, 28, 21);
        }
    }
}
