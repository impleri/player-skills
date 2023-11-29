package net.impleri.playerskills.client

import net.impleri.playerskills.facades.minecraft.Client
import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.network.Manager

case class ClientStateContainer(
  globalState: StateContainer = StateContainer(),
  eventHandler: EventHandler = EventHandler(),
  client: Client = Client(),
) {
  val SKILLS: ClientSkillsRegistry = ClientSkillsRegistry(eventHandler)

  lazy private val MANAGER = Manager(globalState, SKILLS)

  def getNetHandler: NetHandler = NetHandler(client, MANAGER.RESYNC_SKILLS)
}
