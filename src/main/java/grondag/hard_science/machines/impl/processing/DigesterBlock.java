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
package grondag.hard_science.machines.impl.processing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.artbox.ArtBoxTextures;
import grondag.hard_science.gui.ModGuiHandler.ModGui;
import grondag.hard_science.init.ModPortLayouts;
import grondag.hard_science.machines.base.AbstractMachine;
import grondag.hard_science.machines.base.MachineBlock;
import grondag.hard_science.simulator.transport.endpoint.PortLayout;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DigesterBlock extends MachineBlock {
    public DigesterBlock(String name) {
        super(name, ModGui.DIGESTER.ordinal(), MachineBlock.creatBasicMachineModelState(null, ArtBoxTextures.BORDER_CAUTION));
    }

    @Override
    public AbstractMachine createNewMachine() {
        return new DigesterMachine();
    }

    @Override
    public @Nullable TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
        return new DigesterTileEntity();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public TextureAtlasSprite getSymbolSprite() {
        return ArtBoxTextures.DECAL_BIG_DIAMOND.getSampleSprite();
    }

    @Override
    public PortLayout nominalPortLayout() {
        return ModPortLayouts.utb_low_carrier_all;
    }
}
