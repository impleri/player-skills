package net.impleri.playerskills.mixins.network;

import net.impleri.playerskills.api.InterceptedClientboundPacket;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V", at = @At("HEAD"))
    public void playerSkills$onSend(Packet<?> packet, PacketSendListener packetSendListener, CallbackInfo ci) {
        if (packet instanceof InterceptedClientboundPacket restrictedPacket) {
            restrictedPacket.playerSkills$interceptRestrictions(player);
        }
    }
}
