package com.windanesz.sisyphus.client.gui;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.inventory.ContainerSmithing;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiSmithing extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(SisyphusBackport.MODID, "textures/gui/container/smithing.png");

    /**
     * A yaw of 0 faces the screen.
     */
    private static final float ARMOR_STAND_YAW = 30.0F;

    private final EntityArmorStand armorStand;
    private ItemStack shown = ItemStack.EMPTY;

    public GuiSmithing(InventoryPlayer playerInventory, World world, BlockPos pos) {
        super(new ContainerSmithing(playerInventory, world, pos));
        this.armorStand = new EntityArmorStand(world);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("NoBasePlate", true);
        tag.setBoolean("ShowArms", true);
        this.armorStand.readEntityFromNBT(tag);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString(I18n.format("container.upgrade"), 44, 15, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int left = (this.width - this.xSize) / 2;
        int top = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(left, top, 0, 0, this.xSize, this.ySize);

        // A cross shows that what is in the table does not go together
        boolean hasInput = this.inventorySlots.getSlot(ContainerSmithing.BASE).getHasStack() && this.inventorySlots.getSlot(ContainerSmithing.ADDITION).getHasStack();
        if (hasInput && !this.inventorySlots.getSlot(ContainerSmithing.RESULT).getHasStack()) {
            this.drawTexturedModalRect(left + 65, top + 46, this.xSize, 0, 28, 21);
        }

        dress(this.inventorySlots.getSlot(ContainerSmithing.RESULT).getStack());
        drawArmorStand(left + 141, top + 75, 25);
    }

    private void dress(ItemStack result) {
        if (ItemStack.areItemStacksEqual(result, this.shown)) {
            return;
        }
        this.shown = result.copy();
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            this.armorStand.setItemStackToSlot(slot, ItemStack.EMPTY);
        }
        if (result.isEmpty()) {
            return;
        }
        EntityEquipmentSlot slot = result.getItem() instanceof ItemArmor ? ((ItemArmor) result.getItem()).armorType : EntityEquipmentSlot.MAINHAND;
        this.armorStand.setItemStackToSlot(slot, result.copy());
    }

    private void drawArmorStand(int x, int y, int scale) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 50.0F);
        GlStateManager.scale(-scale, scale, scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        // seen a little from above and turned to the side
        GlStateManager.rotate(25.0F, 1.0F, 0.0F, 0.0F);
        this.armorStand.renderYawOffset = ARMOR_STAND_YAW;
        this.armorStand.prevRenderYawOffset = ARMOR_STAND_YAW;
        this.armorStand.rotationYaw = ARMOR_STAND_YAW;
        this.armorStand.prevRotationYaw = ARMOR_STAND_YAW;
        this.armorStand.rotationYawHead = ARMOR_STAND_YAW;
        this.armorStand.prevRotationYawHead = ARMOR_STAND_YAW;
        RenderManager manager = this.mc.getRenderManager();
        manager.setPlayerViewY(180.0F);
        manager.setRenderShadow(false);
        manager.renderEntity(this.armorStand, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        manager.setRenderShadow(true);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
