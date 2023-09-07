package net.impleri.playerskills.mixins;

import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.atomic.AtomicReference;

public class PlayerContext {
  private static final AtomicReference<ServerPlayer> currentPlayer = new AtomicReference<>(null);

  public static void set(ServerPlayer player) {
    currentPlayer.set(player);
  }

  public static ServerPlayer get() {
    return currentPlayer.get();
  }

  public static void unset() {
    currentPlayer.set(null);
  }
}
