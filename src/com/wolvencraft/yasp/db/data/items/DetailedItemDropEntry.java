/*
 * DetailedDroppedItemsEntry.java
 * 
 * Statistics
 * Copyright (C) 2013 bitWolfy <http://www.wolvencraft.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.wolvencraft.yasp.db.data.items;

import lombok.AccessLevel;
import lombok.Getter;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.wolvencraft.yasp.db.Query;
import com.wolvencraft.yasp.db.data.DetailedData;
import com.wolvencraft.yasp.db.tables.Detailed.DroppedItems;
import com.wolvencraft.yasp.util.Util;
import com.wolvencraft.yasp.util.cache.MaterialCache;

/**
 * Represents an entry in the Detailed data store.
 * It is static, i.e. it cannot be edited once it has been created.
 * @author bitWolfy
 *
 */
@Getter(AccessLevel.PUBLIC)
public class DetailedItemDropEntry extends DetailedData {
    
    private ItemStack stack;
    private Location location;
    private long timestamp;

    /**
     * <b>Default constructor</b><br />
     * Creates a new DetailedDroppedItemsEntry based on the data provided
     * @param location Location of the item
     * @param stack Item stack
     */
    public DetailedItemDropEntry(Location location, ItemStack stack) {
        this.stack = stack.clone();
        this.stack.setAmount(1);
        this.location = location.clone();
        timestamp = Util.getTimestamp();
    }
    
    @Override
    public boolean pushData(int playerId) {
        return Query.table(DroppedItems.TableName)
                .value(DroppedItems.PlayerId, playerId)
                .value(DroppedItems.MaterialId, MaterialCache.parse(stack))
                .value(DroppedItems.World, location.getWorld().getName())
                .value(DroppedItems.XCoord, location.getBlockX())
                .value(DroppedItems.YCoord, location.getBlockY())
                .value(DroppedItems.ZCoord, location.getBlockZ())
                .value(DroppedItems.Timestamp, timestamp)
                .insert();
    }

}
