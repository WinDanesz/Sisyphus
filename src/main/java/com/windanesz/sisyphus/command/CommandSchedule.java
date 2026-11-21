package com.windanesz.sisyphus.command;

import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandSchedule extends CommandBase {

    private static final long TICKS_PER_SECOND = 20;
    private static final long TICKS_PER_DAY = 24000;

    private static long parseTime(String text) throws CommandException {
        long unit = 1;
        String number = text;
        char last = text.isEmpty() ? ' ' : text.charAt(text.length() - 1);
        if (last == 's') {
            unit = TICKS_PER_SECOND;
            number = text.substring(0, text.length() - 1);
        } else if (last == 'd') {
            unit = TICKS_PER_DAY;
            number = text.substring(0, text.length() - 1);
        } else if (last == 't') {
            number = text.substring(0, text.length() - 1);
        }
        double value;
        try {
            value = Double.parseDouble(number);
        } catch (NumberFormatException e) {
            throw new NumberInvalidException("commands.generic.num.invalid", text);
        }
        if (value < 0) {
            throw new NumberInvalidException("commands.generic.num.tooSmall", text, 0);
        }
        return Math.max(1, Math.round(value * unit));
    }

    @Override
    public String getName() {
        return "schedule";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.schedule.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            throw new WrongUsageException("commands.schedule.usage");
        }
        ScheduledFunctions schedule = ScheduledFunctions.get(server);
        if (schedule == null) {
            throw new CommandException("commands.schedule.failed.noworld");
        }
        ResourceLocation function = new ResourceLocation(args[1]);

        if ("clear".equals(args[0])) {
            if (args.length != 2) {
                throw new WrongUsageException("commands.schedule.usage");
            }
            if (schedule.clear(function) == 0) {
                throw new CommandException("commands.schedule.clear.failed", function);
            }
            notifyCommandListener(sender, this, "commands.schedule.cleared", function);
            return;
        }

        if (!"function".equals(args[0]) || args.length < 3 || args.length > 4) {
            throw new WrongUsageException("commands.schedule.usage");
        }
        if (server.getFunctionManager().getFunction(function) == null) {
            throw new CommandException("commands.schedule.unknownfunction", function);
        }
        long delay = parseTime(args[2]);
        boolean replace = true;
        if (args.length == 4) {
            if ("append".equals(args[3])) {
                replace = false;
            } else if (!"replace".equals(args[3])) {
                throw new WrongUsageException("commands.schedule.usage");
            }
        }
        long now = server.getWorld(0).getTotalWorldTime();
        schedule.schedule(function, now + delay, replace);
        notifyCommandListener(sender, this, "commands.schedule.created", function, delay, now + delay);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "function", "clear");
        }
        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, server.getFunctionManager().getFunctions().keySet());
        }
        if (args.length == 4 && "function".equals(args[0])) {
            return getListOfStringsMatchingLastWord(args, Arrays.asList("append", "replace"));
        }
        return Collections.emptyList();
    }
}
