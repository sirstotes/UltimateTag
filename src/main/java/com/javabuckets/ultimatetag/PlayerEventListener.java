package com.javabuckets.ultimatetag;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

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

                    // Target is now a tagger, so give them tagger items
                    ItemStack bow = new ItemStack(Material.BOW);
                    ItemMeta bowMeta = bow.getItemMeta();
                    bowMeta.setUnbreakable(true);
                    bowMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
                    bowMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, 2, true);
                    bow.setItemMeta(bowMeta);

                    target.getInventory().addItem(
                            bow,
                            new ItemStack(Material.FISHING_ROD),
                            new ItemStack(Material.ARROW, 1)
                    );

                    target.removePotionEffect(PotionEffectType.GLOWING);
                }
            }
        }
    }
}
