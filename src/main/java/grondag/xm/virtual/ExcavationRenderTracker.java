/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.xm.virtual;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.IdentityHashMap;
import java.util.Map;

import org.apiguardian.api.API;

import grondag.fermion.sc.unordered.SimpleUnorderedArrayList;
import grondag.fermion.simulator.domain.DomainManager;
import grondag.fermion.world.WorldMap;
import grondag.xm.Xm;
import grondag.xm.XmConfig;
import grondag.xm.network.S2C_ExcavationRenderUpdate;
import grondag.xm.network.S2C_PacketExcavationRenderRefresh;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;

@SuppressWarnings("serial")
@API(status = INTERNAL)
public class ExcavationRenderTracker extends WorldMap<Int2ObjectOpenHashMap<ExcavationRenderEntry>> {

	public static final ExcavationRenderTracker INSTANCE = new ExcavationRenderTracker();

	private final IdentityHashMap<ServerPlayerEntity, PlayerData> playerTracking = new IdentityHashMap<>();

	@Override
	protected Int2ObjectOpenHashMap<ExcavationRenderEntry> load(int dimension) {
		return new Int2ObjectOpenHashMap<>();
	}

	public void add(ExcavationRenderTask task) {
		final ExcavationRenderEntry entry = new ExcavationRenderEntry(task);
		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("id = %d new Entry, valid=%s", entry.id, Boolean.toString(entry.isValid()));
		}
		if (entry.isValid()) {
			synchronized (this) {
				this.get(entry.rawDimensionId).put(entry.id, entry);
			}

			for (final Map.Entry<ServerPlayerEntity, PlayerData> playerEntry : playerTracking.entrySet()) {
				final PlayerData pd = playerEntry.getValue();
				if (pd.dimensionID == entry.rawDimensionId && task.visibleTo(playerEntry.getKey())) {
					if (XmConfig.logExcavationRenderTracking) {
						Xm.LOG.info("adding listeners for %s", playerEntry.getKey().getDisplayName());
					}
					entry.addListener(playerEntry.getKey(), true);
				}
			}
		}
	}

	public synchronized void remove(ExcavationRenderEntry entry) {
		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("id = %d removing excavation render entry", entry.id);
		}

		this.get(entry.rawDimensionId).remove(entry.id);

		final Packet<?> packet = S2C_ExcavationRenderUpdate.toPacket(entry.id);

		for (final Map.Entry<ServerPlayerEntity, PlayerData> playerEntry : playerTracking.entrySet()) {
			final PlayerData pd = playerEntry.getValue();
			if (pd.dimensionID == entry.rawDimensionId) {
				entry.removeListener(playerEntry.getKey());
				ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerEntry.getKey(), packet);
			}
		}
	}

	/**
	 * Call when player joins server, changes dimension or changes active domain.
	 */
	public void updatePlayerTracking(ServerPlayerEntity player) {
		if (XmConfig.logExcavationRenderTracking) {
			Xm.LOG.info("updatePlayerTracking for %s", player.getName());
		}

		final PlayerData newData = new PlayerData(player);
		final PlayerData oldData = playerTracking.get(player);

		if (oldData != null && oldData.dimensionID == newData.dimensionID && oldData.domainID == newData.domainID) {
			// no changes
			if (XmConfig.logExcavationRenderTracking) {
				Xm.LOG.info("updatePlayerTracking exit no changes");
			}
			return;
		}

		playerTracking.put(player, newData);

		// remove old listeners if needed
		if (oldData != null) {
			synchronized (this) {
				final Int2ObjectOpenHashMap<ExcavationRenderEntry> entries = this.get(oldData.dimensionID);
				if (entries != null && !entries.isEmpty()) {
					for (final ExcavationRenderEntry entry : entries.values()) {
						entry.removeListener(player);
					}
				}
			}
		}

		// build refresh
		final SimpleUnorderedArrayList<ExcavationRenderEntry> output = new SimpleUnorderedArrayList<>();
		synchronized (this) {
			final Int2ObjectOpenHashMap<ExcavationRenderEntry> entries = this.get(newData.dimensionID);
			if (entries != null && !entries.isEmpty()) {
				for (final ExcavationRenderEntry entry : entries.values()) {
					if(entry.task.visibleTo(player)) {
						entry.addListener(player, false);
						if (entry.isFirstComputeDone()) {
							output.add(entry);
						}
					}
				}
			}
		}
		ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, S2C_PacketExcavationRenderRefresh.toPacket(output));
	}

	public void stopPlayerTracking(ServerPlayerEntity player) {
		final PlayerData oldData = playerTracking.get(player);

		if (oldData == null)
			return;

		synchronized (this) {
			final Int2ObjectOpenHashMap<ExcavationRenderEntry> entries = this.get(oldData.dimensionID);
			if (entries != null && !entries.isEmpty()) {
				for (final ExcavationRenderEntry entry : entries.values()) {
					entry.removeListener(player);
				}
			}
		}

		playerTracking.remove(player);
	}

	private static class PlayerData {
		private final int domainID;
		private final int dimensionID;

		private PlayerData(ServerPlayerEntity player) {
			domainID = DomainManager.instance().getActiveDomain(player).getAssignedNumber();
			dimensionID = player.dimension.getRawId();
		}
	}

	@Override
	public void clear() {
		super.clear();
		playerTracking.clear();
	}
}
