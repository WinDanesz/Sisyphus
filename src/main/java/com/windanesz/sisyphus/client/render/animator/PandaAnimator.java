package com.windanesz.sisyphus.client.render.animator;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.living.EntityPanda;
import mod.azure.azurelib.animation.blend.AzBlendSpace1D;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.pose.AzPoseModifierRegistry;
import mod.azure.azurelib.animation.pose.AzPoseModifiers;
import net.minecraft.util.ResourceLocation;

public final class PandaAnimator extends AzEntityAnimator<EntityPanda> {

    private static final ResourceLocation ANIMATIONS = new ResourceLocation(SisyphusBackport.MODID, "animations/entity/panda.animation.json");

    private static final String NONE = "";

    private AzAnimationController<EntityPanda> action;
    private String playing = NONE;
    private AzAnimationController<EntityPanda> attack;
    private boolean attacking;

    public PandaAnimator() {
        addUpdateHook((entity, partialTicks) -> update(entity));
    }

    private static String wanted(EntityPanda panda) {
        if (panda.isRolling()) {
            return "roll";
        }
        if (panda.isOnBack()) {
            return "lie_on_back";
        }
        if (panda.isSitting()) {
            if (panda.isScared()) {
                return "sit_scared";
            }
            return panda.isEating() ? "sit_eat" : "sit";
        }
        if (panda.isSneezing()) {
            return "sneeze";
        }
        if (panda.isUnhappy()) {
            return "unhappy";
        }
        return NONE;
    }

    private static boolean loops(String animation) {
        return animation.startsWith("sit") || animation.equals("lie_on_back");
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<EntityPanda> container) {
        AzBlendSpace1D.<EntityPanda>builder(this, "locomotion")
                .input((entity, partialTicks) -> entity.gaitSpeed())
                .sample(0.0D, "idle")
                .sample(2.0D, "walk")
                .smoothing(5.0D)
                .build()
                .addTo(container);
        action = AzAnimationController.builder(this, "action").setTransitionLength(6).setWeight(0.0D).build();
        container.add(action);
        attack = AzAnimationController.builder(this, "attack").setTransitionLength(1).setWeight(0.0D).build();
        container.add(attack);
    }

    @Override
    protected void registerPoseModifiers(AzPoseModifierRegistry<EntityPanda> modifiers) {
        // The rig faces north with AzureLib's internal axes, so both look axes are flipped to turn the right way.
        modifiers.add("look", AzPoseModifiers.<EntityPanda>headLook("head").limits(45.0F, 30.0F).invertYaw().invertPitch()).priority(10);
    }

    private void update(EntityPanda panda) {
        String wanted = wanted(panda);
        String hit = panda.currentAttack();
        boolean attackingNow = hit != null;
        if (wanted.equals(playing) && attackingNow == attacking) {
            return;
        }
        if (!wanted.equals(playing)) {
            if (!wanted.isEmpty()) {
                AzCommand.create("action", wanted, loops(wanted) ? AzPlayBehaviors.LOOP : AzPlayBehaviors.HOLD_ON_LAST_FRAME).sendForEntity(panda);
                // Sitting down and lying down take a few ticks, the way the model eases them in
                action.fadeWeight(1.0D, wanted.equals("roll") ? 3.0D : 7.0D);
            } else {
                action.fadeWeight(0.0D, 5.0D);
            }
            playing = wanted;
        }
        if (attackingNow != attacking) {
            // The bite or swipe plays over the walk, so a panda can strike while it chases
            if (attackingNow) {
                AzCommand.create("attack", hit, AzPlayBehaviors.PLAY_ONCE).sendForEntity(panda);
                attack.fadeWeight(1.0D, 1.0D);
            } else {
                attack.fadeWeight(0.0D, 2.0D);
            }
            attacking = attackingNow;
        }
        // The head follows what the panda looks at only while it is standing or walking around, not while it strikes
        context().poseLayer().fadeWeight("look", wanted.isEmpty() && !attacking ? 1.0D : 0.0D, attacking ? 2.0D : 8.0D);
    }

    @Override
    public ResourceLocation getAnimationLocation(EntityPanda panda) {
        return ANIMATIONS;
    }
}
