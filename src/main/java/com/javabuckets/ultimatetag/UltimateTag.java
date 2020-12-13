package com.javabuckets.ultimatetag;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public final class UltimateTag extends JavaPlugin {

    public static boolean isRunning = false;

    public static int timer = 2 * 60; // minutes * seconds_converter

    public static ArrayList<Player> contestants = new ArrayList<>();
    public static HashMap<Player, Role> roles = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getCommand("ultimatetag").setExecutor(new CommandUltimateTag(this));

        getServer().getPluginManager().registerEvents(new PlayerEventListener(), this);

        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    // Timer runs out, it's a loss for the taggers
                    if (timer < 0) {
                        contestants.forEach(contestant -> contestant.sendMessage("Timer went out! Taggers lose!"));
                        deinitialize();
                    }

                    if (timer == 10) {
                        contestants.forEach(contestant -> contestant.sendMessage("There is 10 seconds left!"));
                    }

                    // There are no more players, taggers win
                    if (!roles.containsValue(Role.PLAYER)) {
                        contestants.forEach(contestant -> contestant.sendMessage("Everyone has been tagged! Taggers win!"));
                        deinitialize();
                    }

                    // This will still run if someone lose, so we have to wrap the rest of the checks in another if statement and check if isRunning is still true
                    if (isRunning) {
                        // Last thing to do is to decrease timer
                        timer--;
                    }
                }
            }
        }, 0, 20); // Should be every second
    }

    public static void initialize(JavaPlugin plugin) {
        // Decide on a tagger
        Random random = new Random();
        int taggerIndex = random.nextInt(contestants.size());
        Player tagger = contestants.get(taggerIndex);
        tagger.setDisplayName(ChatColor.AQUA + tagger.getName());
        tagger.setPlayerListName(ChatColor.AQUA + tagger.getName());

        // Assign roles to all contestants
        for (Player contestant : contestants) {
            if (contestant == tagger) {
                roles.put(contestant, Role.TAGGER);
                contestant.sendMessage(ChatColor.RED + "You are the tagger!");
            } else {
                roles.put(contestant, Role.PLAYER);
                contestant.sendMessage(ChatColor.RED + tagger.getDisplayName() + " is the tagger!");
            }
        }

        // Decide on a random area for the game
        World gameWorld = getGameWorld(tagger);

        if (gameWorld == null) {
            return;
        }

        Location center = findSuitableCenter(gameWorld);
        gameWorld.getChunkAt(center).load();
        gameWorld.setTime(0);

        for (Player contestant : contestants) {
            int playerRandomX = center.getBlockX() + random.nextInt(64) - 32;
            int playerRandomZ = center.getBlockZ() + random.nextInt(64) - 32;
            int playerRandomY = gameWorld.getHighestBlockYAt(playerRandomX, playerRandomZ);

            Location location = new Location(gameWorld, playerRandomX, playerRandomY + 1, playerRandomZ);

            // Potion effects
            contestant.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 60 * 2, 3));

            if (contestant != tagger) {
                contestant.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY,140, 1));

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        contestant.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 60 * 2, 1));
                    }
                }, 7000);
            }

            // Teleport the contestants
            contestant.teleport(location);

            // Reset player stats and inventory
            resetContestant(contestant);

            // Give players starter items
            ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE);
            ItemMeta pickaxeMeta = pickaxe.getItemMeta();
            pickaxeMeta.setUnbreakable(true);
            pickaxe.setItemMeta(pickaxeMeta);

            ItemStack shovel = new ItemStack(Material.IRON_SHOVEL);
            ItemMeta shovelMeta = shovel.getItemMeta();
            shovelMeta.setUnbreakable(true);
            shovel.setItemMeta(shovelMeta);

            ItemStack axe = new ItemStack(Material.IRON_AXE);
            ItemMeta axeMeta = axe.getItemMeta();
            axeMeta.setUnbreakable(true);
            axe.setItemMeta(axeMeta);

            contestant.getInventory().addItem(
                    pickaxe,
                    shovel,
                    axe,
                    new ItemStack(Material.COBBLESTONE, 16),
                    new ItemStack(Material.WATER_BUCKET)
            );

            // Reset their possible bed location to their teleport location
            contestant.setBedSpawnLocation(location);
        }

        // Give tagger some additional items
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bow.getItemMeta();
        bowMeta.setUnbreakable(true);
        bowMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        bowMeta.addEnchant(Enchantment.ARROW_KNOCKBACK, 2, true);
        bow.setItemMeta(bowMeta);

        tagger.getInventory().addItem(
                bow,
                new ItemStack(Material.FISHING_ROD),
                new ItemStack(Material.ARROW, 1)
        );

        // Set the world border
        WorldBorder border = gameWorld.getWorldBorder();

        border.setCenter(center);
        border.setSize(64);

        isRunning = true;
    }

    public static void deinitialize() {
        isRunning = false;
        timer = 2 * 60;
        contestants.forEach(contestant -> resetContestant(contestant));
        contestants.clear();
        roles.clear();

        for (World world : Bukkit.getWorlds()) {
            world.getWorldBorder().reset();
        }
    }

    public static World getGameWorld(Player tagger) {
        for (World world : Bukkit.getWorlds()) {
            if (world.getPlayers().contains(tagger)) {
                return world;
            }
        }
        return null;
    }

    public static void resetContestant(Player contestant) {
        contestant.getInventory().clear();
        contestant.setFoodLevel(20);
        contestant.setHealth(20);
        contestant.setExhaustion(0);
        contestant.setDisplayName(contestant.getName());
        contestant.setPlayerListName(contestant.getName());
        contestant.setGameMode(GameMode.SURVIVAL);
    }

    public static Location findSuitableCenter(World world) {
        Random random = new Random();

        int randomX = random.nextInt(10000);
        int randomZ = random.nextInt(10000);
        int randomY = world.getHighestBlockYAt(randomX, randomZ);

        Location location = new Location(world, randomX, randomY, randomZ);

        while (location.getBlock().isLiquid()) {
            int newX = random.nextInt(10000);
            int newZ = random.nextInt(10000);
            int newY = world.getHighestBlockYAt(newX, newZ);
            location = new Location(world, newX, newY, newZ);
        }

        return location;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        deinitialize();
    }
}
