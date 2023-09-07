package net.impleri.playerskills.mixins.block;

import net.impleri.playerskills.api.FluidRestrictions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiquidBlock.class)
public class MixinLiquidBlock {
    @Shadow
    @Final
    protected FlowingFluid fluid;
    @Unique


    @Inject(method = "pickupBlock", at = @At("HEAD"), cancellable = true)
    private void playerSkills$onPickupBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, CallbackInfoReturnable<ItemStack> cir) {
        // Action is targeting a non-fluid block, so nothing needs to be done here
        if (!FluidRestrictions.Companion.isFluidBlock(blockState)) {
            return;
        }

        // Action is targeting a non-source block, so it can't be picked up
        if (!blockState.getFluidState().isSource()) {
            return;
        }

        if (!FluidRestrictions.Companion.canBucket(fluid, levelAccessor, blockPos)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
