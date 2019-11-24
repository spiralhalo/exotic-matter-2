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
package grondag.xm.modelstate;

import static grondag.xm.api.modelstate.ModelStateFlags.BLOCK_SPECIES;
import static grondag.xm.api.modelstate.ModelStateFlags.CORNER_JOIN;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apiguardian.api.API;

import com.google.common.collect.ImmutableList;

import grondag.fermion.bits.BitPacker32;
import grondag.fermion.varia.Useful;
import grondag.xm.Xm;
import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.ModelStateFlags;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.modelstate.base.BaseModelStateFactory;
import grondag.xm.api.modelstate.base.MutableBaseModelState;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.paint.XmPaint;
import grondag.xm.api.paint.XmPaintRegistry;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.connect.CornerJoinStateSelector;
import grondag.xm.painter.PaintManager;
import grondag.xm.texture.TextureSetHelper;
import it.unimi.dsi.fastutil.HashCommon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
@API(status = INTERNAL)
public abstract class AbstractPrimitiveModelState
<V extends AbstractPrimitiveModelState<V, R, W>, R extends BaseModelState<R, W>, W extends MutableBaseModelState<R,W>>
extends AbstractModelState
implements MutableModelState, BaseModelState<R, W>, MutableBaseModelState<R, W>
{

	////////////////////////////////////////// BIT-WISE ENCODING //////////////////////////////////////////

	/** note that sign bit on world encoder is reserved to persist static state during serialization */
	private static final BitPacker32<AbstractPrimitiveModelState> WORLD_ENCODER = new BitPacker32<>(m -> m.worldBits,(m, b) -> m.worldBits = b);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement POS_X = WORLD_ENCODER.createIntElement(256);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement POS_Y = WORLD_ENCODER.createIntElement(256);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement POS_Z = WORLD_ENCODER.createIntElement(256);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement SPECIES = WORLD_ENCODER.createIntElement(16);


	public static final int PRIMITIVE_BIT_COUNT;
	private static final BitPacker32<AbstractPrimitiveModelState> SHAPE_ENCODER = new BitPacker32<>(m -> m.shapeBits,(m, b) -> m.shapeBits = b);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement ORIENTATION = SHAPE_ENCODER.createIntElement(32);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement BLOCK_JOIN = SHAPE_ENCODER.createIntElement(CornerJoinState.STATE_COUNT);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement MASONRY_JOIN = SHAPE_ENCODER.createIntElement(SimpleJoinState.STATE_COUNT);
	private static final BitPacker32<AbstractPrimitiveModelState>.IntElement PRIMITIVE_BITS;

	static {
		assert WORLD_ENCODER.bitLength() <= 32;
		assert SHAPE_ENCODER.bitLength() <= 32;
		PRIMITIVE_BIT_COUNT = 32 - SHAPE_ENCODER.bitLength();
		PRIMITIVE_BITS = SHAPE_ENCODER.createIntElement(1 << PRIMITIVE_BIT_COUNT);
		assert SHAPE_ENCODER.bitLength() <= 32;
	}

	static Consumer<AbstractPrimitiveModelState> clientClearHandler = s -> {};

	public static void useClientHandler() {
		clientClearHandler = s -> s.clearRendering();
	}

	////////////////////////////////////////// FACTORY //////////////////////////////////////////

	public static class ModelStateFactoryImpl<T extends AbstractPrimitiveModelState<T, R, W>, R extends BaseModelState<R, W>, W extends MutableBaseModelState<R,W>>
	implements BaseModelStateFactory<R, W>
	{
		private final ArrayBlockingQueue<T> POOL = new ArrayBlockingQueue<>(4096);

		private final Supplier<T> factory;

		public ModelStateFactoryImpl(Supplier<T> factory) {
			this.factory = factory;
		}

		public final T claimInner(ModelPrimitive<R, W> primitive) {
			T result = POOL.poll();
			if (result == null) {
				result = factory.get();
				result.isImmutable = false;
			} else {
				result.clear();
			}
			result.primitive = primitive;
			result.retain();
			return result;
		}

		protected final T claimInner(T template) {
			T result = POOL.poll();
			if (result == null) {
				result = factory.get();
				result.isImmutable = false;
			} else {
				result.clear();
			}
			result.retain();
			result.primitive = template.primitive;
			result.copyInternal(template);
			return result;
		}

		@Override
		public final W claim(ModelPrimitive<R, W> primitive) {
			return (W) claimInner(primitive);
		}

		public final W claim(R template, ModelPrimitive<R, W> primitive) {
			final W result = claim(primitive);
			result.copyFrom(template);
			return result;
		}

		public final W claim(R template) {
			return claim(template, template.primitive());
		}

		@Override
		public final W fromTag(ModelPrimitive<R, W> shape, CompoundTag tag) {
			final T result = claimInner(shape);

			if (tag.contains(ModelStateTagHelper.NBT_MODEL_BITS)) {
				final int[] stateBits = tag.getIntArray(ModelStateTagHelper.NBT_MODEL_BITS);
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
			return (W) result;
		}

		@Override
		public final W fromBuffer(ModelPrimitive<R, W> shape, PacketByteBuf buf) {
			final T result = claimInner(shape);
			result.fromBytes(buf);
			return (W) result;
		}
	}

	////////////////////////////////////////// GENERAL DECLARATIONS //////////////////////////////////////////

	@Override
	protected int computeHashCode() {
		int result = 0;
		final int limit = this.surfaceCount();
		for (int i = 0; i < limit; i++) {
			result ^= paints[i];
		}
		return result ^ HashCommon.mix(this.shapeBits | (this.worldBits << 32));
	}

	protected abstract ModelStateFactoryImpl<V, R, W> factoryImpl();

	@Override
	public final BaseModelStateFactory<R, W> factory() {
		return factoryImpl();
	}

	@Override
	public final <T> T applyAndRelease(Function<ModelState, T> func) {
		final T result = func.apply(this);
		release();
		return result;
	}

	@Override
	public final W apply(Consumer<W> consumer) {
		final W self = (W)this;
		consumer.accept(self);
		return self;
	}

	@Override
	protected final void onLastRelease() {
		factoryImpl().POOL.offer((V)this);
	}

	@Override
	public R toImmutable() {
		if(isImmutable)
			return (R)this;
		else {
			final V result = factoryImpl().claimInner((V)this);
			result.isImmutable = true;
			return (R)result;
		}
	}

	protected <T extends AbstractPrimitiveModelState> void copyInternal(T template) {
		final AbstractPrimitiveModelState other = template;
		System.arraycopy(template.paints, 0, this.paints, 0, this.surfaceCount());
		worldBits = other.worldBits;
		shapeBits = other.shapeBits;
	}

	protected void clear() {
		Arrays.fill(paints, 0);
		worldBits = 0;
		shapeBits = 0;
		clearStateFlags();
		invalidateHashCode();
		clientClearHandler.accept(this);
	}

	@Override
	public W copyFrom(ModelState template) {
		copyInternal((V)template);
		return (W)this;
	}

	@Override
	public R releaseToImmutable() {
		final R result = this.toImmutable();
		release();
		return result;
	}

	@Override
	public W mutableCopy() {
		return (W) factoryImpl().claimInner((V)this);
	}

	////////////////////////////////////////// COMPARISON //////////////////////////////////////////

	/**
	 * Does NOT consider isStatic in comparison.<p>
	 *
	 * {@inheritDoc}
	 */
	@Override
	public final boolean equals(Object obj) {
		return this == obj ? true : obj != null && obj.getClass() == this.getClass() && equalsInner(obj);
	}

	protected boolean equalsInner(Object obj) {
		final AbstractPrimitiveModelState other = (AbstractPrimitiveModelState) obj;
		return this.primitive == other.primitive
				&& this.worldBits == other.worldBits
				&& this.shapeBits == other.shapeBits
				&& doPaintsMatchNative(other);
	}

	//    @Override
	//    public final boolean equalsIncludeStatic(Object obj) {
	//        if (this == obj)
	//            return true;
	//
	//        if (obj instanceof AbstractPrimitiveModelState) {
	//            AbstractPrimitiveModelState other = (AbstractPrimitiveModelState) obj;
	//            return this.isStatic == other.isStatic && equalsInner(other);
	//        } else {
	//            return false;
	//        }
	//    }

	/**
	 * Returns true if visual elements and geometry match. Does not consider species
	 * in matching.
	 */
	@Override
	public final boolean doShapeAndAppearanceMatch(ModelState other) {
		if(other.getClass() != this.getClass())
			return false;
		return primitive.doesShapeMatch((R)this, (R)other) && doesAppearanceMatch(other);
	}

	/**
	 * Returns true if visual elements match. Does not consider species or geometry
	 * in matching.
	 */
	@Override
	public final boolean doesAppearanceMatch(ModelState other) {
		return other != null && doPaintsMatch(other);
	}

	////////////////////////////////////////// SERIALIZATION //////////////////////////////////////////

	@Override
	protected int intSize() {
		return surfaceCount() + 3;
	}

	@Override
	protected void doSerializeToInts(int[] data, int startAt) {
		data[startAt++] = this.isStatic ? (worldBits | Useful.INT_SIGN_BIT) : worldBits;
		data[startAt++] = shapeBits;
		System.arraycopy(paints, 0, data, startAt, surfaceCount());
	}

	@Override
	protected void doDeserializeFromInts(int[] data, int startAt) {
		// sign on first long word is used to store static indicator
		this.isStatic = (Useful.INT_SIGN_BIT & data[startAt]) == Useful.INT_SIGN_BIT;
		this.worldBits = Useful.INT_SIGN_BIT_INVERSE & data[startAt++];
		this.shapeBits = data[startAt++];
		System.arraycopy(data, startAt, paints, 0, surfaceCount());
	}

	@Override
	public void toTag(CompoundTag tag) {
		tag.putIntArray(ModelStateTagHelper.NBT_MODEL_BITS, serializeToInts());

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
		shapeBits = pBuff.readInt();
		worldBits = pBuff.readInt();
		final int limit = primitive.surfaces((R)this).size();
		for (int i = 0; i < limit; i++) {
			this.paints[i] = pBuff.readVarInt();
		}
	}

	@Override
	public void toBytes(PacketByteBuf pBuff) {
		pBuff.writeVarInt(primitive.index());
		pBuff.writeInt(shapeBits);
		pBuff.writeInt(worldBits);
		final int limit = primitive.surfaces((R)this).size();
		for (int i = 0; i < limit; i++) {
			pBuff.writeVarInt(paints[i]);
		}
	}

	////////////////////////////////////////// STATE FLAGS //////////////////////////////////////////

	/** contains indicators derived from shape and painters */
	private int stateFlags = 0;

	@Override
	public final int stateFlags() {
		int result = stateFlags;
		if (result == 0) {

			result = ModelStateFlags.IS_POPULATED | primitive.stateFlags((R)this);

			final int surfCount = primitive.surfaces((R)this).size();
			for (int i = 0; i < surfCount; i++) {
				final XmPaint p = paint(i);
				final int texDepth = p.textureDepth();
				for (int j = 0; j < texDepth; j++) {
					result |= p.texture(j).stateFlags();
				}
			}
			stateFlags = result;
		}
		return result;
	}

	protected final void clearStateFlags() {
		stateFlags = 0;
	}

	////////////////////////////////////////// PRIMITIVE //////////////////////////////////////////

	protected ModelPrimitive<R, W> primitive;

	@Override
	public final ModelPrimitive<R, W> primitive() {
		return primitive;
	}

	@Override
	public final void emitPolygons(Consumer<Polygon> target) {
		primitive.emitQuads((R)this, target);
	}

	@Override
	public W geometricState() {
		return primitive.geometricState((R)this);
	}

	@Override
	public final OrientationType orientationType() {
		return primitive.orientationType((R)this);
	}

	protected final int surfaceCount() {
		return primitive.surfaces((R)this).size();
	}

	////////////////////////////////////////// WORLD REFRESH CONTROL //////////////////////////////////////////

	protected boolean isStatic = false;

	@Override
	public final boolean isStatic() {
		return this.isStatic;
	}

	@Override
	public final W setStatic(boolean isStatic) {
		this.isStatic = isStatic;
		return (W)this;
	}

	////////////////////////////////////////// PAINT //////////////////////////////////////////

	protected abstract int maxSurfaces();

	protected final int[] paints = new int[maxSurfaces()];

	@Override
	public final boolean doPaintsMatch(ModelState other) {
		return other instanceof AbstractPrimitiveModelState && doPaintsMatchNative((AbstractPrimitiveModelState) other);
	}

	protected final boolean doPaintsMatchNative(AbstractPrimitiveModelState other) {
		final int limit = surfaceCount();
		if (limit == other.surfaceCount()) {
			final int[] paints = this.paints;
			final int[] otherPaints = other.paints;
			for (int i = 0; i < limit; i++) {
				if (otherPaints[i] != paints[i])
					return false;
			}
			return true;
		}
		return false;
	}

	protected final void paintInner(int surfaceIndex, int paintIndex) {
		paints[surfaceIndex] = paintIndex;
	}

	@Override
	public final int paintIndex(int surfaceIndex) {
		return paints[surfaceIndex];
	}

	@Override
	public final XmPaint paint(int surfaceIndex) {
		return XmPaintRegistry.INSTANCE.get(paintIndex(surfaceIndex));
	}

	@Override
	public final XmPaint paint(XmSurface surface) {
		return XmPaintRegistry.INSTANCE.get(paintIndex(surface.ordinal()));
	}

	@Override
	public final W paint(int surfaceIndex, int paintIndex) {
		paintInner(surfaceIndex, paintIndex);
		return (W)this;
	}

	@Override
	public final W paint(int surfaceIndex, XmPaint paint) {
		return paint(surfaceIndex, paint.index());
	}

	@Override
	public final W paint(XmSurface surface, XmPaint paint) {
		return paint(surface.ordinal(), paint.index());
	}

	@Override
	public final W paint(XmSurface surface, int paintIndex) {
		return paint(surface.ordinal(), paintIndex);
	}

	@Override
	public final W paintAll(XmPaint paint) {
		return paintAll(paint.index());
	}

	@Override
	public final W paintAll(int paintIndex) {
		final XmSurfaceList slist = primitive().surfaces((R)this);
		final int limit = slist.size();
		for (int i = 0; i < limit; i++) {
			paint(i, paintIndex);
		}
		return (W)this;
	}


	////////////////////////////////////////// WORLD ATTRIBUTES //////////////////////////////////////////

	protected int worldBits;

	@Override
	public int posX() {
		return POS_X.getValue(this);
	}

	@Override
	public int posY() {
		return POS_Y.getValue(this);
	}

	@Override
	public int posZ() {
		return POS_Z.getValue(this);
	}

	@Override
	public final W posX(int index) {
		POS_X.setValue(index, this);
		invalidateHashCode();
		return (W)this;
	}

	@Override
	public final W posY(int index) {
		POS_Y.setValue(index, this);
		invalidateHashCode();
		return (W)this;
	}

	@Override
	public final W posZ(int index) {
		POS_Z.setValue(index, this);
		invalidateHashCode();
		return (W)this;
	}

	@Override
	public final W pos(BlockPos pos) {
		POS_X.setValue((pos.getX()), this);
		POS_Y.setValue((pos.getY()), this);
		POS_Z.setValue((pos.getZ()), this);
		invalidateHashCode();
		return (W)this;
	}

	/**
	 * Means that one or more elements (like a texture) uses species. Does not mean
	 * that the shape or block actually capture or generate species other than 0.
	 */
	@Override
	public final boolean hasSpecies() {
		final int stateFlags = stateFlags();
		return ((stateFlags & BLOCK_SPECIES) == BLOCK_SPECIES);
	}

	/**
	 * Will return 0 if model state does not include species. This is more
	 * convenient than checking each place species is used.
	 *
	 * @return
	 */
	@Override
	public final int species() {
		return SPECIES.getValue(this);
	}

	@Override
	public final W species(int species) {
		SPECIES.setValue(species, this);
		invalidateHashCode();
		return (W)this;
	}

	////////////////////////////////////////// SHAPE ATTRIBUTES //////////////////////////////////////////

	protected int shapeBits;

	@Override
	public int orientationIndex() {
		return ORIENTATION.getValue(this);
	}

	@Override
	public W orientationIndex(int index) {
		ORIENTATION.setValue(index, this);
		return (W) this;
	}

	@Override
	public CornerJoinState cornerJoin() {
		return CornerJoinStateSelector.fromOrdinal(MathHelper.clamp(BLOCK_JOIN.getValue(this), 0, CornerJoinState.STATE_COUNT - 1));
	}

	@Override
	public final W cornerJoin(CornerJoinState join) {
		final int stateFlags = stateFlags();
		assert (stateFlags & CORNER_JOIN) != 0 : "Attempt to set corner join for model state not requiring it";

		BLOCK_JOIN.setValue(join.ordinal(), this);
		invalidateHashCode();
		return (W)this;
	}

	@Override
	public SimpleJoinState simpleJoin() {
		// If this state is using corner join, join index is for a corner join
		// and so need to derive simple join from the corner join
		final int stateFlags = stateFlags();
		return ((stateFlags & CORNER_JOIN) == 0) ? SimpleJoinState.fromOrdinal(BLOCK_JOIN.getValue(this)) : cornerJoin().simpleJoin();
	}

	@Override
	public final W simpleJoin(SimpleJoinState join) {
		final int stateFlags = stateFlags();
		assert (stateFlags & CORNER_JOIN) == 0 : "Attempt to set simple join for model state requiring corner join";

		BLOCK_JOIN.setValue(join.ordinal(), this);
		invalidateHashCode();
		return (W)this;
	}

	@Override
	public SimpleJoinState masonryJoin() {
		return SimpleJoinState.fromOrdinal(MASONRY_JOIN.getValue(this));
	}

	@Override
	public final W masonryJoin(SimpleJoinState join) {
		MASONRY_JOIN.setValue(join.ordinal(), this);
		invalidateHashCode();
		return (W)this;
	}

	@Override
	public int primitiveBits() {
		return PRIMITIVE_BITS.getValue(this);
	}

	@Override
	public final W primitiveBits(int bits) {
		PRIMITIVE_BITS.setValue(bits, this);
		return (W)this;
	}


	////////////////////////////////////////// RENDERING //////////////////////////////////////////

	@Environment(EnvType.CLIENT)
	void clearRendering() {
		mesh = null;
		particleSprite = null;
		particleColorARBG = 0;
		quadLists = null;
	}

	@Environment(EnvType.CLIENT)
	private Mesh mesh = null;

	@Environment(EnvType.CLIENT)
	private Mesh mesh() {
		Mesh result = mesh;
		if (result == null) {
			result = PaintManager.paint(this);
			mesh = result;
		}
		return result;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public final void emitQuads(RenderContext context) {
		context.meshConsumer().accept(mesh());
	}

	@Environment(EnvType.CLIENT)
	private Sprite particleSprite = null;

	@Environment(EnvType.CLIENT)
	private int particleColorARBG = 0;

	@Override
	@Environment(EnvType.CLIENT)
	public final Sprite particleSprite() {
		if(particleSprite == null) {
			final Mesh mesh = mesh();
			mesh.forEach(q -> {
				if(particleSprite == null) {
					final SpriteFinder finder = SpriteFinder.get(TextureSetHelper.blockAtas());
					particleSprite = finder.find(q, 0);
					particleColorARBG = q.spriteColor(0, 0);
				}
			});
			if(particleSprite == null) {
				particleSprite = TextureSetHelper.missingSprite();
			}
		}
		return particleSprite;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public final int particleColorARBG() {
		final int result = particleColorARBG;
		if(result == 0) {
			// if zero then probably requires lookup
			particleSprite();
			return particleColorARBG;
		} else
			return result;
	}

	@Environment(EnvType.CLIENT)
	private List<BakedQuad>[] quadLists = null;

	@Override
	@Environment(EnvType.CLIENT)
	public final List<BakedQuad> bakedQuads(BlockState state, Direction face, Random rand) {
		List<BakedQuad>[] lists = quadLists;
		if (lists == null) {
			lists = ModelHelper.toQuadLists(mesh());
			quadLists = lists;
		}
		final List<BakedQuad> result = lists[face == null ? 6 : face.getId()];
		return result == null ? ImmutableList.of() : result;
	}
}
