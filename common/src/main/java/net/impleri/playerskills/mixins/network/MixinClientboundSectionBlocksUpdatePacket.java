package net.impleri.playerskills.mixins.network;

import net.impleri.playerskills.api.InterceptedClientboundPacket;
import net.impleri.playerskills.api.BlockRestrictions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundSectionBlocksUpdatePacket.class)
public class MixinClientboundSectionBlocksUpdatePacket implements InterceptedClientboundPacket {
    @Shadow
    @Final
    private BlockState[] states;

    @Shadow
    @Final
    private SectionPos sectionPos;

    @Shadow
    @Final
    private short[] positions;

    @Override
    public void playerSkills$interceptRestrictions(ServerPlayer player) {
        for (int i = 0; i < states.length; i++) {
            var posOffset = positions[i];
            var blockPos = new BlockPos(sectionPos.relativeToBlockX(posOffset), sectionPos.relativeToBlockY(posOffset), sectionPos.relativeToBlockZ(posOffset));
            var newState = BlockRestrictions.Companion.getReplacement(player, states[i], blockPos);

            if (BlockRestrictions.Companion.isReplacedBlock(states[i], newState)) {
                states[i] = newState;
            }
        }
    }
}
