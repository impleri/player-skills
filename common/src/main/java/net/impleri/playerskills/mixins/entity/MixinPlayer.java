package net.impleri.playerskills.mixins.entity;

import net.impleri.playerskills.api.BlockRestrictions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer {
    @Shadow
    public abstract boolean hasCorrectToolForDrops(BlockState arg);

    @Inject(method = "hasCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    public void playerSkills$onHasCorrectToolForDrops(BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
      var player = (Player) (Object) this;
        var replacement = BlockRestrictions.Companion.getReplacement(player, blockState, player.blockPosition());
        if (BlockRestrictions.Companion.isReplacedBlock(blockState, replacement)) {
            cir.setReturnValue(this.hasCorrectToolForDrops(replacement));
        }
    }
}
