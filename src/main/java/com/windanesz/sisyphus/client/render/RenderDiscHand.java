package com.windanesz.sisyphus.client.render;

import com.windanesz.sisyphus.item.ItemThrowingDisc;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public final class RenderDiscHand {

    private RenderDiscHand() {
    }

    @SubscribeEvent
    public static void onRenderHand(RenderSpecificHandEvent event) {
        ItemStack stack = event.getItemStack();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null || !(stack.getItem() instanceof ItemThrowingDisc) || player.getActiveHand() != event.getHand()) {
            return;
        }
        float partialTicks = event.getPartialTicks();
        float windUp = ItemThrowingDisc.getWindUp(player, partialTicks);
        if (windUp < 0.0F) {
            return;
        }
        event.setCanceled(true);
        EnumHandSide arm = event.getHand() == EnumHand.MAIN_HAND ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
        boolean right = arm == EnumHandSide.RIGHT;
        float side = right ? 1.0F : -1.0F;
        // Fast at first, then slowing as the arm is drawn back as far as it goes
        float drawn = 1.0F - (1.0F - windUp) * (1.0F - windUp);

        GlStateManager.pushMatrix();
        // Where the item in this hand is, as the game puts it
        GlStateManager.translate(side * 0.56F, -0.52F + event.getEquipProgress() * -0.6F, -0.72F);
        // Across the body, a little lower and nearer
        GlStateManager.translate(side * -0.42F * drawn, -0.1F * drawn, 0.15F * drawn);
        // Rolled a quarter turn about its own middle, so that it lies flat. A held item stands on its edge facing sideways, and its middle is
        // where the first person view of item/generated puts it
        float middleX = side * 1.13F / 16.0F;
        float middleY = 3.2F / 16.0F;
        float middleZ = 1.13F / 16.0F;
        GlStateManager.translate(middleX, middleY, middleZ);
        GlStateManager.rotate(side * 90.0F * drawn, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(-middleX, -middleY, -middleZ);
        Minecraft.getMinecraft().getItemRenderer().renderItemSide(player, stack,
                right ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !right);
        GlStateManager.popMatrix();
    }
}
