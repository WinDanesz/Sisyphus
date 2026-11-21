package com.windanesz.sisyphus.client.render.entity;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.client.model.ModelBambooRaft;
import net.minecraft.client.renderer.entity.RenderBoat;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.ResourceLocation;

public class RenderBambooRaft extends RenderBoat {

    private static final ResourceLocation RAFT = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/boat/bamboo.png");
    private static final ResourceLocation CHEST_RAFT = new ResourceLocation(SisyphusBackport.MODID, "textures/entity/chest_boat/bamboo.png");

    private final ResourceLocation texture;

    public RenderBambooRaft(RenderManager manager, boolean withChest) {
        super(manager);
        this.modelBoat = new ModelBambooRaft(withChest);
        this.texture = withChest ? CHEST_RAFT : RAFT;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBoat entity) {
        return this.texture;
    }

    /**
     * A raft floats on the water, so there is no water inside it to hide.
     */
    @Override
    public boolean isMultipass() {
        return false;
    }
}
