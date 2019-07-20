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
package grondag.hard_science.machines.base;

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.block.ISuperBlockAccess;
import grondag.exotic_matter.world.BlockCorner;
import grondag.exotic_matter.world.FarCorner;
import grondag.exotic_matter.world.IBlockTest;
import grondag.hard_science.simulator.transport.endpoint.IPortLayout;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class SuperBlockCableMatch implements IBlockTest {
    private final IPortLayout portLayout;
    private final int channel;

    /** pass in the info for the block you want to match */
    public SuperBlockCableMatch(IPortLayout portLayout, int channel) {
        this.portLayout = portLayout;
        this.channel = channel;
    }

    @Override
    public boolean testBlock(EnumFacing face, ISuperBlockAccess world, IBlockState ibs, BlockPos pos) {
        if (ibs.getBlock() instanceof IMachineBlock) {
            IPortLayout otherLayout = ((IMachineBlock) ibs.getBlock()).portLayout(world, pos, ibs);
            int otherChannel = ibs.getValue(ISuperBlock.META);

            return this.portLayout.couldConnect(face, this.channel, otherLayout, otherChannel);
        }
        return false;
    }

    @Override
    public boolean testBlock(BlockCorner corner, ISuperBlockAccess world, IBlockState ibs, BlockPos pos) {
        return false;
    }

    @Override
    public boolean testBlock(FarCorner corner, ISuperBlockAccess world, IBlockState ibs, BlockPos pos) {
        return false;
    }
}
