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

package grondag.xm2.model.state;

import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_NEEDS_CORNER_JOIN;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_NEEDS_MASONRY_JOIN;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_NEEDS_SIMPLE_JOIN;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_NEEDS_SPECIES;
import static grondag.xm2.model.state.ModelStateData.TEST_GETTER_STATIC;

import grondag.fermion.varia.BitPacker32;
import grondag.xm2.Xm;
import grondag.xm2.api.connect.model.ClockwiseRotation;
import grondag.xm2.api.connect.state.CornerJoinState;
import grondag.xm2.api.connect.state.SimpleJoinState;
import grondag.xm2.api.connect.world.BlockNeighbors;
import grondag.xm2.api.model.ModelPrimitive;
import grondag.xm2.api.model.ModelPrimitiveRegistry;
import grondag.xm2.api.model.ModelState;
import grondag.xm2.api.model.MutableModelState;
import grondag.xm2.block.XmBlockRegistryImpl.XmBlockStateImpl;
import grondag.xm2.block.XmMasonryMatch;
import grondag.xm2.connect.CornerJoinStateSelector;
import grondag.xm2.mesh.helper.PolyTransform;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;

public class PrimitiveModelState extends AbstractWorldModelState implements MutableModelState {
    private static final BitPacker32<PrimitiveModelState> SHAPE_PACKER = new BitPacker32<PrimitiveModelState>(
            m -> m.shapeBits, (m, b) -> m.shapeBits = b);
    
    private static final BitPacker32<PrimitiveModelState>.EnumElement<Direction.Axis> AXIS = SHAPE_PACKER
            .createEnumElement(Direction.Axis.class);

    private static final BitPacker32<PrimitiveModelState>.BooleanElement AXIS_INVERTED = SHAPE_PACKER.createBooleanElement();
    
    private static final BitPacker32<PrimitiveModelState>.EnumElement<ClockwiseRotation> AXIS_ROTATION = SHAPE_PACKER.createEnumElement(ClockwiseRotation.class);
    
    private static final BitPacker32<PrimitiveModelState>.IntElement BLOCK_JOIN = SHAPE_PACKER
            .createIntElement(CornerJoinState.STATE_COUNT);
    
    private static final BitPacker32<PrimitiveModelState>.IntElement MASONRY_JOIN = SHAPE_PACKER
            .createIntElement(SimpleJoinState.STATE_COUNT);
    
    public static final int PRIMITIVE_BIT_COUNT = 6;
    
    private static final BitPacker32<PrimitiveModelState>.IntElement PRIMITIVE_BITS = SHAPE_PACKER
            .createIntElement(1 << PRIMITIVE_BIT_COUNT);
    
    static {
        assert SHAPE_PACKER.bitLength() <= 32;
        //TODO: remove
        System.out.println(SHAPE_PACKER.bitLength());
    }
    
    protected int shapeBits;

    // default to white, full alpha

    public PrimitiveModelState(ModelPrimitive primitive) {
        super(primitive);
    }

    public PrimitiveModelState(PrimitiveModelState template) {
        super(template.primitive);
        copyInternal(template);
    }
    
    @Override
    protected int intSize() {
        return super.intSize() + 1;
    }
    
    @Override
    protected <T extends AbstractModelState> void copyInternal(T template) {
        super.copyInternal(template);
        final PrimitiveModelState other = (PrimitiveModelState)template;
        this.shapeBits = other.shapeBits;
    }

    @Override
    public PrimitiveModelState mutableCopy() {
        return new PrimitiveModelState(this);
    }

    @Override
    public PrimitiveModelState copyFrom(ModelState templateIn) {
        copyInternal((PrimitiveModelState)templateIn);
        return this;
    }
    
    @Override
    protected void doSerializeToInts(int[] data, int startAt) {
        data[startAt] = shapeBits;
        super.doSerializeToInts(data, startAt + 1);
    }

    @Override
    protected void doDeserializeFromInts(int[] data, int startAt) {
        this.shapeBits = data[startAt];
        super.doDeserializeFromInts(data, startAt + 1);
    }
    
    @Override
    protected boolean equalsInner(Object obj) {
        final PrimitiveModelState other = (PrimitiveModelState)obj;
        return shapeBits == other.shapeBits
                && super.equalsInner(obj);
    }

    @Override
    protected int computeHashCode() {
        return super.computeHashCode() ^ HashCommon.mix(this.shapeBits);
    }

    @Override
    protected void doRefreshFromWorld(XmBlockStateImpl xmState, BlockView world, BlockPos pos) {
        super.doRefreshFromWorld(xmState, world, pos);

        final int stateFlags = stateFlags();

        BlockNeighbors neighbors = null;

        if ((STATE_FLAG_NEEDS_CORNER_JOIN & stateFlags) == STATE_FLAG_NEEDS_CORNER_JOIN) {
            neighbors = BlockNeighbors.claim(world, pos, TEST_GETTER_STATIC, xmState.blockJoinTest());
            BLOCK_JOIN.setValue(CornerJoinState.fromWorld(neighbors).ordinal(), this);

        } else if ((STATE_FLAG_NEEDS_SIMPLE_JOIN & stateFlags) == STATE_FLAG_NEEDS_SIMPLE_JOIN) {
            neighbors = BlockNeighbors.claim(world, pos, TEST_GETTER_STATIC, xmState.blockJoinTest());
            BLOCK_JOIN.setValue(SimpleJoinState.fromWorld(neighbors).ordinal(), this);
        }

        if ((STATE_FLAG_NEEDS_MASONRY_JOIN & stateFlags) == STATE_FLAG_NEEDS_MASONRY_JOIN) {
            if (neighbors == null) {
                neighbors = BlockNeighbors.claim(world, pos, TEST_GETTER_STATIC, XmMasonryMatch.INSTANCE);
            } else {
                neighbors.withTest(XmMasonryMatch.INSTANCE);
            }
            MASONRY_JOIN.setValue(SimpleJoinState.fromWorld(neighbors).ordinal(), this);
        }

        if (neighbors != null) {
            invalidateHashCode();
            neighbors.release();
        }
    }

    ////////////////////////////////////////////////////
    // PACKER 0 ATTRIBUTES (NOT SHAPE-DEPENDENT)
    ////////////////////////////////////////////////////

    @Override
    public Direction.Axis getAxis() {
        return AXIS.getValue(this);
    }

    @Override
    public void setAxis(Direction.Axis axis) {
        AXIS.setValue(axis, this);
        invalidateHashCode();
    }

    @Override
    public boolean isAxisInverted() {
        return AXIS_INVERTED.getValue(this);
    }

    @Override
    public void setAxisInverted(boolean isInverted) {
        AXIS_INVERTED.setValue(isInverted, this);
        invalidateHashCode();
    }


    ////////////////////////////////////////////////////
    // PACKER 3 ATTRIBUTES (BLOCK FORMAT)
    ////////////////////////////////////////////////////

    @Override
    public CornerJoinState cornerJoin() {
        return CornerJoinStateSelector.fromOrdinal(
                MathHelper.clamp(BLOCK_JOIN.getValue(this), 0, CornerJoinState.STATE_COUNT - 1));
    }

    @Override
    public void cornerJoin(CornerJoinState join) {
        BLOCK_JOIN.setValue(join.ordinal(), this);
        invalidateHashCode();
    }

    @Override
    public SimpleJoinState simpleJoin() {
        // If this state is using corner join, join index is for a corner join
        // and so need to derive simple join from the corner join
        final int stateFlags = stateFlags();
        return ((stateFlags & STATE_FLAG_NEEDS_CORNER_JOIN) == 0)
                ? SimpleJoinState.fromOrdinal(BLOCK_JOIN.getValue(this))
                : cornerJoin().simpleJoin();
    }

    @Override
    public void simpleJoin(SimpleJoinState join) {
        BLOCK_JOIN.setValue(join.ordinal(), this);
        invalidateHashCode();
    }

    @Override
    public SimpleJoinState masonryJoin() {
        return SimpleJoinState.fromOrdinal(MASONRY_JOIN.getValue(this));
    }

    @Override
    public void masonryJoin(SimpleJoinState join) {
        MASONRY_JOIN.setValue(join.ordinal(), this);
        invalidateHashCode();
    }

    @Override
    public ClockwiseRotation getAxisRotation() {
        return AXIS_ROTATION.getValue(this);
    }

    @Override
    public void setAxisRotation(ClockwiseRotation rotation) {
        AXIS_ROTATION.setValue(rotation, this);
        invalidateHashCode();
    }

    @Override
    public boolean hasSpecies() {
        final int stateFlags = stateFlags();
        return ((stateFlags & STATE_FLAG_NEEDS_SPECIES) == STATE_FLAG_NEEDS_SPECIES);
    }

    @Override
    public final boolean doShapeAndAppearanceMatch(ModelState other) {
        return primitive.doesShapeMatch(this, other) && doesAppearanceMatch(other);
    }
    
    @Override
    public Direction rotateFace(Direction face) {
        return PolyTransform.rotateFace(this, face);
    }

    public static PrimitiveModelState deserializeFromNBTIfPresent(CompoundTag tag) {
        ModelPrimitive shape = ModelPrimitiveRegistry.INSTANCE.get(tag.getString(ModelStateTagHelper.NBT_SHAPE));
        if(shape == null) {
            return null;
        }
        PrimitiveModelState result = new PrimitiveModelState(shape);
    
        if (tag.containsKey(ModelStateTagHelper.NBT_MODEL_BITS)) {
            int[] stateBits = tag.getIntArray(ModelStateTagHelper.NBT_MODEL_BITS);
            if (stateBits.length != 22) {
                Xm.LOG.warn("Bad or missing data encounter during ModelState NBT deserialization.");
            } else {
                result.deserializeFromInts(stateBits);
            }
        }
        
        // textures and vertex processors serialized by name because registered can
        // change if mods/config change
//        String layers = tag.getString(NBT_LAYERS);
//        if (layers.isEmpty()) {
//            String[] names = layers.split(",");
//            if (names.length != 0) {
//                int i = 0;
//                for (PaintLayer l : PaintLayer.VALUES) {
//                    if (ModelStateData.PAINT_TEXTURE[l.ordinal()].getValue(this) != 0) {
//                        TextureSet tex = TextureSetRegistryImpl.INSTANCE.getById(new Identifier(names[i++]));
//                        ModelStateData.PAINT_TEXTURE[l.ordinal()].setValue(tex.index(), this);
//                        if (i == names.length)
//                            break;
//                    }
//
//                    if (ModelStateData.PAINT_VERTEX_PROCESSOR[l.ordinal()].getValue(this) != 0) {
//                        VertexProcessor vp = VertexProcessors.get(names[i++]);
//                        ModelStateData.PAINT_VERTEX_PROCESSOR[l.ordinal()].setValue(vp.ordinal, this);
//                        if (i == names.length)
//                            break;
//                    }
//                }
//            }
//        }
        
        result.clearStateFlags();
        return result;
    }

    @Override
    public void serializeNBT(CompoundTag tag) {
        tag.putIntArray(ModelStateTagHelper.NBT_MODEL_BITS, this.serializeToInts());

        // shape is serialized by name because registered shapes can change if
        // mods/config change
        tag.putString(ModelStateTagHelper.NBT_SHAPE, this.primitive().id().toString());

        // TODO: serialization for paint/surface map
        // textures and vertex processors serialized by name because registered can
        // change if mods/config change
//        StringBuilder layers = new StringBuilder();
//       
//        if (layers.length() != 0)
//            tag.putString(NBT_LAYERS, layers.toString());
    }

    @Override
    public void fromBytes(PacketByteBuf pBuff) {
        super.fromBytes(pBuff);
        shapeBits = pBuff.readInt();
    }

    @Override
    public void toBytes(PacketByteBuf pBuff) {
        super.toBytes(pBuff);
        pBuff.writeInt(shapeBits);
    }

    @Override
    public boolean isImmutable() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutablePrimitiveModelState toImmutable() {
        return new ImmutablePrimitiveModelState(this);
    }
    
    public int primitiveBits() {
        return PRIMITIVE_BITS.getValue(this);
    }
    
    public void primitiveBits(int bits) {
        PRIMITIVE_BITS.setValue(bits, this);
    }
}
