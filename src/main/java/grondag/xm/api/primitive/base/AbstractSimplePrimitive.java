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
package grondag.xm.api.primitive.base;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Function;

import org.apiguardian.api.API;

import net.minecraft.util.Identifier;

import grondag.xm.api.modelstate.base.BaseModelStateFactory;
import grondag.xm.api.modelstate.primitive.MutablePrimitiveState;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurfaceList;

@API(status = EXPERIMENTAL)
public abstract class AbstractSimplePrimitive extends AbstractPrimitive<PrimitiveState, MutablePrimitiveState> implements SimplePrimitive {
	protected AbstractSimplePrimitive(Identifier id, int stateFlags, BaseModelStateFactory<PrimitiveState, MutablePrimitiveState> factory, Function<PrimitiveState, XmSurfaceList> surfaceFunc) {
		super(id, stateFlags, factory, surfaceFunc);
	}

	protected AbstractSimplePrimitive(String idString, int stateFlags, BaseModelStateFactory<PrimitiveState, MutablePrimitiveState> factory, Function<PrimitiveState, XmSurfaceList> surfaceFunc) {
		super(idString, stateFlags, factory, surfaceFunc);
	}
}
