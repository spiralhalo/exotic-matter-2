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
package grondag.xm.api.texture;

import grondag.xm.texture.TextureLayoutMapImpl;

public interface TextureLayoutMap {
    static TextureLayoutMap create(TextureLayout layout, TextureNameFunction nameFunc) {
        return TextureLayoutMapImpl.create(layout, nameFunc);
    }

    public static TextureLayoutMap SINGLE = create(TextureLayout.SIMPLE, TextureNameFunction.SINGLE);

    public static TextureLayoutMap VERSIONED = create(TextureLayout.SIMPLE, TextureNameFunction.VERSIONED);

    public static TextureLayoutMap VERSION_X_8 = create(TextureLayout.SPLIT_X_8, TextureNameFunction.VERSION_X_8);

    public static TextureLayoutMap BORDER_13 = create(TextureLayout.BORDER_13, TextureNameFunction.BORDER_X_8);

    public static TextureLayoutMap BORDER_14 = create(TextureLayout.BORDER_14, TextureNameFunction.BORDER_X_8);

    public static TextureLayoutMap MASONRY_5 = create(TextureLayout.MASONRY_5, TextureNameFunction.MASONRY_X_8);

    public static TextureLayoutMap BIGTEX_ANIMATED = create(TextureLayout.BIGTEX_ANIMATED, TextureNameFunction.SINGLE);

    public static TextureLayoutMap QUADRANT_CONNECTED_SINGLE = create(TextureLayout.QUADRANT_CONNECTED, TextureNameFunction.SINGLE);

    public static TextureLayoutMap QUADRANT_CONNECTED_VERSIONED = create(TextureLayout.QUADRANT_CONNECTED, TextureNameFunction.SINGLE);

    TextureLayout layout();
}
