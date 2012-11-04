package com.norcode.bukkit.flytime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class FlyTimePlugin extends JavaPlugin {
	private FlyTimePlayerListener playerListener;
	public YamlConfiguration playerFlytimes;
	public HashSet<Player> fallingPlayers;
	public HashMap<Player, Long> flyingPlayers;
	public HashSet<Player> enabledPlayers;
	public PlayerTownyListener townyListener;
	private HashMap<Player, Integer> notifierIds;
	private HashMap<Player, Integer> disablerIds;
	
	public class PlayerFlytimeNotifier implements Runnable {
		private Player player;
		public PlayerFlytimeNotifier(Player p) {
			this.player = p;
		}
		@Override
		public void run() {
			long seconds = playerFlytimes.getLong(this.player.getName())/1000;
			this.player.sendMessage("You have " + seconds + " seconds of FlyTime remaining.");
		}
	}

	public class SetFallingTask implements  Runnable {
		private Player player;
		public SetFallingTask(Player p) {
			this.player = p;
		}
		@Override
		public void run() {
			getLogger().info("Adding " + player.getName() + " to falling players list.");
			fallingPlayers.add(this.player);
		}
		
	}
	public class AutoDisabler implements Runnable {
		private Player player;
		public AutoDisabler(Player p) {
			this.player = p;
		}
		@Override
		public void run() {
			disableFlight(this.player);
		}
	}
	
	@Override
	public void onEnable() {
		this.fallingPlayers = new HashSet<Player>();
		this.flyingPlayers = new HashMap<Player, Long>();
		this.enabledPlayers = new HashSet<Player>();
		this.notifierIds = new HashMap<Player, Integer>();
		this.disablerIds = new HashMap<Player, Integer>();
		saveDefaultConfig();
		loadPlayerData();
		townyCheck();
		playerListener = new FlyTimePlayerListener(this);
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new FlyTimeChecker(this), 20, 20);
	}
	
	
	public void townyCheck() {
		Plugin towny = getServer().getPluginManager().getPlugin("Towny");
		if (towny != null) {
			townyListener = new PlayerTownyListener(this);
			getServer().getPluginManager().registerEvents(townyListener, this);
		}
	}
	
	public void loadPlayerData() {
		File playerFile = new File(getDataFolder(), "players.yml");
		playerFlytimes = new YamlConfiguration();
		if (playerFile.exists()) {
			try {
				playerFlytimes.load(playerFile);
			} catch (IOException
					| InvalidConfigurationException e) {
				// TODO Auto-generated catch block
				getLogger().warning("Invalid players.yml, will be overwritten");
				return;
			}
		}
	}
	
	public void savePlayerData() {
		File playerFile = new File(getDataFolder(), "players.yml");
		try {
			playerFlytimes.save(playerFile);
		} catch (IOException e) {
			getLogger().warning("Failed to save players.yml, fly times will not be saved!");
		}
	}
	
	public void onDisable() {
		savePlayerData();
	}
	
	public void endFlight(Player p) {
		endFlight(p, false);
	}
	
	public void endFlight(Player p, boolean disabling) {
		getLogger().info("Ending flight for " + p.getName());
		flyingPlayers.remove(p);
		
		getServer().getScheduler().cancelTask(notifierIds.get(p));
		if (getConfig().getBoolean("disableOnLanding", true) && !disabling) {
			Integer disablerId = getServer().getScheduler()
					.scheduleSyncDelayedTask(this, new AutoDisabler(p), getConfig().getLong("autodisable_ticks"));
			disablerIds.put(p, disablerId);
		}
	}
	
	public long addTime(OfflinePlayer p, long amount) {
		long amt = playerFlytimes.getLong(p.getName(), 0);
		amt += amount;
		playerFlytimes.set(p.getName(), amt);
		return amt;
	}
	
	public void enableFlight(Player p) {
		if (enabledPlayers.contains(p)) {
			p.sendMessage("FlyTime is already enabled.");
			return;
		}
		
		getLogger().info("Enabling Flight for " + p.getName());
		p.sendMessage("Enabling FlyTime, you have " + (playerFlytimes.getLong(p.getName())/1000) + " seconds remaining.");
		p.setAllowFlight(true);
		enabledPlayers.add(p);
		Integer disablerId = getServer().getScheduler()
				.scheduleSyncDelayedTask(this, new AutoDisabler(p), getConfig().getLong("autodisable_ticks"));
		disablerIds.put(p, disablerId);		
	}
	
	
	public void disableFlight(Player p) {
		if (p.isFlying()) {
			getServer().getScheduler().scheduleSyncDelayedTask(this, new SetFallingTask(p), getConfig().getLong("fallprotect_ticks"));
		}
		if (flyingPlayers.containsKey(p)) {
			endFlight(p, true);
		}
		
		enabledPlayers.remove(p);
		getLogger().info("Disabling flight for " + p.getName());
		p.sendMessage("Disabling FlyTime, you have " + (playerFlytimes.getLong(p.getName())/1000) + " seconds remaining.");

		p.setAllowFlight(false);
	}
	
	
	public void startFlight(Player p) {
		
		int taskId = getServer().getScheduler()
				.scheduleSyncRepeatingTask(this, new PlayerFlytimeNotifier(p), getConfig().getLong("notify_ticks"), getConfig().getLong("notify_ticks"));
		
		Integer disablerId = disablerIds.get(p);
		if (disablerId != null) {
			getServer().getScheduler()
				.cancelTask(disablerId);
		}
		
		notifierIds.put(p, taskId);
		getLogger().info("Starting flight for " + p.getName());
		if (fallingPlayers.contains(p)) {
			fallingPlayers.remove(p);
		}
		flyingPlayers.put(p, System.currentTimeMillis());
	}
	
	
	public boolean inOwnTown(Player p) {
		TownBlock block = TownyUniverse.getTownBlock(p.getLocation());
		try {
			if (block.getTown().getName().equals(
					TownyUniverse.getDataSource()
						.getResident(p.getName())
						.getTown().getName())) {
				return true;
			}
		} catch (NotRegisteredException|NullPointerException e) {
			return false;
		}
		return false;
	}
	
	public boolean hasFlightTime(Player p) {
		if (playerFlytimes.contains(p.getName())) {
			return playerFlytimes.getLong(p.getName()) > 0;
		}
		return false;
	}

	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("flytime")) { 
			
			if (args.length == 0) {
				
				// enable flight;
					Player p = (Player)sender;
					if (!p.hasPermission("flytime.townflight")) {
						sender.sendMessage("You do not have permission to use FlyTime.");
						return true;
					}
					if (enabledPlayers.contains(p)) {
						disableFlight(p);
					} else {
						if (!inOwnTown(p)) {
							sender.sendMessage("You can only fly in your own town.");
							return true;
						}
						if (!hasFlightTime(p)) {
							sender.sendMessage("You have no fly-time available");
							return true;
						}
						enableFlight(p);
					}
			} else {
				switch (args[0].toLowerCase()) {
				case "give":
					if (!sender.hasPermission("flytime.give")) {
						sender.sendMessage("You are not permitted to give away FlyTime.");
						break;
					}
					
					try {
						OfflinePlayer p = getServer().getOfflinePlayer(args[1]);
						long amt = Long.parseLong(args[2]);
						amt *= 1000;
						long total = addTime(p,amt);
						sender.sendMessage(p.getName() + " now has " + (total/1000) + " seconds of fly time.");
						if (p.isOnline()) {
							p.getPlayer().sendMessage("You have been given " + (amt/1000) + " seconds of FlyTime.");
							p.getPlayer().sendMessage("You now have " + (total/1000) + " seconds of FlyTime available.");
						}
					} catch (IndexOutOfBoundsException e) {
						sender.sendMessage("Usage: /flytime give <player> <seconds>");
					}
					break;
				}
				
			}
			return true;
		}
		return false;
	}
}
