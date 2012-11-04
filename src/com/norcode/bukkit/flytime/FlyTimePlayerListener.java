package com.norcode.bukkit.flytime;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import com.avaje.ebean.config.dbplatform.SQLitePlatform;
import com.norcode.bukkit.flytime.FlyTimePlugin;


public class FlyTimePlayerListener implements Listener {
	private FlyTimePlugin plugin;
	
	public FlyTimePlayerListener(FlyTimePlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent ev) {
		Player p = ev.getPlayer();
		if (this.plugin.fallingPlayers.contains(p)) {
			if (ev.getFrom().getY() < ev.getTo().getY()) {
				plugin.getLogger().info(p.getName() + " was falling, but moved upward. not falling.");
				this.plugin.fallingPlayers.remove(p);
			}
		}
	}
	
	@EventHandler
	public void onPlayerFallDamage(EntityDamageEvent ev) {
		if (ev.getEntityType() == EntityType.PLAYER && ev.getCause() == DamageCause.FALL) {
			Player p = (Player)ev.getEntity();
			if (plugin.fallingPlayers.contains(p)) {
				ev.setCancelled(true);
				plugin.getLogger().info("Preventing " + Integer.toString(ev.getDamage()) + " Fall Damage.");
				plugin.fallingPlayers.remove(p);
			}
		}
	}
	
	@EventHandler
	public void onPlayerToggleFlight(PlayerToggleFlightEvent ev) {
		Player p = ev.getPlayer();
		if (!plugin.enabledPlayers.contains(p)) {
			return;
		}
		if (!ev.isFlying()) {
			plugin.endFlight(p);
		} else {
			plugin.startFlight(p);
		}
		
	}
}
