package net.impleri.playerskills.network

import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.client.ClientStateContainer
import net.impleri.playerskills.server.ServerStateContainer
import net.impleri.slab.network.MessageManager
import net.impleri.slab.network.Network

case class Manager(
  globalState: StateContainer = StateContainer(),
  clientStateContainer: Option[ClientStateContainer] = None,
  serverStateContainer: Option[ServerStateContainer] = None,
) extends MessageManager {
  override val network: Network = globalState.NETWORK

  val SYNC_SKILLS: SyncSkillsMessageFactory = SyncSkillsMessageFactory(
    globalState.SKILL_TYPE_OPS,
    clientStateContainer,
  )

  registerFactoryToClient(SYNC_SKILLS)

  val RESYNC_SKILLS: ResyncSkillsMessageFactory = ResyncSkillsMessageFactory(serverStateContainer)

  registerFactoryToServer(RESYNC_SKILLS)
}
