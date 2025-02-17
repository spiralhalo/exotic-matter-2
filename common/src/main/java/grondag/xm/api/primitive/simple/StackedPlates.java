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

package grondag.xm.api.primitive.simple;

import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import grondag.xm.Xm;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.modelstate.primitive.MutablePrimitiveState;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.texture.TextureOrientation;
import grondag.xm.orientation.api.OrientationType;

@Experimental
public class StackedPlates {
	private StackedPlates() { }

	public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
			.add("bottom", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.add("top", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
			.add("sides", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE).build();

	public static final XmSurface SURFACE_BOTTOM = SURFACES.get(0);
	public static final XmSurface SURFACE_TOP = SURFACES.get(1);
	public static final XmSurface SURFACE_SIDES = SURFACES.get(2);

	static final Function<PrimitiveState, XmMesh> POLY_FACTORY = modelState -> {
		final PolyTransform transform = PolyTransform.get(modelState);

		final WritableMesh mesh = XmMeshes.claimWritable();
		final MutablePolygon writer = mesh.writer();
		writer.colorAll(0, 0xFFFFFFFF);
		writer.lockUV(0, true);
		writer.rotation(0, TextureOrientation.IDENTITY);
		writer.sprite(0, "");
		writer.saveDefaults();

		final float height = getHeight(modelState) / 16;

		writer.surface(SURFACE_BOTTOM);
		writer.setupFaceQuad(Direction.DOWN, 0, 0, 1, 1, 0, Direction.NORTH);
		transform.accept(writer);
		writer.append();

		writer.surface(SURFACE_TOP);
		writer.setupFaceQuad(Direction.UP, 0, 0, 1, 1, 1 - height, Direction.NORTH);
		transform.accept(writer);
		writer.append();

		writer.surface(SURFACE_SIDES);
		writer.setupFaceQuad(Direction.EAST, 0, 0, 1, height, 0, Direction.UP);
		transform.accept(writer);
		writer.append();

		writer.surface(SURFACE_SIDES);
		writer.setupFaceQuad(Direction.WEST, 0, 0, 1, height, 0, Direction.UP);
		transform.accept(writer);
		writer.append();

		writer.surface(SURFACE_SIDES);
		writer.setupFaceQuad(Direction.NORTH, 0, 0, 1, height, 0, Direction.UP);
		transform.accept(writer);
		writer.append();

		writer.surface(SURFACE_SIDES);
		writer.setupFaceQuad(Direction.SOUTH, 0, 0, 1, height, 0, Direction.UP);
		transform.accept(writer);
		writer.append();

		return mesh.releaseToReader();
	};

	public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
			.surfaceList(SURFACES)
			.polyFactory(POLY_FACTORY)
			.orientationType(OrientationType.FACE)
			.primitiveBitCount(4)
			.build(Xm.id("stacked_plates"));

	/**
	 *
	 * @param height  1-16
	 * @param modelState
	 */
	public static void setHeight(int height, MutablePrimitiveState modelState) {
		modelState.primitiveBits(Mth.clamp(height, 1, 16) - 1);
	}

	public static int getHeight(PrimitiveState modelState) {
		return modelState.primitiveBits() + 1;
	}
}
