package com.massivecraft.factions.cmd;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.massivecraft.factions.BoardColl;
import com.massivecraft.factions.ConfServer;
import com.massivecraft.factions.FFlag;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayerColl;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Perm;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.integration.EssentialsFeatures;
import com.massivecraft.mcore.ps.PS;
import com.massivecraft.mcore.util.SmokeUtil;


public class CmdFactionsHome extends FCommand
{
	
	public CmdFactionsHome()
	{
		super();
		this.aliases.add("home");
		
		//this.requiredArgs.add("");
		//this.optionalArgs.put("", "");
		
		this.permission = Perm.HOME.node;
		
		senderMustBePlayer = true;
		senderMustBeMember = true;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		// TODO: Hide this command on help also.
		if ( ! ConfServer.homesEnabled)
		{
			fme.msg("<b>Sorry, Faction homes are disabled on this server.");
			return;
		}

		if ( ! ConfServer.homesTeleportCommandEnabled)
		{
			fme.msg("<b>Sorry, the ability to teleport to Faction homes is disabled on this server.");
			return;
		}
		
		if ( ! myFaction.hasHome())
		{
			fme.msg("<b>Your faction does not have a home. " + (fme.getRole().isLessThan(Rel.OFFICER) ? "<i> Ask your leader to:" : "<i>You should:"));
			fme.sendMessage(p.cmdBase.cmdFactionsSethome.getUseageTemplate());
			return;
		}
		
		if ( ! ConfServer.homesTeleportAllowedFromEnemyTerritory && fme.isInEnemyTerritory())
		{
			fme.msg("<b>You cannot teleport to your faction home while in the territory of an enemy faction.");
			return;
		}
		
		if ( ! ConfServer.homesTeleportAllowedFromDifferentWorld && me.getWorld().getUID() != myFaction.getHome().getWorld().getUID())
		{
			fme.msg("<b>You cannot teleport to your faction home while in a different world.");
			return;
		}
		
		
		Faction faction = BoardColl.get().getFactionAt(PS.valueOf(me));
		Location loc = me.getLocation().clone();
		
		// if player is not in a safe zone or their own faction territory, only allow teleport if no enemies are nearby
		if
		(
			ConfServer.homesTeleportAllowedEnemyDistance > 0
			&&
			faction.getFlag(FFlag.PVP)
			&&
			(
				! fme.isInOwnTerritory()
				||
				(
					fme.isInOwnTerritory()
					&&
					! ConfServer.homesTeleportIgnoreEnemiesIfInOwnTerritory
				)
			)
		)
		{
			World w = loc.getWorld();
			double x = loc.getX();
			double y = loc.getY();
			double z = loc.getZ();

			for (Player p : me.getServer().getOnlinePlayers())
			{
				if (p == null || !p.isOnline() || p.isDead() || p == me || p.getWorld() != w)
					continue;

				FPlayer fp = FPlayerColl.get().get(p);
				if (fme.getRelationTo(fp) != Rel.ENEMY)
					continue;

				Location l = p.getLocation();
				double dx = Math.abs(x - l.getX());
				double dy = Math.abs(y - l.getY());
				double dz = Math.abs(z - l.getZ());
				double max = ConfServer.homesTeleportAllowedEnemyDistance;

				// box-shaped distance check
				if (dx > max || dy > max || dz > max)
					continue;

				fme.msg("<b>You cannot teleport to your faction home while an enemy is within " + ConfServer.homesTeleportAllowedEnemyDistance + " blocks of you.");
				return;
			}
		}

		// if Essentials teleport handling is enabled and available, pass the teleport off to it (for delay and cooldown)
		if (EssentialsFeatures.handleTeleport(me, myFaction.getHome())) return;

		// if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
		if ( ! payForCommand(ConfServer.econCostHome, "to teleport to your faction home", "for teleporting to your faction home")) return;

		// Create a smoke effect
		if (ConfServer.homesTeleportCommandSmokeEffectEnabled)
		{
			List<Location> smokeLocations = new ArrayList<Location>();
			smokeLocations.add(loc);
			smokeLocations.add(loc.add(0, 1, 0));
			smokeLocations.add(myFaction.getHome());
			smokeLocations.add(myFaction.getHome().clone().add(0, 1, 0));
			SmokeUtil.spawnCloudRandom(smokeLocations, 3f);
		}

		me.teleport(myFaction.getHome());
	}
	
}
