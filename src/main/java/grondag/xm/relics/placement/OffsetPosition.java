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
package grondag.xm.relics.placement;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

/**
 * Alternate offsets directions for block placement regions. Used to find
 * regsions that don't contain obstacles.
 */
@API(status = Status.DEPRECATED)
@Deprecated
public enum OffsetPosition {
    FLIP_NONE(1, 1, 1), FLIP_WIDTH(-1, 1, 1), FLIP_DEPTH(1, -1, 1), FLIP_BOTH(-1, -1, 1), FLIP_HEIGHT(1, 1, -1);

    public final int widthFactor;
    public final int depthFactor;
    public final int heightFactor;

    /**
     * Contains all values except the default value.
     */
    public static final OffsetPosition[] ALTERNATES = { FLIP_WIDTH, FLIP_DEPTH, FLIP_BOTH, FLIP_HEIGHT };

    private OffsetPosition(int widthFactor, int depthFactor, int heightFactor) {
        this.widthFactor = widthFactor;
        this.depthFactor = depthFactor;
        this.heightFactor = heightFactor;
    }
}
