package net.impleri.playerskills.mixins.forge;

import net.impleri.playerskills.api.BlockRestrictions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LavaFluid.class)
public class MixinLavaFluid {
    @Redirect(method = "isFlammable(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState onIsFlammable$BlockSkills(LevelReader instance, BlockPos blockPos) {
        return BlockRestrictions.Companion.getReplacement(instance, blockPos);
    }
}
