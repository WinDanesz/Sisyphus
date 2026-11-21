package com.windanesz.sisyphus.mixin.minecraft;

import com.windanesz.sisyphus.worldgen.camps.WorldGenAbandonedCamps;
import com.windanesz.sisyphus.worldgen.ruins.WorldGenRuins;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandLocate;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the trail ruins, ocean ruins and abandoned camps to the /locate completions.
 */
@Mixin(CommandLocate.class)
public abstract class MixinCommandLocate {

    @Inject(method = "getTabCompletions", at = @At("RETURN"), cancellable = true)
    private void sisyphus$completeRuins(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos,
                                        CallbackInfoReturnable<List<String>> cir) {
        if (args.length != 1) {
            return;
        }
        List<String> names = new ArrayList<>(WorldGenRuins.locateNames());
        if (WorldGenAbandonedCamps.locatable()) {
            names.add(WorldGenAbandonedCamps.LOCATE);
        }
        List<String> ours = CommandBase.getListOfStringsMatchingLastWord(args, names);
        if (ours.isEmpty()) {
            return;
        }
        List<String> completions = new ArrayList<>(cir.getReturnValue());
        completions.addAll(ours);
        cir.setReturnValue(completions);
    }
}
