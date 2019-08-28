package grondag.xm.api.modelstate.primitive;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import grondag.xm.api.connect.world.BlockNeighbors;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@API(status = EXPERIMENTAL)
@FunctionalInterface
public interface BlockStateToPrimitiveStateMutator extends WorldToPrimitiveStateMutator {
    @Override
    MutablePrimitiveState apply(MutablePrimitiveState modelState, BlockState blockState);
    
    @Override
    default void accept(MutablePrimitiveState modelState, BlockState blockState, @Nullable BlockView world, @Nullable BlockPos pos, @Nullable BlockNeighbors neighbors, boolean refreshFromWorld) {
        apply(modelState, blockState);
    }
}