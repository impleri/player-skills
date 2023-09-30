package net.impleri.playerskills.mixins.fabric;

import net.impleri.playerskills.api.BlockRestrictions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireBlock.class)
public class MixinFireBlock {
    @Redirect(method = "canSurvive", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState onCanSurvive$BlockSkills(LevelReader instance, BlockPos blockPos) {
        return BlockRestrictions.Companion.getReplacement(instance, blockPos);
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState onTick$BlockSkills(ServerLevel instance, BlockPos blockPos) {
        return BlockRestrictions.Companion.getReplacement(instance, blockPos);
    }

    @Redirect(method = "getStateForPlacement(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState onGetStateForPlacement$BlockSkills(BlockGetter instance, BlockPos blockPos) {
        return BlockRestrictions.Companion.getReplacement(instance, blockPos);
    }

    @Redirect(method = "getIgniteOdds(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState onGetIgniteOdds$BlockSkills(LevelReader instance, BlockPos blockPos) {
        return BlockRestrictions.Companion.getReplacement(instance, blockPos);
    }

    @Redirect(method = "isValidFireLocation", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BlockGetter;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState onIsValidFireLocation$BlockSkills(BlockGetter instance, BlockPos blockPos) {
        return BlockRestrictions.Companion.getReplacement(instance, blockPos);
    }

    @Redirect(method = "checkBurnOut", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState onCheckBurnOut$BlockSkills(Level instance, BlockPos blockPos) {
        return BlockRestrictions.Companion.getReplacement(instance, blockPos);
    }
}
