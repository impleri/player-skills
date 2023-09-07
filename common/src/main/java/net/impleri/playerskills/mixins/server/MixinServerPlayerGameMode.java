package net.impleri.playerskills.mixins.server;

import net.impleri.playerskills.api.BlockRestrictions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerGameMode.class)
public class MixinServerPlayerGameMode {
    @Shadow
    @Final
    protected ServerPlayer player;

    @Redirect(method = "handleBlockBreakAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    public BlockState playerSkills$onBlockBreak(ServerLevel instance, BlockPos blockPos) {
      var original = instance.getBlockState(blockPos);
      return BlockRestrictions.Companion.getReplacement(player, original, blockPos);
    }

    @Redirect(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    public BlockState playerSkills$onDestroyBlock(ServerLevel instance, BlockPos blockPos) {
      var original = instance.getBlockState(blockPos);
      return BlockRestrictions.Companion.getReplacement(player, original, blockPos);
    }

    @Redirect(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    public BlockState playerSkills$onUseItemOn(Level instance, BlockPos blockPos) {
      var original = instance.getBlockState(blockPos);
      return BlockRestrictions.Companion.getReplacement(player, original, blockPos);
    }
}
