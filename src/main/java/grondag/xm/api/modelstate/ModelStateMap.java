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
package grondag.xm.api.modelstate;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import grondag.xm.api.connect.world.BlockNeighbors;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@API(status = EXPERIMENTAL)
@FunctionalInterface
public interface ModelStateMap<T, V extends ModelState.Mutable> {
    V apply(T blockState);
    
    @FunctionalInterface
    public static interface Modifier<T, V extends ModelState.Mutable> extends ModelStateUpdate<V> {
        V apply(V modelState, T blockState);
        
        @Override
        default void accept(V modelState, BlockState blockState, @Nullable BlockView world, @Nullable BlockPos pos, @Nullable BlockNeighbors neighbors, boolean refreshFromWorld) {
            apply(modelState, blockState);
        }
    }
}
