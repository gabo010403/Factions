package com.massivecraft.factions.integration;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.plugin.Plugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import com.griefcraft.lwc.LWC;
import com.griefcraft.lwc.LWCPlugin;
import com.massivecraft.factions.ConfServer;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.mcore.ps.PS;

public class LWCFeatures 
{
	private static LWC lwc;

	public static void setup()
	{
		Plugin test = Bukkit.getServer().getPluginManager().getPlugin("LWC");
		if(test == null || !test.isEnabled()) return;

		lwc = ((LWCPlugin)test).getLWC();
		Factions.get().log("Successfully hooked into LWC!"+(ConfServer.lwcIntegration ? "" : " Integration is currently disabled, though (\"lwcIntegration\")."));
	}

	public static boolean getEnabled()
	{
		return ConfServer.lwcIntegration && lwc != null;
	}

	public static void clearOtherChests(PS chunkPs, Faction faction)
	{
		Chunk chunk = null;
		try
		{
			chunk = chunkPs.asBukkitChunk(true);
		}
		catch (Exception e)
		{
			return;
		}
		
		BlockState[] blocks = chunk.getTileEntities();
		List<Block> chests = new LinkedList<Block>();
		
		for(int x = 0; x < blocks.length; x++)
		{
			if(blocks[x].getType() == Material.CHEST)
			{
				chests.add(blocks[x].getBlock());
			}
		}
		
		for(int x = 0; x < chests.size(); x++)
		{
			if(lwc.findProtection(chests.get(x)) != null)
			{
				if(!faction.getFPlayers().contains(FPlayer.get(lwc.findProtection(chests.get(x)).getOwner())))
					lwc.findProtection(chests.get(x)).remove();
			}
		}
	}
	
	public static void clearAllChests(PS chunkPs)
	{
		Chunk chunk = null;
		try
		{
			chunk = chunkPs.asBukkitChunk(true);
		}
		catch (Exception e)
		{
			return;
		}
		
		BlockState[] blocks = chunk.getTileEntities();
		List<Block> chests = new LinkedList<Block>();
		
		for (int x = 0; x < blocks.length; x++)
		{
			if(blocks[x].getType() == Material.CHEST)
			{
				chests.add(blocks[x].getBlock());
			}
		}
		
		for (int x = 0; x < chests.size(); x++)
		{
			if(lwc.findProtection(chests.get(x)) != null)
			{
				lwc.findProtection(chests.get(x)).remove();
			}
		}
	}
}
