/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.xm.api.terrain;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.modelstate.base.MutableBaseModelState;
import grondag.xm.terrain.TerrainState;

@Experimental
public interface TerrainModelState extends BaseModelState<TerrainModelState, TerrainModelState.Mutable> {
	public interface Mutable extends TerrainModelState, MutableBaseModelState<TerrainModelState, TerrainModelState.Mutable> {
		TerrainModelState.Mutable setTerrainStateKey(long terrainStateKey);

		TerrainModelState.Mutable setTerrainState(TerrainState flowState);
	}

	long getTerrainStateKey();

	int getTerrainHotness();

	TerrainState getTerrainState();
}
