package com.windanesz.sisyphus.client.render;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.block.BlockCandle;
import com.windanesz.sisyphus.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BakedQuadRetextured;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = SisyphusBackport.MODID)
public class MixedCandleModel extends BakedModelWrapper<IBakedModel> {

    private static final double[][][] CANDLES = {
            {{8, 8}},
            {{6, 8}, {10, 7}},
            {{8, 10}, {6, 8}, {9, 7}},
            {{7, 9}, {10, 9}, {6, 6}, {9, 6}}};

    private final int candles;
    private final boolean lit;
    private final Map<String, List<BakedQuad>> cache = new ConcurrentHashMap<>();

    private MixedCandleModel(IBakedModel original, int candles, boolean lit) {
        super(original);
        this.candles = candles;
        this.lit = lit;
    }

    @SubscribeEvent
    public static void wrapModels(ModelBakeEvent event) {
        IRegistry<ModelResourceLocation, IBakedModel> registry = event.getModelRegistry();
        for (String name : ModBlocks.candleNames()) {
            for (int candles = 1; candles <= 4; candles++) {
                for (boolean lit : new boolean[]{false, true}) {
                    ModelResourceLocation key = new ModelResourceLocation(new ResourceLocation(SisyphusBackport.MODID, name),
                            "candles=" + candles + ",lit=" + lit);
                    IBakedModel original = registry.getObject(key);
                    if (original != null) {
                        registry.putObject(key, new MixedCandleModel(original, candles, lit));
                    }
                }
            }
        }
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        List<BakedQuad> quads = super.getQuads(state, side, rand);
        if (!(state instanceof IExtendedBlockState)) {
            return quads;
        }
        String colors = ((IExtendedBlockState) state).getValue(BlockCandle.CANDLE_COLORS);
        if (colors == null || colors.isEmpty()) {
            return quads;
        }
        return this.cache.computeIfAbsent(colors + "|" + side, key -> this.recolor(quads, colors.split(",")));
    }

    private List<BakedQuad> recolor(List<BakedQuad> quads, String[] colors) {
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[colors.length];
        for (int i = 0; i < colors.length; i++) {
            sprites[i] = this.sprite(colors[i]);
        }
        List<BakedQuad> recolored = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            int candle = this.candleOf(quad);
            TextureAtlasSprite sprite = candle < sprites.length ? sprites[candle] : null;
            recolored.add(sprite == null || sprite == quad.getSprite() ? quad : new BakedQuadRetextured(quad, sprite));
        }
        return recolored;
    }

    @Nullable
    private TextureAtlasSprite sprite(String name) {
        Block block = Block.getBlockFromName(name);
        ResourceLocation id = block instanceof BlockCandle ? block.getRegistryName() : null;
        if (id == null) {
            return null;
        }
        return Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(id.getNamespace() + ":blocks/" + id.getPath() + (this.lit ? "_lit" : ""));
    }

    private int candleOf(BakedQuad quad) {
        VertexFormat format = quad.getFormat();
        int[] data = quad.getVertexData();
        int stride = format.getIntegerSize();
        int offset = 0;
        for (int i = 0; i < format.getElementCount(); i++) {
            if (format.getElement(i).isPositionElement()) {
                offset = format.getOffset(i) / 4;
                break;
            }
        }
        double x = 0, z = 0;
        for (int i = 0; i < 4; i++) {
            x += Float.intBitsToFloat(data[i * stride + offset]);
            z += Float.intBitsToFloat(data[i * stride + offset + 2]);
        }
        x = x * 4.0D; // The average, in pixels
        z = z * 4.0D;
        double[][] wicks = CANDLES[this.candles - 1];
        int nearest = 0;
        double best = Double.MAX_VALUE;
        for (int i = 0; i < wicks.length; i++) {
            double dx = wicks[i][0] - x, dz = wicks[i][1] - z;
            if (dx * dx + dz * dz < best) {
                best = dx * dx + dz * dz;
                nearest = i;
            }
        }
        return nearest;
    }
}
