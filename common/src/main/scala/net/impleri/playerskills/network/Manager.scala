package net.impleri.playerskills.network

import dev.architectury.networking.simple.MessageType
import net.impleri.playerskills.StateContainer
import net.impleri.playerskills.client.ClientSkillsRegistry
import net.impleri.playerskills.server.ServerStateContainer

case class Manager(
  globalState: StateContainer = StateContainer(),
  clientSkills: ClientSkillsRegistry = ClientSkillsRegistry(),
  serverStateContainer: ServerStateContainer = ServerStateContainer(),
) {
  val SYNC_SKILLS: SyncSkillsMessageFactory = SyncSkillsMessageFactory(
    globalState.getSkillTypeOps,
    clientSkills,
    globalState.NETWORK,
  )

  private val SYNC_TYPE: MessageType = globalState.NETWORK
    .registerClientboundMessage(SyncSkillsMessageFactory.NAME, SYNC_SKILLS.receive)

  SYNC_SKILLS.setMessageType(SYNC_TYPE)

  val RESYNC_SKILLS: ResyncSkillsMessageFactory = ResyncSkillsMessageFactory(serverStateContainer)

  private val RESYNC_TYPE: MessageType = globalState.NETWORK
    .registerServerboundMessage(ResyncSkillsMessageFactory.NAME, RESYNC_SKILLS.receive)

  RESYNC_SKILLS.setMessageType(RESYNC_TYPE)
}
