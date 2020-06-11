package me.xsmart.blockswap.commands;

import net.minecraft.server.v1_15_R1.BlockGrass;
import net.minecraft.server.v1_15_R1.PlayerSelector;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Snow;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Bukkit.broadcastMessage;

public class EnableBlockSwap implements CommandExecutor {

    boolean gameEnabled = false;
    boolean roundIsOver = true;
    Material block = Material.AIR;
    ArrayList<Material> blocks = new ArrayList();
    ArrayList<Player> players = new ArrayList();
    ArrayList<Material> playerBlock = new ArrayList();
    ArrayList<Boolean> survivedRound = new ArrayList();


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings){
        if(commandSender instanceof Player)
        {
            Player player = (Player) commandSender;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&aBlock Swap Is Now Enabled!!"));
            gameEnabled = true;
            makeBlockList();
            firstRound();
            while(players.size() >= 1 || gameEnabled) {
                round();
            }


        }
        else
        {
            commandSender.sendMessage("You Can't Use This in the Terminal!");
        }

        return true;
    }

    public void makeBlockList()
    {
        blocks.add(Material.GRASS_BLOCK);
        blocks.add(Material.DIRT);
        blocks.add(Material.COBBLESTONE);
        blocks.add(Material.STONE);
        blocks.add(Material.COAL_ORE);
        blocks.add(Material.COAL_BLOCK);
        blocks.add(Material.IRON_ORE);
        blocks.add(Material.IRON_BLOCK);
    }

    public void round()
    {
        roundIsOver = false;
        setupRound();
        ArrayList<Boolean> survivedRound = loopCheck();
        eliminate(survivedRound);
    }

    public void firstRound()
    {
        for(Player p : Bukkit.getOnlinePlayers())
        {
            players.add(p);
            p.setGameMode(GameMode.SURVIVAL);
        }
    }

    public void setupRound()
    {
         for(int i = 0; i < players.size();i++)
            playerBlock.add(assignBlock(players.get(i)));

    }

    public Material assignBlock(Player p)
    {
        int randomNumber = (int)(Math.random() * blocks.size());
        block = blocks.get(randomNumber);
        p.sendMessage(ChatColor.GREEN + "You must find and stand on: " + block.toString() + ".");
        return block;
    }

    public boolean isStandingOnBlock(Player player, Material material) {
        Location location = player.getLocation().clone(); // Cloned location
        for (int blocks = 1; blocks <= 1; blocks++) {
            location.subtract(0, 1, 0); // Move one block down
            if (location.getBlock().getType() == material) { // If this is the material -> return true (break/exit loop)
                return true;
            }
        }
        return false; // No such material was found in all blocks -> return false
    }

    public ArrayList<Boolean> loopCheck ()
    {
        ArrayList<Boolean> survivedRound = new ArrayList(players.size());
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        long t = System.currentTimeMillis();
        long end = t + 300000;
        while(System.currentTimeMillis() < end) {
            executorService.schedule(this::loop, 1, TimeUnit.SECONDS);
        }

        for(int i = 0; i < players.size(); i++)
        {
            if(survivedRound.get(i) == null)
            {
                survivedRound.add(i,false);
                broadcastMessage(ChatColor.RED + players.get(i).getName() + " has failed to stand on " + playerBlock.get(i).toString() + " and has been eliminated!");
            }
        }

        return survivedRound;
    }

    public void loop() {
        for (int i = 0; i < players.size(); i++) {
            if (isStandingOnBlock(players.get(i), playerBlock.get(i)) && !survivedRound.get(i)) {
                survivedRound.add(i, true);
                broadcastMessage(ChatColor.GOLD + players.get(i).getName() + " has stood on " + playerBlock.get(i).toString());
            }
        }
    }

    public void eliminate(ArrayList<Boolean> playersSurvived)
    {
        for(int i = 0; i < playersSurvived.size(); i++)
        {
            if(playersSurvived.get(i) == false) {
                players.get(i).setGameMode(GameMode.SPECTATOR);
                players.get(i).sendMessage(ChatColor.AQUA + "You have been eliminated!");
                players.remove(i);
            }
        }
    }

}

