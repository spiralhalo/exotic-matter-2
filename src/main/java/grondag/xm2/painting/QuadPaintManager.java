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

package grondag.xm2.painting;

import java.util.IdentityHashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;

import grondag.xm2.primitives.polygon.IMutablePolygon;
import grondag.xm2.primitives.polygon.IPolygon;
import grondag.xm2.primitives.stream.IMutablePolyStream;
import grondag.xm2.primitives.stream.PolyStreams;
import grondag.xm2.state.ModelState;

/**
 * Low-garbage consumer for quads from mesh generators that manages
 * instantiation and processing of painters and then passes through painted quad
 * to another consumer.
 *
 */
public class QuadPaintManager implements Consumer<IPolygon> {
    private static ThreadLocal<QuadPaintManager> managers = ThreadLocal.withInitial(() -> new QuadPaintManager());

    public static final QuadPaintManager get() {
        return managers.get();
    }

    private final IdentityHashMap<Surface, IMutablePolyStream> surfaces = new IdentityHashMap<Surface, IMutablePolyStream>();

    @Override
    public void accept(IPolygon poly) {
        IMutablePolyStream stream = surfaces.computeIfAbsent(poly.getSurface(), p -> PolyStreams.claimMutable(0));

        int address = stream.writerAddress();
        stream.appendCopy(poly);
        stream.moveEditor(address);
        IMutablePolygon editor = stream.editor();

        // expects all input polys to be single-layer
        assert editor.layerCount() == 1;

        // assign three layers for painting and then correct after paint occurs
        editor.setLayerCount(3);

        // should have no textures assigned at start
        assert editor.getTextureName(0) == null;
        assert editor.getTextureName(1) == null;
        assert editor.getTextureName(2) == null;

        // Copy generator UVs (quad and vertex)
        // from layer 0 to upper layers.
        float f = editor.getMinU(0);
        editor.setMinU(1, f);
        editor.setMinU(2, f);
        f = editor.getMaxU(0);
        editor.setMaxU(1, f);
        editor.setMaxU(2, f);
        f = editor.getMinV(0);
        editor.setMinV(1, f);
        editor.setMinV(2, f);
        f = editor.getMaxV(0);
        editor.setMaxV(1, f);
        editor.setMaxV(2, f);

        final int vertexCount = editor.vertexCount();
        for (int i = 0; i < vertexCount; i++) {
            int c = editor.spriteColor(i, 0);
            editor.spriteColor(i, 1, c);
            editor.spriteColor(i, 2, c);

            float u = editor.spriteU(i, 0);
            float v = editor.spriteV(i, 0);
            editor.sprite(i, 1, u, v);
            editor.sprite(i, 2, u, v);
        }
    }

    public void producePaintedQuads(final ModelState modelState, final boolean isItem, final Consumer<IPolygon> target) {
        for (Entry<Surface, IMutablePolyStream> entry : surfaces.entrySet()) {
            Surface surface = entry.getKey();
            IMutablePolyStream stream = entry.getValue();

            for (PaintLayer paintLayer : PaintLayer.VALUES)
                if (modelState.isLayerEnabled(paintLayer) && !surface.isLayerDisabled(paintLayer)
                        && stream.editorOrigin())
                    QuadPainterFactory.getPainter(modelState, surface, paintLayer).paintQuads(stream, modelState,
                            paintLayer);

            if (stream.editorOrigin()) {
                final IMutablePolygon editor = stream.editor();
                do {
                    // omit polys that weren't textured by any painter
                    if (editor.getTextureName(0) != null) {
                        final int layerCount = editor.getTextureName(1) == null ? 1
                                : editor.getTextureName(2) == null ? 2 : 3;

                        editor.setLayerCount(layerCount);
                    }

                    target.accept(editor);
                } while (stream.editorNext());
            }

            stream.release();
        }

        surfaces.clear();
    }
}
