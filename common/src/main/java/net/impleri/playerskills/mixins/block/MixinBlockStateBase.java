package net.impleri.playerskills.mixins.block;

import net.impleri.playerskills.api.BlockRestrictions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinBlockStateBase {
    @Shadow
    protected abstract BlockState asState();

    // Intercept `use` call to a block and inject the replacement's output
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void playerSkills$onUse(Level level, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        var actualBlock = BlockRestrictions.Companion.maybeGetReplacement(player, asState(), blockHitResult.getBlockPos());

        if (actualBlock != null) {
            cir.setReturnValue(actualBlock.use(level, player, interactionHand, blockHitResult));
        }
    }

    // Prevent break progress for unbreakable blocks
    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void playerSkills$onGetDestroySpeed(BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<Float> cir) {
        if (!BlockRestrictions.Companion.isBreakable(blockGetter, blockPos)) {
            cir.setReturnValue(-1.0F);
        }
    }
}
