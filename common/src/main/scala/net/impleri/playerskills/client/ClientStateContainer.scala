package net.impleri.playerskills.client

import net.impleri.playerskills.facades.MinecraftClient

case class ClientStateContainer(client: MinecraftClient = MinecraftClient()) {
  val SKILLS: ClientSkillsRegistry = ClientSkillsRegistry()

  def getNetHandler: NetHandler = NetHandler(client)
}
