package net.impleri.playerskills.mixins.block;

import net.impleri.playerskills.api.BlockRestrictions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

// @TODO: Maybe delete?
@Mixin(Block.class)
public class MixinBlock {
    @Inject(method = "getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    private static void playerSkills$onGetDrops(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfoReturnable<List<ItemStack>> cir) {
        var currentValue = cir.getReturnValue();
        if (entity instanceof ServerPlayer player && !currentValue.isEmpty()) {
            var newDrops = BlockRestrictions.Companion.getDrops(player, blockState, serverLevel, blockPos, blockEntity, tool);
            if (newDrops != null) {
                cir.setReturnValue(newDrops);
            }
        }
    }
}
