package net.impleri.playerskills.facades

import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer

case class MinecraftClient() {
  def getInstace: Minecraft = Minecraft.getInstance()

  def getPlayer: MinecraftPlayer[LocalPlayer] = MinecraftPlayer(getInstace.player)
}
