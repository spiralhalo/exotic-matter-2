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

import java.util.function.BooleanSupplier;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import grondag.fermion.position.BlockRegion;
import grondag.fermion.position.SingleBlockRegion;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

@API(status = Status.DEPRECATED)
@Deprecated
public class SingleBlockPlacementSpec extends SingleStackPlacementSpec {
    public SingleBlockPlacementSpec(ItemStack placedStack, PlayerEntity player, PlacementPosition pPos) {
        super(placedStack, player, pPos);
    }

    @Override
    protected boolean doValidate() {
        if (!this.player.world.isChunkLoaded(this.pPos.inPos) || World.isHeightInvalid(this.pPos.inPos))
            return false;

        if (isExcavation)
            return !player.world.isAir(pPos.inPos);
        else {
            if (player.world.getBlockState(pPos.inPos).getMaterial().isReplaceable()) {
                outputStack = PlacementHandler.cubicPlacementStack(this);
                return true;
            } else
                return false;
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder) {
        // NOOP - selection mode not meaningful for a single-block region
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode) {
        switch (previewMode) {
        case EXCAVATE:

            final Box box = new Box(pPos.inPos);

            // draw edges without depth to show extent of region
            GlStateManager.disableDepthTest();
            GlStateManager.lineWidth(2.0F);
            bufferBuilder.begin(GL11.GL_LINE_STRIP, VertexFormats.POSITION_COLOR);
            // TODO: confirm this is right method - names changed
            WorldRenderer.buildBoxOutline(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, previewMode.red, previewMode.green,
                    previewMode.blue, 1f);
            tessellator.draw();

            // draw sides with depth to better show what parts are unobstructed
            GlStateManager.enableDepthTest();

            bufferBuilder.begin(GL11.GL_TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
            // TODO: Fix for render changes
//            WorldRenderer.buildBox(bufferBuilder, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, previewMode.red, previewMode.green,
//                    previewMode.blue, 0.4f);
//            tessellator.draw();

            break;
        case PLACE:
            drawPlacementPreview(tessellator, bufferBuilder);
            break;

        case SELECT:
        case OBSTRUCTED:
        default:
            break;

        }
    }

    @Override
    public BooleanSupplier worldTask(ServerPlayerEntity player) {
        if (isExcavation)
            return new BooleanSupplier() {

                @Override
                public boolean getAsBoolean() {

                    World world = player.world;

                    BlockPos pos = SingleBlockPlacementSpec.this.pPos.inPos;
                    if (pos == null)
                        return false;

                    // is the position inside the world?
                    if (World.isValid(pos) || !world.isChunkLoaded(pos))
                        return false;

                    BlockState blockState = world.getBlockState(pos);

                    // is the block at the position affected
                    // by this excavation?
                    if (SingleBlockPlacementSpec.this.effectiveFilterMode.shouldAffectBlock(blockState, world, pos, SingleBlockPlacementSpec.this.placedStack(),
                            SingleBlockPlacementSpec.this.isVirtual)) {
//                        Job job = new Job(RequestPriority.MEDIUM, player);
//                        job.setDimension(world.dimension);
//                        job.addTask(new ExcavationTask(pos));
//                        IDomain domain = DomainManager.instance().getActiveDomain(player);
//                        if (domain != null) {
//                            domain.getCapability(JobManager.class).addJob(job);
//                        }
                    }
                    return false;

                // is the position inside the world?
                if (!world.isHeightValidAndBlockLoaded(pos))
                    return false;

                final BlockState blockState = world.getBlockState(pos);

                // is the block at the position affected
                // by this excavation?
                if (SingleBlockPlacementSpec.this.effectiveFilterMode.shouldAffectBlock(blockState, world, pos, SingleBlockPlacementSpec.this.placedStack(),
                        SingleBlockPlacementSpec.this.isVirtual)) {
                    //                        Job job = new Job(RequestPriority.MEDIUM, player);
                    //                        job.setDimension(world.dimension);
                    //                        job.addTask(new ExcavationTask(pos));
                    //                        IDomain domain = DomainManager.instance().getActiveDomain(player);
                    //                        if (domain != null) {
                    //                            domain.getCapability(JobManager.class).addJob(job);
                    //                        }
                }
                return false;
            }

        };
        else
            // Placement world task places virtual blocks in the currently active build
            return new BooleanSupplier() {

            @Override
            public boolean getAsBoolean() {

                //                    Build build = BuildManager.getActiveBuildForPlayer(player);
                //                    if (build == null || !build.isOpen()) {
                //                        String chatMessage = I18n.translate("placement.message.no_build");
                //                        player.sendMessage(new TranslatableText(chatMessage));
                //                        return false;
                //                    }

                final World world = player.world;

                final BlockPos pos = SingleBlockPlacementSpec.this.pPos.inPos;
                if (pos == null)
                    return false;

                    // is the position inside the world?
                    if (World.isValid(pos) || !world.isChunkLoaded(pos))
                        return false;

                final BlockState blockState = world.getBlockState(pos);

                // is the block at the position affected
                // by this excavation?
                if (SingleBlockPlacementSpec.this.effectiveFilterMode.shouldAffectBlock(blockState, world, pos, SingleBlockPlacementSpec.this.placedStack(),
                        SingleBlockPlacementSpec.this.isVirtual)) {
                    PlacementHandler.placeVirtualBlock(world, SingleBlockPlacementSpec.this.outputStack, player, pos); //, build);
                }
                return false;
            }
        };
    }

    @Override
    public BlockRegion region() {
        return new SingleBlockRegion(pPos.inPos);
    }
}
