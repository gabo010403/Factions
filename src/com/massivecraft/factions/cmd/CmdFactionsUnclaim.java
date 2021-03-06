package com.massivecraft.factions.cmd;

import org.bukkit.Bukkit;

import com.massivecraft.factions.ConfServer;
import com.massivecraft.factions.event.LandUnclaimEvent;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.integration.SpoutFeatures;
import com.massivecraft.factions.BoardColl;
import com.massivecraft.factions.FPerm;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.Perm;
import com.massivecraft.mcore.ps.PS;

public class CmdFactionsUnclaim extends FCommand
{
	public CmdFactionsUnclaim()
	{
		this.aliases.add("unclaim");
		this.aliases.add("declaim");
		
		//this.requiredArgs.add("");
		//this.optionalArgs.put("", "");
		
		this.permission = Perm.UNCLAIM.node;
		
		senderMustBePlayer = true;
		senderMustBeMember = false;
		senderMustBeOfficer = false;
		senderMustBeLeader = false;
	}
	
	@Override
	public void perform()
	{
		PS chunk = PS.valueOf(me).getChunk(true);
		Faction otherFaction = BoardColl.get().getFactionAt(chunk);

		if ( ! FPerm.TERRITORY.has(sender, otherFaction, true)) return;

		LandUnclaimEvent unclaimEvent = new LandUnclaimEvent(chunk, otherFaction, fme);
		Bukkit.getServer().getPluginManager().callEvent(unclaimEvent);
		if(unclaimEvent.isCancelled()) return;
	
		//String moneyBack = "<i>";
		if (Econ.shouldBeUsed())
		{
			double refund = Econ.calculateClaimRefund(myFaction.getLandRounded());
			
			if(ConfServer.bankEnabled && ConfServer.bankFactionPaysLandCosts)
			{
				if ( ! Econ.modifyMoney(myFaction, refund, "to unclaim this land", "for unclaiming this land")) return;
			}
			else
			{
				if ( ! Econ.modifyMoney(fme      , refund, "to unclaim this land", "for unclaiming this land")) return;
			}
		}

		BoardColl.get().removeAt(chunk);
		SpoutFeatures.updateTerritoryDisplayLoc(chunk);
		myFaction.msg("%s<i> unclaimed some land.", fme.describeTo(myFaction, true));

		if (ConfServer.logLandUnclaims)
		{
			Factions.get().log(fme.getName()+" unclaimed land at ("+chunk.getChunkX()+","+chunk.getChunkZ()+") from the faction: "+otherFaction.getTag());
		}
	}
	
}
