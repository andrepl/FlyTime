package com.norcode.bukkit.flytime;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Created with IntelliJ IDEA.
 * User: andre
 * Date: 6/7/13
 * Time: 7:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class GriefPreventionListener implements Listener {
    FlyTimePlugin plugin;
    public GriefPreventionListener(FlyTimePlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (plugin.enabledPlayers.contains(event.getPlayer().getName())) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                Player player = event.getPlayer();
                Claim claim = null;
                if (player.hasMetadata("flytime-griefprevention-cached-claim")) {
                    claim = (Claim) player.getMetadata("flytime-griefprevention-cached-claim").get(0).value();
                }
                claim = GriefPrevention.instance.dataStore.getClaimAt(event.getTo(),  true, claim);
                if (claim == null || claim.allowBuild(event.getPlayer()) != null) {
                    plugin.denyMove(event);
                }
            }
        }
    }

    public boolean onOwnLand(Player p) {
        Claim claim = null;
        if (p.hasMetadata("flytime-griefprevention-cached-claim")) {
            claim = (Claim) p.getMetadata("flytime-griefprevention-cached-claim").get(0).value();
        }
        claim = GriefPrevention.instance.dataStore.getClaimAt(p.getLocation(),  true, claim);
        if (claim.allowBuild(p) == null) {
            return true;
        }
        return false;
    }
}
