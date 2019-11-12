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
package grondag.xm.render;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import net.minecraft.class_4604;

@API(status = INTERNAL)
public abstract class XmRenderHelper {
    private XmRenderHelper() {
    }

    private static class_4604 visibleRegion;
    private static float tickDelta;

    public static void visibleRegion(class_4604 region) {
        visibleRegion = region;
    }

    public static class_4604 visibleRegion() {
        return visibleRegion;
    }

    public static void tickDelta(float tickDeltaIn) {
        tickDelta = tickDeltaIn;
    }

    public static float tickDelta() {
        return tickDelta;
    }
}
