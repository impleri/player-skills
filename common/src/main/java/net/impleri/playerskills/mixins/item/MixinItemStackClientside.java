package net.impleri.playerskills.mixins.item;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.impleri.playerskills.api.BlockRestrictions;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class MixinItemStackClientside {
    // Only used client-side for WAILA mods
    @Inject(method = "isCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void playerSkills$isCorrectTool(BlockState blockState, CallbackInfoReturnable<Boolean> cir) {
        if (Platform.getEnvironment() != Env.CLIENT) {
            return;
        }

        var player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        if (!BlockRestrictions.Companion.isHarvestable(player, blockState, player.blockPosition())) {
            cir.setReturnValue(false);
        }
    }
}
