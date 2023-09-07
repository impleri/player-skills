package net.impleri.playerskills.mixins.chunk;

import net.impleri.playerskills.api.BlockRestrictions;
import net.impleri.playerskills.mixins.PlayerContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.HashMapPalette;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HashMapPalette.class)
abstract public class MixinHashMapPalette<T> {
    @Shadow
    @Final
    private CrudeIncrementalIntIdentityHashBiMap<T> values;

    @Shadow
    abstract public int getSize();

    @Inject(method = "write", at = @At("HEAD"), cancellable = true)
    public void playerSkills$onWrite(FriendlyByteBuf arg, CallbackInfo ci) {
        if (this.getSize() > 0 && this.values.byId(0) instanceof BlockState) {
            int i = this.getSize();
            arg.writeVarInt(i);

            for (int j = 0; j < i; ++j) {
              var currentState = this.values.byId(j);
              if (currentState != null) {
                arg.writeVarInt(BlockRestrictions.Companion.getReplacementId(PlayerContext.get(), (BlockState) currentState, null));
              }
            }

            ci.cancel();
        }
    }
}
