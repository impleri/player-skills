package net.impleri.playerskills.mixins.block;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.impleri.playerskills.api.BlockRestrictions;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinBlockStateBaseClientside {

    @Shadow
    protected abstract BlockState asState();

    // Intercepts `requireCorrectTool` call clientside for WAILA mods to show the right values
    @Inject(method = "requiresCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void playerSkills$requireCorrectTool(CallbackInfoReturnable<Boolean> cir) {
        if (Platform.getEnvironment() != Env.CLIENT) {
            return;
        }

        var player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        if (!BlockRestrictions.Companion.isHarvestable(player, asState(), player.blockPosition())) {
            cir.setReturnValue(true);
        }
    }
}
