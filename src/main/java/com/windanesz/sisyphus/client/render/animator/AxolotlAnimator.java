package com.windanesz.sisyphus.client.render.animator;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.living.EntityAxolotl;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.pose.AzPoseModifierRegistry;
import mod.azure.azurelib.animation.pose.AzPoseModifiers;
import net.minecraft.util.ResourceLocation;

public final class AxolotlAnimator extends AzEntityAnimator<EntityAxolotl> {

    private static final ResourceLocation ANIMATIONS = new ResourceLocation(SisyphusBackport.MODID, "animations/entity/axolotl.animation.json");

    private String playing = "";

    public AxolotlAnimator() {
        addUpdateHook((entity, partialTicks) -> update(entity));
    }

    static String state(EntityAxolotl axolotl) {
        if (axolotl.isPlayingDead()) {
            return "play_dead";
        }
        boolean moving = axolotl.isMovingForAnimation();
        if (axolotl.isInWater()) {
            return moving ? "swim" : "hover";
        }
        if (axolotl.onGround) {
            return moving ? "crawl" : "lay";
        }
        return "air";
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<EntityAxolotl> container) {
        container.add(AzAnimationController.<EntityAxolotl>builder(this, "pose").setTransitionLength(10).build());
    }

    @Override
    protected void registerPoseModifiers(AzPoseModifierRegistry<EntityAxolotl> modifiers) {
        modifiers.add("pitch", AzPoseModifiers.<EntityAxolotl>headLook("body").limits(0.0F, 85.0F).invertPitch()).priority(5);
    }

    private void update(EntityAxolotl axolotl) {
        String wanted = state(axolotl);
        if (wanted.equals(this.playing)) {
            return;
        }
        AzCommand.create("pose", wanted, AzPlayBehaviors.LOOP).sendForEntity(axolotl);
        context().poseLayer().fadeWeight("pitch", wanted.equals("swim") ? 1.0D : 0.0D, 10.0D);
        this.playing = wanted;
    }

    @Override
    public ResourceLocation getAnimationLocation(EntityAxolotl axolotl) {
        return ANIMATIONS;
    }
}
