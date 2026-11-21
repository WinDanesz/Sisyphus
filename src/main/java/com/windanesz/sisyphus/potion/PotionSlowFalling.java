package com.windanesz.sisyphus.potion;

import com.windanesz.sisyphus.Settings;
import com.windanesz.sisyphus.SisyphusBackport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PotionSlowFalling extends Potion {

    private static final ResourceLocation ICON = new ResourceLocation(SisyphusBackport.MODID, "textures/mob_effect/slow_falling.png");

    private static final double GRAVITY_REFUND = (0.08D - 0.01D) * 0.98D;

    public PotionSlowFalling() {
        super(false, 0xF7F8E0);
        this.setRegistryName(SisyphusBackport.MODID, "slow_falling");
        this.setPotionName("effect." + SisyphusBackport.MODID + ":slow_falling");
    }

    // 1.12 only knows the icons of its own sprite sheet, so the icon is drawn separately below
    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasStatusIcon() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        mc.getTextureManager().bindTexture(ICON);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 7, 0, 0, 18, 18, 18, 18);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        mc.getTextureManager().bindTexture(ICON);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        Gui.drawModalRectWithCustomSizedTexture(x + 3, y + 3, 0, 0, 18, 18, 18, 18);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (!Settings.items.enableSlowFalling) {
            return;
        }
        // Flying, swimming and gliding have their own movement
        if (entity.isInWater() || entity.isInLava() || entity.isElytraFlying() || entity.isOnLadder()
                || (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isFlying)) {
            return;
        }
        if (entity.motionY < 0.0D) {
            // Vanilla applies its gravity after moving, so the refund is what the previous tick took off too much
            entity.motionY += GRAVITY_REFUND;
        }
        entity.fallDistance = 0.0F;
    }
}
