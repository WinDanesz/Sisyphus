package com.windanesz.sisyphus.client.render.animator;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.living.EntityFox;
import mod.azure.azurelib.animation.blend.AzBlendSpace1D;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.pose.AzPoseModifierRegistry;
import mod.azure.azurelib.animation.pose.AzPoseModifiers;
import net.minecraft.util.ResourceLocation;

public final class FoxAnimator extends AzEntityAnimator<EntityFox> {

    private static final ResourceLocation ANIMATIONS = new ResourceLocation(SisyphusBackport.MODID, "animations/entity/fox.animation.json");

    private static final String NONE = "";

    private AzAnimationController<EntityFox> action;
    private AzAnimationController<EntityFox> tilt;
    private String playing = NONE;
    private boolean tilted;
    private boolean pouncing;

    public FoxAnimator() {
        addUpdateHook((entity, partialTicks) -> update(entity));
    }

    private static String wanted(EntityFox fox) {
        if (fox.isFaceplanted()) {
            return "faceplant";
        }
        if (fox.isSleeping()) {
            return "sleep";
        }
        if (fox.isSitting()) {
            return "sit";
        }
        if (fox.isCrouching()) {
            return "crouch";
        }
        return NONE;
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<EntityFox> container) {
        AzBlendSpace1D.<EntityFox>builder(this, "locomotion")
                .input((entity, partialTicks) -> entity.gaitSpeed())
                .sample(0.0D, "idle")
                .sample(2.0D, "walk")
                .sample(5.0D, "run")
                .smoothing(5.0D)
                .build()
                .addTo(container);
        action = AzAnimationController.builder(this, "action").setTransitionLength(6).setWeight(0.0D).build();
        container.add(action);
        tilt = AzAnimationController.builder(this, "tilt").setTransitionLength(4).setWeight(0.0D).build();
        container.add(tilt);
    }

    @Override
    protected void registerPoseModifiers(AzPoseModifierRegistry<EntityFox> modifiers) {
        // The rig faces north with AzureLib's internal axes, so both look axes are flipped to turn the right way.
        modifiers.add("look", AzPoseModifiers.<EntityFox>headLook("head").limits(60.0F, 40.0F).invertYaw().invertPitch()).priority(10);
        // In a pounce the whole fox follows its look direction up and down, which the server sets from the leap. Turning the
        // root bone the way the head turns, without the sideways part, tips the fox the way it flies.
        modifiers.add("pounce", AzPoseModifiers.<EntityFox>headLook("root").limits(0.0F, 70.0F).invertPitch()).priority(5);
    }

    private void update(EntityFox fox) {
        String wanted = wanted(fox);
        if (!wanted.equals(playing)) {
            if (!wanted.isEmpty()) {
                AzCommand.create("action", wanted, AzPlayBehaviors.LOOP).sendForEntity(fox);
                action.fadeWeight(1.0D, wanted.equals("faceplant") ? 2.0D : 7.0D);
            } else {
                action.fadeWeight(0.0D, 5.0D);
            }
            // The head follows what the fox looks at, except while it sleeps, crouches or has its head in the snow
            boolean look = !(wanted.equals("sleep") || wanted.equals("crouch") || wanted.equals("faceplant"));
            context().poseLayer().fadeWeight("look", look ? 1.0D : 0.0D, 8.0D);
            playing = wanted;
        }

        boolean interested = fox.isInterested();
        if (interested != tilted) {
            if (interested) {
                AzCommand.create("tilt", "interested", AzPlayBehaviors.LOOP).sendForEntity(fox);
            }
            tilt.fadeWeight(interested ? 1.0D : 0.0D, 6.0D);
            tilted = interested;
        }

        boolean leaping = fox.isPouncing();
        if (leaping != pouncing) {
            context().poseLayer().fadeWeight("pounce", leaping ? 1.0D : 0.0D, 3.0D);
            pouncing = leaping;
        }
    }

    @Override
    public ResourceLocation getAnimationLocation(EntityFox fox) {
        return ANIMATIONS;
    }
}
