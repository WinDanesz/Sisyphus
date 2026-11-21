package com.windanesz.sisyphus.client.render;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.worldgen.ArchaeologyLoot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class DecoratedPotModel {

    private static final float PI = (float) Math.PI;
    private static final float SCALE = 1.0F / 16.0F;

    private static final ResourceLocation BASE = texture("decorated_pot_base");
    private static final ResourceLocation SIDE = texture("decorated_pot_side");
    private static final Map<String, ResourceLocation> PATTERNS = new HashMap<>();

    private final ModelRenderer neck;
    private final ModelRenderer top;
    private final ModelRenderer bottom;
    private final ModelRenderer[] sides = new ModelRenderer[4];

    public DecoratedPotModel() {
        ModelBase base = new ModelBase() {
        };
        base.textureWidth = 32;
        base.textureHeight = 32;
        this.neck = new ModelRenderer(base);
        this.neck.setTextureOffset(0, 0).addBox(4.0F, 17.0F, 4.0F, 8, 3, 8, -0.1F);
        this.neck.setTextureOffset(0, 5).addBox(5.0F, 20.0F, 5.0F, 6, 1, 6, 0.2F);
        this.neck.setRotationPoint(0.0F, 37.0F, 16.0F);
        this.neck.rotateAngleX = PI;
        this.top = new ModelRenderer(base, -14, 13);
        this.top.addBox(0.0F, 0.0F, 0.0F, 14, 0, 14);
        this.top.setRotationPoint(1.0F, 16.0F, 1.0F);
        this.bottom = new ModelRenderer(base, -14, 13);
        this.bottom.addBox(0.0F, 0.0F, 0.0F, 14, 0, 14);
        this.bottom.setRotationPoint(1.0F, 0.0F, 1.0F);

        ModelBase sideBase = new ModelBase() {
        };
        sideBase.textureWidth = 16;
        sideBase.textureHeight = 16;
        float[][] poses = {
                {15.0F, 16.0F, 1.0F, 0.0F, 0.0F, PI}, // back
                {1.0F, 16.0F, 1.0F, 0.0F, -PI / 2.0F, PI}, // left
                {15.0F, 16.0F, 15.0F, 0.0F, PI / 2.0F, PI}, // right
                {1.0F, 16.0F, 15.0F, PI, 0.0F, 0.0F}}; // front
        for (int i = 0; i < this.sides.length; i++) {
            ModelRenderer side = new ModelRenderer(sideBase, 1, 0);
            side.addBox(0.0F, 0.0F, 0.0F, 14, 16, 0);
            side.setRotationPoint(poses[i][0], poses[i][1], poses[i][2]);
            side.rotateAngleX = poses[i][3];
            side.rotateAngleY = poses[i][4];
            side.rotateAngleZ = poses[i][5];
            this.sides[i] = side;
        }
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(SisyphusBackport.MODID, "textures/entity/decorated_pot/" + name + ".png");
    }

    public static ResourceLocation sideTexture(Item item) {
        String pattern = ArchaeologyLoot.patternOf(item);
        return pattern == null ? SIDE : PATTERNS.computeIfAbsent(pattern, name -> texture(name + "_pottery_pattern"));
    }

    /**
     * The sides in order: back, left, right, front.
     */
    public void render(Item[] decorations) {
        Minecraft mc = Minecraft.getMinecraft();
        // The planes are seen from one side only: without culling their backs would show through each other
        GlStateManager.enableCull();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(BASE);
        this.neck.render(SCALE);
        this.top.render(SCALE);
        this.bottom.render(SCALE);
        for (int i = 0; i < this.sides.length; i++) {
            mc.getTextureManager().bindTexture(sideTexture(decorations[i]));
            this.sides[i].render(SCALE);
        }
    }
}
