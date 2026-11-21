package com.windanesz.sisyphus.client.render.animator;

import com.windanesz.sisyphus.SisyphusBackport;
import com.windanesz.sisyphus.entity.living.EntityWanderingTrader;
import mod.azure.azurelib.animation.blend.AzBlendSpace1D;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerContainer;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import mod.azure.azurelib.animation.pose.AzPoseModifierRegistry;
import mod.azure.azurelib.animation.pose.AzPoseModifiers;
import net.minecraft.util.ResourceLocation;

public final class WanderingTraderAnimator extends AzEntityAnimator<EntityWanderingTrader> {

    private static final ResourceLocation ANIMATIONS = new ResourceLocation(SisyphusBackport.MODID, "animations/entity/wandering_trader.animation.json");

    private static final String NONE = "";

    private AzAnimationController<EntityWanderingTrader> action;
    private String playing = NONE;

    public WanderingTraderAnimator() {
        addUpdateHook((entity, partialTicks) -> update(entity));
    }

    private static String wanted(EntityWanderingTrader trader) {
        if (trader.isHandActive()) {
            return "drink";
        }
        if (trader.isUnhappy()) {
            return "head_shake";
        }
        return NONE;
    }

    @Override
    public void registerControllers(AzAnimationControllerContainer<EntityWanderingTrader> container) {
        AzBlendSpace1D.<EntityWanderingTrader>builder(this, "locomotion")
                .input((entity, partialTicks) -> entity.gaitSpeed())
                .sample(0.0D, "idle")
                .sample(1.5D, "walk")
                .smoothing(5.0D)
                .build()
                .addTo(container);
        action = AzAnimationController.builder(this, "action").setTransitionLength(6).setWeight(0.0D).build();
        container.add(action);
    }

    @Override
    protected void registerPoseModifiers(AzPoseModifierRegistry<EntityWanderingTrader> modifiers) {
        // The rig faces north with AzureLib's internal axes, so both look axes are flipped to turn the right way.
        modifiers.add("look", AzPoseModifiers.<EntityWanderingTrader>headLook("head").limits(50.0F, 35.0F).invertYaw().invertPitch()).priority(10);
    }

    private void update(EntityWanderingTrader trader) {
        String wanted = wanted(trader);
        if (wanted.equals(playing)) {
            return;
        }
        if (!wanted.isEmpty()) {
            AzCommand.create("action", wanted, AzPlayBehaviors.LOOP).sendForEntity(trader);
            action.fadeWeight(1.0D, 5.0D);
        } else {
            action.fadeWeight(0.0D, 5.0D);
        }
        // The head follows what the trader looks at, except while it is busy shaking or tipping back
        context().poseLayer().fadeWeight("look", wanted.isEmpty() ? 1.0D : 0.0D, 6.0D);
        playing = wanted;
    }

    @Override
    public ResourceLocation getAnimationLocation(EntityWanderingTrader trader) {
        return ANIMATIONS;
    }
}
