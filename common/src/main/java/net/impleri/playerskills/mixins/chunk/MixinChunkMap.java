package net.impleri.playerskills.mixins.chunk;

import net.impleri.playerskills.mixins.PlayerContext;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.class)
public class MixinChunkMap {
    @Inject(method = "playerLoadedChunk", at = @At("HEAD"))
    public void playerSkills$beforePlayerLoadedChunk(ServerPlayer serverPlayer, MutableObject<ClientboundLevelChunkWithLightPacket> mutableObject, LevelChunk levelChunk, CallbackInfo ci) {
        PlayerContext.set(serverPlayer);
    }

    @Inject(method = "playerLoadedChunk", at = @At("TAIL"))
    public void playerSkills$afterPlayerLoadedChunk(ServerPlayer serverPlayer, MutableObject<ClientboundLevelChunkWithLightPacket> mutableObject, LevelChunk levelChunk, CallbackInfo ci) {
        PlayerContext.unset();
    }
}
