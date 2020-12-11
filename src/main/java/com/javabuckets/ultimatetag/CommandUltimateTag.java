package com.javabuckets.ultimatetag;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandUltimateTag implements CommandExecutor {
    JavaPlugin plugin;

    public CommandUltimateTag(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player playerSender = (Player) sender;

            if (args.length == 0) {
                if (UltimateTag.isRunning) {
                    playerSender.sendMessage("A game is already in progress");
                    return true;
                }

                playerSender.sendMessage("All players will be assigned as contestants");

                UltimateTag.contestants.addAll(playerSender.getWorld().getPlayers());

                UltimateTag.initialize(plugin);

                return true;

            } else {
                if (args[0].equals("stop")) {
                    UltimateTag.deinitialize();
                    Bukkit.broadcastMessage("UltimateTag has stopped");
                    return true;
                }

                if (UltimateTag.isRunning) {
                    playerSender.sendMessage("A game is already in progress");
                    return true;
                }

                UltimateTag.contestants.add(playerSender);

                for (String arg : args) {
                    Player p = Bukkit.getPlayer(arg);

                    if (p == null) {
                        UltimateTag.contestants.clear();
                        return true;
                    } else {
                        UltimateTag.contestants.add(p);
                    }
                }

                UltimateTag.initialize(plugin); // TODO: /ultimatetag <timer> <win-score>? <border-size> ...<player>
            }
        }

        return false;
    }
}
