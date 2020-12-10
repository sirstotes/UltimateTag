package com.javabuckets.ultimatetag;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUltimateTag implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;

            if (args.length == 0) {
                playerSender.sendMessage("Please specify at least one player");
                return false;
            } else {
                UltimateTag.contestants.add(playerSender);

                for (String arg : args) {
                    Player p = Bukkit.getPlayer(arg);

                    if (p == null) {
                        UltimateTag.contestants.clear();
                        return false;
                    } else {
                        UltimateTag.contestants.add(p);
                    }
                }

                UltimateTag.initialize(); // TODO: /ultimatetag <timer> <win-score>? <border-size> ...<player>
            }
        }

        return false;
    }
}
