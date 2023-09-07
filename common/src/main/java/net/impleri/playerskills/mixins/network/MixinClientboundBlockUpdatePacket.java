package net.impleri.playerskills.mixins.network;

import net.impleri.playerskills.api.InterceptedClientboundPacket;
import net.impleri.playerskills.api.BlockRestrictions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundBlockUpdatePacket.class)
public class MixinClientboundBlockUpdatePacket implements InterceptedClientboundPacket {
    @Shadow
    @Final
    @Mutable
    private BlockState blockState;

    @Shadow
    @Final
    private BlockPos pos;

    @Override
    public void playerSkills$interceptRestrictions(ServerPlayer player) {
        var newState = BlockRestrictions.Companion.getReplacement(player, blockState, pos);

        if (BlockRestrictions.Companion.isReplacedBlock(blockState, newState)) {
            blockState = newState;
        }
    }
}
