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
package grondag.xm.api.collision;

import com.google.common.collect.ImmutableList;

import grondag.xm.api.modelstate.ModelState;
import grondag.xm.collision.CollisionDispatcherImpl;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

public class CollisionDispatcher {
    private CollisionDispatcher() {}
    
    public static ImmutableList<Box> boxesFor(ModelState modelState) {
        return CollisionDispatcherImpl.boxesFor(modelState);
    }

    public static VoxelShape shapeFor(ModelState modelState) {
        return CollisionDispatcherImpl.shapeFor(modelState);
    }
}
