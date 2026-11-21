package com.windanesz.sisyphus.client.render.animator;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.living.HoglinLike;
import mod.azure.azurelib.animation.blend.AzBlendSpace1D;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.pose.AzPoseModifierRegistry;
import mod.azure.azurelib.animation.pose.AzPoseModifiers;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

public final class HoglinAnimator<T extends EntityLiving & HoglinLike> extends AzEntityAnimator<T> {

    private static final ResourceLocation ANIMATIONS = new ResourceLocation(SisyphusBackport.MODID, "animations/entity/hoglin.animation.json");

    private AzAnimationController<T> action;
    private boolean butting;

    public HoglinAnimator() {
        addUpdateHook((entity, partialTicks) -> update(entity));
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<T> container) {
        AzBlendSpace1D.<T>builder(this, "locomotion")
                .input((entity, partialTicks) -> Gait.speed(entity))
                .sample(0.0D, "idle")
                .sample(2.0D, "walk")
                .sample(5.0D, "run")
                .smoothing(5.0D)
                .build()
                .addTo(container);
        action = AzAnimationController.<T>builder(this, "action").setTransitionLength(2).setWeight(0.0D).build();
        container.add(action);
    }

    @Override
    protected void registerPoseModifiers(AzPoseModifierRegistry<T> modifiers) {
        modifiers.add("look", AzPoseModifiers.<T>headLook("head").limits(45.0F, 0.0F).invertYaw().invertPitch()).priority(10);
    }

    private void update(T entity) {
        boolean now = entity.getAttackTicks() > 0;
        if (now == this.butting) {
            return;
        }
        if (now) {
            AzCommand.create("action", "attack", AzPlayBehaviors.PLAY_ONCE).sendForEntity(entity);
            this.action.fadeWeight(1.0D, 1.0D);
        } else {
            this.action.fadeWeight(0.0D, 3.0D);
        }
        this.butting = now;
    }

    @Override
    public ResourceLocation getAnimationLocation(T entity) {
        return ANIMATIONS;
    }
}
