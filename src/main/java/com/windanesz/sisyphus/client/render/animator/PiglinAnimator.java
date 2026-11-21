package com.windanesz.sisyphus.client.render.animator;

import com.windanesz.sisyphus.SisyphusBackport;
import mod.azure.azurelib.animation.blend.AzBlendSpace1D;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.pose.AzPoseModifierRegistry;
import mod.azure.azurelib.animation.pose.AzPoseModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public final class PiglinAnimator<T extends EntityLivingBase> extends AzEntityAnimator<T> {

    private static final ResourceLocation ANIMATIONS = new ResourceLocation(SisyphusBackport.MODID, "animations/entity/piglin.animation.json");

    private static final String NONE = "";

    private final Function<T, String> armPose;
    private AzAnimationController<T> arms;
    private AzAnimationController<T> swing;
    private AzAnimationController<T> baby;
    private String playing = NONE;
    private boolean swinging;
    private boolean small;

    /**
     * The function names the arm animation needed now, or returns an empty string for none.
     */
    public PiglinAnimator(Function<T, String> armPose) {
        this.armPose = armPose;
        addUpdateHook((entity, partialTicks) -> update(entity));
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<T> container) {
        AzBlendSpace1D.<T>builder(this, "locomotion")
                .input((entity, partialTicks) -> Gait.speed(entity))
                .sample(0.0D, "idle")
                .sample(2.0D, "walk")
                .sample(4.5D, "run")
                .smoothing(5.0D)
                .build()
                .addTo(container);
        arms = AzAnimationController.<T>builder(this, "arms").setTransitionLength(6).setWeight(0.0D).build();
        container.add(arms);
        swing = AzAnimationController.<T>builder(this, "swing").setTransitionLength(1).setWeight(0.0D).build();
        container.add(swing);
        baby = AzAnimationController.<T>builder(this, "baby").setTransitionLength(1).setWeight(0.0D).build();
        container.add(baby);
    }

    @Override
    protected void registerPoseModifiers(AzPoseModifierRegistry<T> modifiers) {
        modifiers.add("look", AzPoseModifiers.<T>headLook("head").limits(60.0F, 40.0F).invertYaw().invertPitch()).priority(10);
    }

    private void update(T entity) {
        String wanted = this.armPose.apply(entity);
        if (!wanted.equals(this.playing)) {
            if (!wanted.isEmpty()) {
                AzCommand.create("arms", wanted, AzPlayBehaviors.LOOP).sendForEntity(entity);
                this.arms.fadeWeight(1.0D, 6.0D);
            } else {
                this.arms.fadeWeight(0.0D, 6.0D);
            }
            // The head follows what the piglin looks at, except while it bends down over the gold
            context().poseLayer().fadeWeight("look", wanted.equals("admire") ? 0.0D : 1.0D, 6.0D);
            this.playing = wanted;
        }

        boolean swingNow = entity.isSwingInProgress && wanted.equals("melee_ready");
        if (swingNow != this.swinging) {
            if (swingNow) {
                AzCommand.create("swing", "swing", AzPlayBehaviors.PLAY_ONCE).sendForEntity(entity);
                this.swing.fadeWeight(1.0D, 1.0D);
            } else {
                this.swing.fadeWeight(0.0D, 2.0D);
            }
            this.swinging = swingNow;
        }

        boolean child = entity.isChild();
        if (child != this.small) {
            if (child) {
                AzCommand.create("baby", "baby", AzPlayBehaviors.LOOP).sendForEntity(entity);
            }
            this.baby.fadeWeight(child ? 1.0D : 0.0D, 1.0D);
            this.small = child;
        }
    }

    @Override
    public ResourceLocation getAnimationLocation(T entity) {
        return ANIMATIONS;
    }
}
