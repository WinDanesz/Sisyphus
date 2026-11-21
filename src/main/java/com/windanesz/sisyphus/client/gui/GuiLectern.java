package com.windanesz.sisyphus.client.gui;

import com.windanesz.sisyphus.inventory.ContainerLectern;
import com.windanesz.sisyphus.tileentity.TileEntityLectern;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

import java.io.IOException;

public class GuiLectern extends GuiContainer {

    private static final ResourceLocation BOOK = new ResourceLocation("textures/gui/book.png");
    private static final ResourceLocation MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");

    private static final int MAP_SIZE = 180;

    private final World world;
    private final BlockPos pos;

    public GuiLectern(World world, BlockPos pos) {
        super(new ContainerLectern(world, pos));
        this.world = world;
        this.pos = pos;
        this.xSize = 192;
        this.ySize = 192;
    }

    private TileEntityLectern lectern() {
        TileEntity tile = this.world.getTileEntity(this.pos);
        return tile instanceof TileEntityLectern ? (TileEntityLectern) tile : null;
    }

    @Override
    public void initGui() {
        super.initGui();
        TileEntityLectern lectern = this.lectern();
        String take = lectern != null && lectern.hasMap() ? "lectern.take_map" : "lectern.take_book";
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, 196, 98, 20, I18n.format(take)));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 2, 196, 98, 20, I18n.format("gui.done")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, ContainerLectern.TAKE_BOOK);
        } else if (button.id == 1) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // The book is closed by the server when it is taken, which is noticed here
        TileEntityLectern lectern = this.lectern();
        if (lectern == null || !lectern.hasBook()) {
            this.mc.displayGuiScreen(null);
            return;
        }
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        TileEntityLectern lectern = this.lectern();
        if (lectern == null) {
            return;
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        if (lectern.hasMap()) {
            this.drawMap(lectern);
            return;
        }
        this.mc.getTextureManager().bindTexture(BOOK);
        int left = this.guiLeft;
        int top = this.guiTop;
        this.drawTexturedModalRect(left, top, 0, 0, this.xSize, this.ySize);

        int page = lectern.getPage();
        int pages = lectern.getPageCount();

        String title = I18n.format("book.pageIndicator", page + 1, Math.max(pages, 1));
        this.fontRenderer.drawString(title, left - this.fontRenderer.getStringWidth(title) + this.xSize - 44, top + 18, 0);
        this.fontRenderer.drawSplitString(TileEntityLectern.getPageText(lectern.getBook(), page), left + 36, top + 34, 116, 0);

        // The arrows for turning the page, which light up under the mouse
        if (page < pages - 1) {
            boolean hover = mouseX >= left + 116 && mouseX < left + 139 && mouseY >= top + 154 && mouseY < top + 167;
            this.drawTexturedModalRect(left + 116, top + 154, 23, hover ? 205 : 192, 23, 13);
        }
        if (page > 0) {
            boolean hover = mouseX >= left + 43 && mouseX < left + 66 && mouseY >= top + 154 && mouseY < top + 167;
            this.drawTexturedModalRect(left + 43, top + 154, 3, hover ? 205 : 192, 23, 13);
        }
    }

    private void drawMap(TileEntityLectern lectern) {
        int left = (this.width - MAP_SIZE) / 2;
        int top = 196 - 4 - MAP_SIZE;
        this.mc.getTextureManager().bindTexture(MAP_BACKGROUND);
        Gui.drawModalRectWithCustomSizedTexture(left, top, 0.0F, 0.0F, MAP_SIZE, MAP_SIZE, MAP_SIZE, MAP_SIZE);

        MapData data = Items.FILLED_MAP.getMapData(lectern.getBook(), this.world);
        if (data == null) {
            return;
        }
        // The paper is 142 wide around the 128 of the map, as in the hand
        float scale = MAP_SIZE / 142.0F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(left + 7.0F * scale, top + 7.0F * scale, 1.0F);
        GlStateManager.scale(scale, scale, 1.0F);
        this.mc.entityRenderer.getMapItemRenderer().renderMap(data, true);
        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        TileEntityLectern lectern = this.lectern();
        if (lectern == null || lectern.hasMap() || mouseButton != 0) {
            return;
        }
        int left = this.guiLeft;
        int top = this.guiTop;
        int page = lectern.getPage();
        if (page < lectern.getPageCount() - 1 && mouseX >= left + 116 && mouseX < left + 139 && mouseY >= top + 154 && mouseY < top + 167) {
            this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, page + 1);
        } else if (page > 0 && mouseX >= left + 43 && mouseX < left + 66 && mouseY >= top + 154 && mouseY < top + 167) {
            this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, page - 1);
        }
    }
}
