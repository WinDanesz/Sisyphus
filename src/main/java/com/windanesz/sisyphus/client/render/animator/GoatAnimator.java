package com.windanesz.sisyphus.client.render.animator;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.living.EntityGoat;
import mod.azure.azurelib.animation.blend.AzBlendSpace1D;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.pose.AzPoseModifierRegistry;
import mod.azure.azurelib.animation.pose.AzPoseModifiers;
import net.minecraft.util.ResourceLocation;

public final class GoatAnimator extends AzEntityAnimator<EntityGoat> {

    private static final ResourceLocation ANIMATIONS = new ResourceLocation(SisyphusBackport.MODID, "animations/entity/goat.animation.json");

    private AzAnimationController<EntityGoat> action;
    private boolean lowered;

    public GoatAnimator() {
        addUpdateHook((entity, partialTicks) -> update(entity));
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<EntityGoat> container) {
        AzBlendSpace1D.<EntityGoat>builder(this, "locomotion")
                .input((entity, partialTicks) -> Gait.speed(entity))
                .sample(0.0D, "idle")
                .sample(2.0D, "walk")
                .sample(5.0D, "run")
                .smoothing(5.0D)
                .build()
                .addTo(container);
        action = AzAnimationController.<EntityGoat>builder(this, "action").setTransitionLength(2).setWeight(0.0D).build();
        container.add(action);
    }

    @Override
    protected void registerPoseModifiers(AzPoseModifierRegistry<EntityGoat> modifiers) {
        modifiers.add("look", AzPoseModifiers.<EntityGoat>headLook("head").limits(45.0F, 30.0F).invertYaw().invertPitch()).priority(10);
    }

    private void update(EntityGoat goat) {
        boolean now = goat.isLoweringHead();
        if (now == this.lowered) {
            return;
        }
        if (now) {
            AzCommand.create("action", goat.isChild() ? "lower_head_baby" : "lower_head", AzPlayBehaviors.LOOP).sendForEntity(goat);
            this.action.fadeWeight(1.0D, 20.0D);
        } else {
            this.action.fadeWeight(0.0D, 10.0D);
        }
        // While the head is down, it does not follow where the goat looks
        context().poseLayer().fadeWeight("look", now ? 0.0D : 1.0D, 6.0D);
        this.lowered = now;
    }

    @Override
    public ResourceLocation getAnimationLocation(EntityGoat goat) {
        return ANIMATIONS;
    }
}
