package com.javabuckets.ultimatetag;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerEventListener implements Listener {
    @EventHandler
    public void onPlayerTookDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player target = (Player) event.getEntity();

            if (event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();

                if (UltimateTag.roles.get(damager) == Role.TAGGER && UltimateTag.roles.get(target) == Role.PLAYER) {
                    UltimateTag.roles.put(target, Role.TAGGER);
                    Bukkit.broadcastMessage(target.getDisplayName() + " has been tagged!");
                    target.sendMessage("You've been tagged!");
                    target.setDisplayName(ChatColor.AQUA + target.getName());
                    target.setPlayerListName(ChatColor.AQUA + target.getName());
                    damager.sendMessage("You tagged " + target.getDisplayName() + "!");
                }
            }
        }
    }
}
