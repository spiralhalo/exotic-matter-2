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

package grondag.xm2.api.model;

import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_HAS_AXIS;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_HAS_AXIS_ORIENTATION;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_HAS_AXIS_ROTATION;

import java.util.function.Consumer;

import grondag.xm2.api.surface.XmSurfaceList;
import grondag.xm2.mesh.polygon.IPolygon;
import grondag.xm2.model.varia.BlockOrientationType;
import net.minecraft.util.Identifier;

public interface ModelPrimitive {
    /**
     * Used for registration and serialization of model state.
     */
    Identifier id();

    /**
     * Used for fast, transient serialization. Recommended that implementations
     * override this and cache value to avoid map lookups.
     */
    default int index() {
        return ModelPrimitiveRegistry.INSTANCE.indexOf(this);
    }

    /**
     * This convention is used by XM2 but 3rd-party primitives can use a different
     * one.
     */
    default String translationKey() {
        return "xm2_primitive_name." + id().getNamespace() + "." + id().getPath();
    }

    XmSurfaceList surfaces();

    /**
     * Override if shape has an orientation to be selected during placement.
     */
    default BlockOrientationType orientationType(ModelPrimitiveState modelState) {
        return BlockOrientationType.NONE;
    }

    int stateFlags(ModelPrimitiveState modelState);

    /**
     * Output polygons must be quads or tris. Consumer MUST NOT hold references to
     * any of the polys received.
     */
    void produceQuads(ModelState modelState, Consumer<IPolygon> target);

    // UGLY: really needed?
    /**
     * When ModelState primitive is set, the primitive-specific elements will be set
     * by applying this consumer to a mutable model state instance. Only need to
     * change if shape needs some preset state.
     */
    default void applyDefaultState(MutableModelState modelState) {
        // NOOP
    }

    /**
     * If true, shape can be placed on itself to become bigger.
     */
    default boolean isAdditive() {
        return false;
    }

    /**
     * Override to true for blocks like stairs and wedges. CubicPlacementHandler
     * will know they need to be placed in a corner instead of a face.
     */
    default boolean isAxisOrthogonalToPlacementFace() {
        return false;
    }

    default boolean hasAxis(ModelPrimitiveState modelState) {
        return (stateFlags(modelState) & STATE_FLAG_HAS_AXIS) == STATE_FLAG_HAS_AXIS;
    }

    default boolean hasAxisOrientation(ModelPrimitiveState modelState) {
        return (stateFlags(modelState) & STATE_FLAG_HAS_AXIS_ORIENTATION) == STATE_FLAG_HAS_AXIS_ORIENTATION;
    }

    default boolean hasAxisRotation(ModelPrimitiveState modelState) {
        return (stateFlags(modelState) & STATE_FLAG_HAS_AXIS_ROTATION) == STATE_FLAG_HAS_AXIS_ROTATION;
    }
}
