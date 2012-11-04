package com.norcode.bukkit.flytime;

import org.bukkit.entity.Player;

public class FlyTimeChecker implements Runnable {
	private FlyTimePlugin plugin;
	
	public FlyTimeChecker(FlyTimePlugin plugin) {
		this.plugin = plugin;
	}
	@Override
	public void run() {
		Long lastCheck;
		Long elapsed;
		Long now = System.currentTimeMillis();
		for (Player p: this.plugin.flyingPlayers.keySet()) {
			if (!p.isOnline()) {
				plugin.getLogger().info(p.getName() + " disconnected. ending flight.");
				plugin.endFlight(p);
				continue;
			}
			Long playerTimeLeft = plugin.playerFlytimes.getLong(p.getName());
			
			lastCheck = this.plugin.flyingPlayers.get(p);
			
			elapsed = (now - lastCheck);
			
			Long remaining = playerTimeLeft - elapsed;
			if (remaining < 500) {
				remaining = 0l;
			}
			
			plugin.playerFlytimes.set(p.getName(), remaining);
			plugin.flyingPlayers.put(p, now);
			if (remaining == 0) {
				plugin.getLogger().info(p.getName() + " ran out of fly time, disabling flight.");
				plugin.endFlight(p);
				continue;
			}
			if (remaining <= 10000) {
				p.sendMessage("Warning: You have "+ Long.toString(remaining/1000) + " seconds of fly time remaining.");
			}
		}

	}

}
