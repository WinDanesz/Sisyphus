package com.windanesz.sisyphus.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.List;

public class CommandTeamMsg extends CommandBase {

    @Override
    public String getName() {
        return "teammsg";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("tm");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.teammsg.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException("commands.teammsg.usage");
        }
        Entity entity = sender.getCommandSenderEntity();
        Team team = entity == null ? null : entity.getTeam();
        if (!(team instanceof ScorePlayerTeam)) {
            throw new CommandException("commands.teammsg.failed.noteam");
        }
        ScorePlayerTeam scoreTeam = (ScorePlayerTeam) team;

        ITextComponent text = getChatComponentFromNthArg(sender, args, 0, !(sender instanceof EntityPlayerMP));
        TextFormatting color = scoreTeam.getColor();

        ITextComponent teamName = new TextComponentString("[" + scoreTeam.getDisplayName() + "]");
        if (color != null && color != TextFormatting.RESET) {
            teamName.getStyle().setColor(color);
        }
        ITextComponent message = new TextComponentString("").appendSibling(teamName).appendText(" <").appendSibling(sender.getDisplayName()).appendText("> ").appendSibling(text);

        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            if (player == entity || player.getTeam() == team) {
                player.sendMessage(message);
            }
        }
        server.sendMessage(message);
    }
}
