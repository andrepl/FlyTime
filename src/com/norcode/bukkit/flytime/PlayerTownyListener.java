package com.norcode.bukkit.flytime;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.palmergames.bukkit.towny.event.PlayerChangePlotEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.WorldCoord;

public class PlayerTownyListener implements Listener {
	
	private FlyTimePlugin plugin;
	public PlayerTownyListener(FlyTimePlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerChangePlot(PlayerChangePlotEvent ev) {
		WorldCoord to = ev.getTo();
		Player p = ev.getPlayer();
		if (plugin.enabledPlayers.contains(p)) {
			Resident res;
			try {
				res = TownyUniverse.getDataSource().getResident(p.getName());
			} catch (NotRegisteredException e) {
				return;
			}
			Town town;
			try {
				town = to.getTownBlock().getTown();
				if (!res.getTown().getName().equals(town.getName())) {
					plugin.disableFlight(p);
				}
			} catch (NotRegisteredException e) {
				plugin.disableFlight(p);
				return;
			}
		}
	}
}
