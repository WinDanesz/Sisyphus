package com.windanesz.sisyphus.client.render;

import com.google.common.collect.ImmutableList;
import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.item.ArmorTrims;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The armor models are wrapped so that a trimmed piece gets one more layer, without touching the vanilla model files.
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = SisyphusBackport.MODID)
public final class TrimmedArmorModels {

    private TrimmedArmorModels() {
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event) {
        if (!ArmorTrims.isEnabled()) {
            return;
        }
        for (ResourceLocation sprite : ArmorTrimTextures.itemSprites()) {
            event.getMap().registerSprite(sprite);
        }
    }

    @SubscribeEvent
    public static void onBake(ModelBakeEvent event) {
        if (!ArmorTrims.isEnabled()) {
            return;
        }
        for (Item item : ArmorTrims.trimmableArmor()) {
            ModelResourceLocation location = new ModelResourceLocation(item.getRegistryName(), "inventory");
            IBakedModel model = event.getModelRegistry().getObject(location);
            if (model != null) {
                event.getModelRegistry().putObject(location, new Wrapped(model));
            }
        }
    }

    private static final class Wrapped extends BakedModelWrapper<IBakedModel> {

        private final ItemOverrideList overrides;

        Wrapped(IBakedModel original) {
            super(original);
            this.overrides = new Overrides(original);
        }

        @Override
        public ItemOverrideList getOverrides() {
            return this.overrides;
        }
    }

    private static final class Overrides extends ItemOverrideList {

        private final IBakedModel original;
        private final Map<Pair<IBakedModel, ResourceLocation>, IBakedModel> trimmed = new ConcurrentHashMap<>();

        Overrides(IBakedModel original) {
            super(Collections.emptyList());
            this.original = original;
        }

        @Override
        public IBakedModel handleItemState(IBakedModel model, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            IBakedModel resolved = this.original.getOverrides().handleItemState(this.original, stack, world, entity);
            ArmorTrims.Trim trim = ArmorTrims.get(stack);
            if (trim == null || !(stack.getItem() instanceof ItemArmor)) {
                return resolved;
            }
            ItemArmor armor = (ItemArmor) stack.getItem();
            ResourceLocation sprite = ArmorTrimTextures.itemSprite(ArmorTrims.pieceOf(armor.armorType), trim.palette(armor));
            return this.trimmed.computeIfAbsent(Pair.of(resolved, sprite), key -> new Trimmed(key.getLeft(), key.getRight()));
        }
    }

    private static final class Trimmed extends BakedModelWrapper<IBakedModel> {

        private final List<BakedQuad> quads;

        Trimmed(IBakedModel original, ResourceLocation sprite) {
            super(original);
            TextureAtlasSprite atlasSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(sprite.toString());
            // no tint, so that the color of leather armor is not put on the trim
            ImmutableList<BakedQuad> layer = ItemLayerModel.getQuadsForSprite(-1, atlasSprite, DefaultVertexFormats.ITEM, Optional.empty());
            List<BakedQuad> all = new ArrayList<>(original.getQuads(null, null, 0L));
            all.addAll(layer);
            this.quads = all;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return side == null ? this.quads : this.originalModel.getQuads(state, side, rand);
        }

        @Override
        public ItemOverrideList getOverrides() {
            return ItemOverrideList.NONE;
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type) {
            return Pair.of(this, this.originalModel.handlePerspective(type).getRight());
        }
    }
}
