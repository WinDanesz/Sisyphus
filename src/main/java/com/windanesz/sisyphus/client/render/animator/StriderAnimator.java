package com.windanesz.sisyphus.client.render.animator;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.living.EntityStrider;
import mod.azure.azurelib.animation.blend.AzBlendSpace1D;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.minecraft.util.ResourceLocation;

public final class StriderAnimator extends AzEntityAnimator<EntityStrider> {

    private static final ResourceLocation ANIMATIONS = new ResourceLocation(SisyphusBackport.MODID, "animations/entity/strider.animation.json");

    private AzAnimationController<EntityStrider> shiver;
    private boolean shivering;

    public StriderAnimator() {
        addUpdateHook((entity, partialTicks) -> update(entity));
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<EntityStrider> container) {
        AzBlendSpace1D.<EntityStrider>builder(this, "locomotion")
                .input((entity, partialTicks) -> Gait.speed(entity))
                .sample(0.0D, "idle")
                .sample(2.0D, "walk")
                .sample(4.5D, "run")
                .smoothing(5.0D)
                .build()
                .addTo(container);
        shiver = AzAnimationController.<EntityStrider>builder(this, "shiver").setTransitionLength(2).setWeight(0.0D).build();
        container.add(shiver);
    }

    private void update(EntityStrider strider) {
        boolean cold = strider.isSuffocating();
        if (cold == this.shivering) {
            return;
        }
        if (cold) {
            AzCommand.create("shiver", "shiver", AzPlayBehaviors.LOOP).sendForEntity(strider);
        }
        this.shiver.fadeWeight(cold ? 1.0D : 0.0D, 4.0D);
        this.shivering = cold;
    }

    @Override
    public ResourceLocation getAnimationLocation(EntityStrider strider) {
        return ANIMATIONS;
    }
}
