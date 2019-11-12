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

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Does two things: 1) Concise way to pass the block hit information for
 * placement events. 2) For floating selection, emulates block hit information
 * would get if had clicked on face behind the floating selection. This means we
 * can use consistent logic downstream for everything that uses block hit info.
 *
 * Note that will do this even if floating selection if disabled but pass in
 * null values for hit information, so need to check floating selection before
 * calling this from an event that doesn't generate hit info, like clicking in
 * air. Such events generally display a GUI or have no effect if floating
 * selection is off.
 *
 * For excavations, inPos will be the same as onPos unless floating selection is
 * on.
 */
@API(status = Status.DEPRECATED)
@Deprecated
public class PlacementPosition {
    public final Direction onFace;
    public final BlockPos onPos;
    public final BlockPos inPos;
    public final double hitX;
    public final double hitY;
    public final double hitZ;
    public final boolean isFloating;

    /**
     *
     * @param player
     * @param onPos
     * @param onFace
     * @param hitVec
     * @param floatingSelectionRange zero means non-floating
     * @param isExcavation           if true will select *in* starting block vs *on*
     *                               it
     */
    public PlacementPosition(PlayerEntity player, BlockPos onPos, Direction onFace, Vec3d hitVec, int floatingSelectionRange, boolean isExcavation) {

        isFloating = floatingSelectionRange > 0;
        if (isFloating || onPos == null || onFace == null || hitVec == null) {

            final Vec3d start = player.getCameraPosVec(1);
            final Vec3d end = start.add(player.getRotationVector().multiply(floatingSelectionRange));

            inPos = new BlockPos(end);

            // have the position, now emulate which pos/face are we targeting
            // Do this by tracing towards the viewer from other side of block
            // to get the far-side hit. Hit coordinates are same irrespective
            // of face but need to the flip the face we get.
            final BlockHitResult hit = Blocks.DIRT.getDefaultState().getRayTraceShape(player.world, inPos)
                    .rayTrace(start.add(player.getRotationVector().multiply(10)), start, inPos);

            this.onPos = isExcavation ? inPos : inPos.offset(hit.getSide());
            this.onFace = hit.getSide().getOpposite();
            final Vec3d hitPos = hit.getPos();
            hitX = hitPos.x;
            hitY = hitPos.y;
            hitZ = hitPos.z;
        } else {
            this.onFace = onFace;
            this.onPos = onPos;
            hitX = hitVec.x;
            hitY = hitVec.y;
            hitZ = hitVec.z;
            inPos = isExcavation ? this.onPos : this.onPos.offset(this.onFace);
        }
    }
}
