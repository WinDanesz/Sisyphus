package com.windanesz.sisyphus.client.gui;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.inventory.ContainerLoom;
import com.windanesz.sisyphus.item.ItemFlowerDye;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

public class GuiLoom extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(SisyphusBackport.MODID, "textures/gui/container/loom.png");
    private static final int COLUMNS = 4;
    private static final int ROWS = 4;
    private static final int BUTTON = 14;
    private static final int GRID_LEFT = 60;
    private static final int GRID_TOP = 13;

    private final ContainerLoom container;
    private int scrollRow;

    public GuiLoom(InventoryPlayer playerInventory, World world, BlockPos pos) {
        super(new ContainerLoom(playerInventory, world, pos));
        this.container = (ContainerLoom) this.inventorySlots;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("container.loom"), 8, 4, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    private int firstIndex(int size) {
        int maxRow = Math.max(0, (size + COLUMNS - 1) / COLUMNS - ROWS);
        this.scrollRow = Math.max(0, Math.min(this.scrollRow, maxRow));
        return this.scrollRow * COLUMNS;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        List<BannerPattern> patterns = this.container.getAvailable();
        int first = this.firstIndex(patterns.size());
        int last = Math.min(patterns.size(), first + COLUMNS * ROWS);

        for (int i = first; i < last; i++) {
            int x = this.guiLeft + GRID_LEFT + ((i - first) % COLUMNS) * BUTTON;
            int y = this.guiTop + GRID_TOP + ((i - first) / COLUMNS) * BUTTON;
            int v = this.ySize;
            if (i == this.container.getSelected()) {
                v += BUTTON;
            } else if (mouseX >= x && mouseY >= y && mouseX < x + BUTTON && mouseY < y + BUTTON) {
                v += BUTTON * 2;
            }
            this.mc.getTextureManager().bindTexture(TEXTURE);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(x, y, 0, v, BUTTON, BUTTON);
        }

        // A small banner with the pattern on it, in the colour of the dye that was put in
        EnumDyeColor color = ItemFlowerDye.colorOf(this.container.getSlot(1).getStack());
        if (color == null) {
            color = EnumDyeColor.WHITE;
        }
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = first; i < last; i++) {
            int x = this.guiLeft + GRID_LEFT + ((i - first) % COLUMNS) * BUTTON;
            int y = this.guiTop + GRID_TOP + ((i - first) / COLUMNS) * BUTTON;
            NBTTagList layers = new NBTTagList();
            NBTTagCompound layer = new NBTTagCompound();
            layer.setString("Pattern", patterns.get(i).getHashname());
            layer.setInteger("Color", color.getDyeDamage());
            layers.appendTag(layer);
            ItemStack preview = ItemBanner.makeBanner(color == EnumDyeColor.GRAY ? EnumDyeColor.WHITE : EnumDyeColor.GRAY, layers);
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 1.0F, y + 0.5F, 0.0F);
            GlStateManager.scale(0.8F, 0.8F, 1.0F);
            this.itemRender.renderItemIntoGUI(preview, 0, 0);
            GlStateManager.popMatrix();
        }
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int size = this.container.getAvailable().size();
        int first = this.firstIndex(size);
        int last = Math.min(size, first + COLUMNS * ROWS);
        for (int i = first; i < last; i++) {
            int x = this.guiLeft + GRID_LEFT + ((i - first) % COLUMNS) * BUTTON;
            int y = this.guiTop + GRID_TOP + ((i - first) / COLUMNS) * BUTTON;
            if (mouseX >= x && mouseY >= y && mouseX < x + BUTTON && mouseY < y + BUTTON) {
                this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                this.mc.playerController.sendEnchantPacket(this.container.windowId, i);
                return;
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            this.scrollRow += wheel > 0 ? -1 : 1;
        }
    }
}
