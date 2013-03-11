package com.wolvencraft.yasp.db.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.wolvencraft.yasp.db.QueryResult;
import com.wolvencraft.yasp.db.QueryUtils;
import com.wolvencraft.yasp.db.tables.Detailed;
import com.wolvencraft.yasp.db.tables.Normal.TotalPVPKillsTable;
import com.wolvencraft.yasp.util.Util;

/**
 * Data collector that records all PVP statistics on the server for a specific player.
 * @author bitWolfy
 *
 */
public class PVPData {
	
	/**
	 * <b>Default constructor</b><br />
	 * Creates an empty data store to save the statistics until database synchronization.
	 */
	public PVPData(int playerId) {
		this.playerId = playerId;
		dynamicData = new ArrayList<TotalPVPEntry>();
	}

	private int playerId;
	private List<TotalPVPEntry> dynamicData;
	
	/**
	 * Returns the contents of the data store.<br />
	 * Asynchronous method; changes to the returned List will not affect the data store.
	 * @return Contents of the data store
	 */
	public List<TotalPVPEntry> get() {
		List<TotalPVPEntry> temp = new ArrayList<TotalPVPEntry>();
		for(TotalPVPEntry value : dynamicData) temp.add(value);
		return temp;
	}
	
	/**
	 * Returns the specific entry from the data store.<br />
	 * If the entry does not exist, it will be created.
	 * @param victimId ID of the victim in a PVP event
	 * @param weapon Weapon used in the event
	 * @return Corresponding entry
	 */
	public TotalPVPEntry get(int victimId, ItemStack weapon) {
		for(TotalPVPEntry entry : dynamicData) {
			if(entry.equals(victimId, weapon)) return entry;
		}
		TotalPVPEntry entry = new TotalPVPEntry(victimId, weapon);
		dynamicData.add(entry);
		return entry;
	}
	
	/**
	 * Synchronizes the data from the data store to the database, then removes it.<br />
	 * If an entry was not synchronized, it will not be removed.
	 */
	public void sync() {
		for(TotalPVPEntry entry : get()) {
			if(entry.pushData(playerId)) dynamicData.remove(entry);
		}
	}
	
	/**
	 * Represents an entry in the PVP data store.
	 * It is dynamic, i.e. it can be edited once it has been created.
	 * @author bitWolfy
	 *
	 */
	public class TotalPVPEntry implements _NormalData {
		
		/**
		 * <b>Default constructor</b><br />
		 * Creates a new TotalPVP object based on the killer and victim in question
		 * @param killer Player who killed the victim
		 * @param victim Player who was killed
		 */
		public TotalPVPEntry(int victimId, ItemStack weapon) {
			this.victimId = victimId;
			this.weapon = weapon;
			this.times = 0;
		}
		
		private int victimId;
		private ItemStack weapon;
		private int times;
		
		@Override
		public void fetchData(int killerId) {
			List<QueryResult> results = QueryUtils.select(
				TotalPVPKillsTable.TableName.toString(),
				new String[] {"*"},
				new String[] { TotalPVPKillsTable.PlayerId.toString(), killerId + ""},
				new String[] { TotalPVPKillsTable.VictimId.toString(), victimId + ""},
				new String[] { TotalPVPKillsTable.MaterialId.toString(), weapon.getTypeId() + ""},
				new String[] { TotalPVPKillsTable.MaterialData.toString(), weapon.getData().getData() + ""}
			);
			if(results.isEmpty()) QueryUtils.insert(TotalPVPKillsTable.TableName.toString(), getValues(killerId));
			else {
				times = results.get(0).getValueAsInteger(TotalPVPKillsTable.Times.toString());
			}
		}

		@Override
		public boolean pushData(int killerId) {
			return QueryUtils.update(
				TotalPVPKillsTable.TableName.toString(),
				getValues(killerId), 
				new String[] { TotalPVPKillsTable.PlayerId.toString(), killerId + ""},
				new String[] { TotalPVPKillsTable.VictimId.toString(), victimId + ""},
				new String[] { TotalPVPKillsTable.MaterialId.toString(), weapon.getTypeId() + ""},
				new String[] { TotalPVPKillsTable.MaterialData.toString(), weapon.getData().getData() + ""}
			);
		}

		@Override
		public Map<String, Object> getValues(int killerId) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(TotalPVPKillsTable.PlayerId.toString(), killerId);
			map.put(TotalPVPKillsTable.VictimId.toString(), victimId);
			map.put(TotalPVPKillsTable.MaterialId.toString(), weapon.getTypeId());
			map.put(TotalPVPKillsTable.MaterialData.toString(), weapon.getData().getData());
			map.put(TotalPVPKillsTable.Times.toString(), times);
			return map;
		}
		
		/**
		 * Matches data provided in the arguments with the one in the entry.
		 * @param victimId ID of the victim
		 * @param weapon Weapon used in the PVP event
		 * @return <b>true</b> if the data matches, <b>false</b> otherwise.
		 */
		public boolean equals(int victimId, ItemStack weapon) {
			return this.victimId == victimId && this.weapon.equals(weapon);
		}
		
		/**
		 * Increments the number of times the victim was killed
		 */
		public void addTimes() { times++; }
		
	}
	
	/**
	 * Represents an entry in the Detailed data store.
	 * It is static, i.e. it cannot be edited once it has been created.
	 * @author bitWolfy
	 *
	 */
	public class DetailedPVPEntry implements _DetailedData {
		
		/**
		 * <b>Default constructor</b><br />
		 * Creates a new DetailedPVPEntry based on the data provided
		 * @param location
		 * @param victimId
		 * @param weapon
		 */
		public DetailedPVPEntry(Location location, int victimId, ItemStack weapon) {
			this.victimId = victimId;
			this.weapon = weapon;
			this.location = location;
			this.timestamp = Util.getTimestamp();
		}
		
		private int victimId;
		private ItemStack weapon;
		private Location location;
		private long timestamp;
		
		@Override
		public boolean pushData(int killerId) {
			return QueryUtils.insert(
				Detailed.PVPKills.TableName.toString(),
				getValues(killerId)
			);
		}

		@Override
		public Map<String, Object> getValues(int killerId) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(Detailed.PVPKills.KillerID.toString(), killerId);
			map.put(Detailed.PVPKills.VictimID.toString(), victimId);
			map.put(Detailed.PVPKills.MaterialID.toString(), weapon.getTypeId());
			map.put(Detailed.PVPKills.MaterialData.toString(), weapon.getData().getData());
			map.put(Detailed.PVPKills.World.toString(), location.getWorld().getName());
			map.put(Detailed.PVPKills.XCoord.toString(), location.getBlockX());
			map.put(Detailed.PVPKills.YCoord.toString(), location.getBlockY());
			map.put(Detailed.PVPKills.ZCoord.toString(), location.getBlockZ());
			map.put(Detailed.PVPKills.Timestamp.toString(), timestamp);
			return map;
		}

	}
	
}