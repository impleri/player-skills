package net.impleri.playerskills.api;

import net.minecraft.server.level.ServerPlayer;

public interface InterceptedClientboundPacket {
    public void playerSkills$interceptRestrictions(ServerPlayer player);
}
