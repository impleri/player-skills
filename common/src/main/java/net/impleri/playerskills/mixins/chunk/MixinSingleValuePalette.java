package net.impleri.playerskills.mixins.chunk;

import net.impleri.playerskills.api.BlockRestrictions;
import net.impleri.playerskills.mixins.PlayerContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.SingleValuePalette;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SingleValuePalette.class)
public class MixinSingleValuePalette<T> {
    @Shadow
    @Nullable
    private T value;

    @Inject(method = "write", at = @At("HEAD"), cancellable = true)
    public void playerSkills$onWrite(FriendlyByteBuf arg, CallbackInfo ci) {
        if (this.value != null && this.value instanceof BlockState) {
            arg.writeVarInt(BlockRestrictions.Companion.getReplacementId(PlayerContext.get(), (BlockState) this.value, null));

            ci.cancel();
        }
    }
}
