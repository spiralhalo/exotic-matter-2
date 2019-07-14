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

package grondag.xm2.model.impl.primitive;

import static grondag.xm2.model.impl.state.ModelStateData.STATE_FLAG_NONE;

import java.util.function.Consumer;

import grondag.fermion.world.Rotation;
import grondag.xm2.mesh.helper.CubeInputs;
import grondag.xm2.mesh.polygon.IPolygon;
import grondag.xm2.mesh.stream.IPolyStream;
import grondag.xm2.mesh.stream.IWritablePolyStream;
import grondag.xm2.mesh.stream.PolyStreams;
import grondag.xm2.model.impl.state.ModelState;
import grondag.xm2.model.impl.state.StateFormat;
import grondag.xm2.painting.SurfaceTopology;
import grondag.xm2.surface.api.XmSurface;
import grondag.xm2.surface.impl.XmSurfaceImpl;
import grondag.xm2.surface.impl.XmSurfaceImpl.XmSurfaceListImpl;
import net.minecraft.util.math.Direction;

public class CubePrimitive extends AbstractModelPrimitive {
	public static final XmSurfaceListImpl SURFACES = XmSurfaceImpl.builder()
			.add("back", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.build();
	
	public static final XmSurfaceImpl SURFACE_ALL = SURFACES.get(0);
	
    /** never changes so may as well save it */
    private final IPolyStream cachedQuads;

    public CubePrimitive(String idString) {
        super(idString, SURFACES, StateFormat.BLOCK, STATE_FLAG_NONE);
        this.cachedQuads = getCubeQuads();
    }

    @Override
    public void produceQuads(ModelState modelState, Consumer<IPolygon> target) {
        if (cachedQuads.origin()) {
            IPolygon reader = cachedQuads.reader();

            do
                target.accept(reader);
            while (cachedQuads.next());
        }
    }

    private IPolyStream getCubeQuads() {
        CubeInputs cube = new CubeInputs();
        cube.color = 0xFFFFFFFF;
        cube.textureRotation = Rotation.ROTATE_NONE;
        cube.isFullBrightness = false;
        cube.u0 = 0;
        cube.v0 = 0;
        cube.u1 = 1;
        cube.v1 = 1;
        cube.isOverlay = false;
        cube.surface = SURFACE_ALL;

        IWritablePolyStream stream = PolyStreams.claimWritable();
        cube.appendFace(stream, Direction.DOWN);
        cube.appendFace(stream, Direction.UP);
        cube.appendFace(stream, Direction.EAST);
        cube.appendFace(stream, Direction.WEST);
        cube.appendFace(stream, Direction.NORTH);
        cube.appendFace(stream, Direction.SOUTH);

        IPolyStream result = stream.releaseAndConvertToReader();

        result.origin();
        assert result.reader().vertexCount() == 4;

        return result;
    }
}
