package me.xsmart.blockswap;

import com.mojang.datafixers.types.templates.Check;
import me.xsmart.blockswap.commands.*;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Bukkit.broadcastMessage;

public class BlockSwap extends JavaPlugin implements Listener {


    public void onEnable(){
        getLogger().info("BlockSwap Is loaded!");
        getServer().getPluginManager().registerEvents(this,this);
        makeBlockList();
        //this.getCommand("enableblockswap").setExecutor((CommandExecutor)new EnableBlockSwap());

    }

    //Last Error --> Instant Lose and Server Lag!

    public void onDisable(){
        getLogger().info("BlockSwap Is now unloaded.");
    }

    boolean gameEnabled = false;
    boolean roundIsOver = true;
    int count = 0;
    boolean isRunning = false;
    Material block = Material.AIR;
    ArrayList<Material> blocks = new ArrayList();
    ArrayList<Player> players = new ArrayList();




    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings){
        if(commandSender instanceof Player && command.getName().equalsIgnoreCase("enableblockswap"))
        {
            Player player = (Player) commandSender;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',"&aBlock Swap Is Now Enabled!!"));
            gameEnabled = true;
            for(Player p : Bukkit.getOnlinePlayers())
            {
                players.add(p);
                p.setGameMode(GameMode.SURVIVAL);
            }
            ArrayList<Material> playerBlock = new ArrayList<Material>();
            for(int i = 0; i < players.size(); i++)
            {
                playerBlock.add(Material.AIR);
            }

            while(players.size() >= 1 || gameEnabled) {
                round(playerBlock);
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

    public void round(ArrayList<Material> playerBlock)
    {
        roundIsOver = false;

        for(int i = 0; i < players.size();i++)
            playerBlock.add(i, assignBlock(players.get(i)));

        ArrayList<Boolean> survivedRound = loopCheck(playerBlock);
        eliminate(survivedRound);
        roundIsOver = true;
    }


    public Material assignBlock(Player p)
    {
        int randomNumber = (int)(Math.random() * blocks.size());
        block = blocks.get(randomNumber);
        p.sendMessage(ChatColor.GREEN + "You must find and stand on: " + block.toString() + ".");
        return block;
    }


    public ArrayList<Boolean> loopCheck (ArrayList<Material> playerBlock)
    {
        ArrayList<Boolean> survivedRound = new ArrayList();
        for(int i = 0; i < players.size(); i++)
        {
            survivedRound.add(false);
        }

        int tickCount = 0;

        count = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if(tickCount < 7200) {
                    loop(playerBlock, survivedRound);
                }
                else
                    Bukkit.getServer().getScheduler().cancelTask(count);
            }
        },0L,1L);


        return survivedRound;
    }

    public void loop(ArrayList<Material> playerBlock, ArrayList<Boolean> survivedRound) {
        for (int i = 0; i < players.size(); i++) {
            if (isStandingOnBlock(players.get(i), playerBlock.get(i)) && !survivedRound.get(i)) {
                survivedRound.add(i, true);
                broadcastMessage(ChatColor.GOLD + players.get(i).getName() + " has stood on " + playerBlock.get(i).toString());
            }
        }
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


    public void eliminate(ArrayList<Boolean> playersSurvived)
    {
        for (int i = 0; i < players.size(); i++) {
            if (playersSurvived.get(i) == false) {
                broadcastMessage(ChatColor.RED + players.get(i).getName() + " has failed to stand on their block and has been eliminated!");
            }
        }

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
