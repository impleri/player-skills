package net.impleri.playerskills.mixins.material;

import net.impleri.playerskills.api.FluidFiniteMode;
import net.impleri.playerskills.api.FluidRestrictions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowingFluid.class)
public abstract class MixinFlowingFluid {
    @Shadow
    protected abstract boolean isSourceBlockOfThisType(FluidState fluidState);

    @Shadow
    protected abstract boolean canPassThroughWall(Direction direction, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2);

    @Shadow
    protected abstract int getDropOff(LevelReader levelReader);

    @Unique
    private FluidFiniteMode playerSkills$getFiniteMode(Fluid fluid, LevelReader levelReader, BlockPos blockPos) {
        ResourceLocation currentDimension;
        var currentBiome = levelReader.getBiome(blockPos).unwrapKey().orElseThrow().location();

        if (levelReader instanceof Level level) {
            currentDimension = level.dimension().location();
        } else {
            // This is pretty hacky and rests on the hope that the dimension name matches the dimension type
            currentDimension = BuiltinRegistries.DIMENSION_TYPE.getKey(levelReader.dimensionType());
        }

        var mode = FluidRestrictions.Companion.getFiniteModeFor(fluid, currentDimension, currentBiome);

        if (mode != FluidFiniteMode.DEFAULT) {
//            PlayerSkillsLogger.Companion.FLUIDS.debug("Altering fluid {} to be {}", FluidRestrictions.Companion.getFluidName(fluid), mode);
        }

        return mode;
    }

    @Inject(method = "getNewLiquid", at = @At("RETURN"), cancellable = true)
    private void playerSkills$onGetNewLiquid(LevelReader levelReader, BlockPos blockPos, BlockState blockState, CallbackInfoReturnable<FluidState> cir) {
        var fluid = (FlowingFluid) (Object) this;

        int maxAmount = 0;
        int nearbySources = 0;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos relativeBlock = blockPos.relative(direction);
            BlockState relativeState = levelReader.getBlockState(relativeBlock);
            FluidState relativeFluid = relativeState.getFluidState();
            if (relativeFluid.getType().isSame(fluid) && canPassThroughWall(direction, levelReader, blockPos, blockState, relativeBlock, relativeState)) {
                if (relativeFluid.isSource()) {
                    ++nearbySources;
                }

                maxAmount = Math.max(maxAmount, relativeFluid.getAmount());
            }
        }

        var mode = playerSkills$getFiniteMode(fluid, levelReader, blockPos);

        switch (mode) {
            case DEFAULT -> {
                // do nothing
            }
            case FINITE -> {
                // Copy logic from FlowingFluid.getNewLiquid for non-source blocks
                var originalReturn = cir.getReturnValue();
                if (originalReturn.isSource()) {
                    var blockPosAbove = blockPos.above();
                    BlockState blockStateAbove = levelReader.getBlockState(blockPosAbove);
                    FluidState fluidStateAbove = blockStateAbove.getFluidState();
                    if (!fluidStateAbove.isEmpty() && fluidStateAbove.getType().isSame(fluid) && canPassThroughWall(Direction.UP, levelReader, blockPos, blockState, blockPosAbove, blockStateAbove)) {
                        cir.setReturnValue(fluid.getFlowing(8, true));
                    } else {
                        int k = maxAmount - getDropOff(levelReader);
                        cir.setReturnValue(k <= 0 ? Fluids.EMPTY.defaultFluidState() : fluid.getFlowing(k, false));
                    }
                }
            }
            case INFINITE -> {
                // Copy logic from FlowingFluid.getNewLiquid for creating new source blocks but exclude call to canConvertToSource
                if (nearbySources >= 2) {
                    BlockState blockStateBelow = levelReader.getBlockState(blockPos.below());
                    FluidState fluidStateBelow = blockStateBelow.getFluidState();
                    if (blockStateBelow.getMaterial().isSolid() || isSourceBlockOfThisType(fluidStateBelow)) {
                        cir.setReturnValue(fluid.getSource(false));
                    }
                }
            }
        }
    }
}
