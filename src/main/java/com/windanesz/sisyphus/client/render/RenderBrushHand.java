package com.windanesz.sisyphus.client.render;

import com.windanesz.sisyphus.item.ItemBrush;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class RenderBrushHand {

    private RenderBrushHand() {
    }

    @SubscribeEvent
    public static void onRenderHand(RenderSpecificHandEvent event) {
        ItemStack stack = event.getItemStack();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null || !(stack.getItem() instanceof ItemBrush) || !player.isHandActive() || player.getActiveHand() != event.getHand()) {
            return;
        }
        event.setCanceled(true);
        EnumHandSide arm = event.getHand() == EnumHand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
        boolean right = arm == EnumHandSide.RIGHT;
        float partialTicks = event.getPartialTicks();

        GlStateManager.pushMatrix();
        // Where the item in this hand is, as the game puts it
        float side = right ? 1.0F : -1.0F;
        GlStateManager.translate(side * 0.56F, -0.52F + event.getEquipProgress() * -0.6F, -0.72F);
        // The stroke: a sweep every ten ticks
        float ticks = player.getItemInUseCount() % 10;
        float progress = 1.0F - (ticks - partialTicks + 1.0F) / 10.0F;
        float sweep = -15.0F + 75.0F * MathHelper.cos(progress * 2.0F * (float) Math.PI);
        if (right) {
            GlStateManager.translate(-0.25F, 0.22F, 0.35F);
            GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(sweep, 1.0F, 0.0F, 0.0F);
        } else {
            GlStateManager.translate(0.1F, 0.83F, 0.35F);
            GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(sweep, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(-0.3F, 0.22F, 0.35F);
        }
        Minecraft.getMinecraft().getItemRenderer().renderItemSide(player, stack,
                right ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !right);
        GlStateManager.popMatrix();
    }
}
