package com.windanesz.sisyphus.client.render;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.util.PottedPlants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.*;

@SideOnly(Side.CLIENT)
public final class RenderPottedPlant {

    private static final double EARTH = 4.0D / 16.0D;
    private static final float SCALE = 0.75F;

    private static final Set<Block> BROKEN = Collections.synchronizedSet(new HashSet<>());

    private RenderPottedPlant() {
    }

    public static void render(TileEntityFlowerPot pot, double x, double y, double z) {
        if (!pot.hasWorld()) {
            return;
        }
        IBlockState plant = PottedPlants.plantOf(pot.getFlowerPotItem(), pot.getFlowerPotData());
        if (plant == null || BROKEN.contains(plant.getBlock())) {
            return;
        }
        World world = pot.getWorld();
        BlockPos pos = pot.getPos();
        IBlockState potState = world.getBlockState(pos);
        // The plants of the game have potted models of their own in the block model of the pot
        if (!(potState.getBlock() instanceof BlockFlowerPot)
                || potState.getActualState(world, pos).getValue(BlockFlowerPot.CONTENTS) != BlockFlowerPot.EnumFlowerType.EMPTY) {
            return;
        }

        List<BakedQuad> quads = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        try {
            Minecraft mc = Minecraft.getMinecraft();
            IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(plant);
            BlockColors blockColors = mc.getBlockColors();
            long rand = MathHelper.getPositionRandom(pos);
            for (EnumFacing side : EnumFacing.values()) {
                quads.addAll(model.getQuads(plant, side, rand));
            }
            quads.addAll(model.getQuads(plant, null, rand));
            for (BakedQuad quad : quads) {
                int color = quad.hasTintIndex() ? blockColors.colorMultiplier(plant, world, pos, quad.getTintIndex()) : -1;
                colors.add(shade(color, quad));
            }
        } catch (RuntimeException e) {
            BROKEN.add(plant.getBlock());
            SisyphusBackport.logger.warn("Could not draw {} in a flower pot, it is left out from now on.", plant.getBlock().getRegistryName(), e);
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        ITextureObject blocks = mc.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        // Plants are drawn without mipmaps in the world, so their leaves do not fade away from a distance
        blocks.setBlurMipmap(false, false);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y + EARTH, z + 0.5D);
        GlStateManager.scale(SCALE, SCALE, SCALE);
        GlStateManager.translate(-0.5D, 0.0D, -0.5D);
        // The quads are shaded by their faces below, as in the world, and not by the light of entities
        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
        for (int i = 0; i < quads.size(); i++) {
            LightUtil.renderQuadColor(buffer, quads.get(i), colors.get(i));
        }
        Tessellator.getInstance().draw();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        blocks.restoreLastBlurMipmap();
    }

    private static int shade(int color, BakedQuad quad) {
        float light = quad.shouldApplyDiffuseLighting() ? LightUtil.diffuseLight(quad.getFace()) : 1.0F;
        int r = (int) (((color >> 16) & 0xFF) * light);
        int g = (int) (((color >> 8) & 0xFF) * light);
        int b = (int) ((color & 0xFF) * light);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }
}
