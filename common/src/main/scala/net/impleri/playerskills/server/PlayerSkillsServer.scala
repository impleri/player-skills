package net.impleri.playerskills.server

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.facades.architectury.ReloadListeners
import net.impleri.playerskills.server.skills.PlayerRegistry

object PlayerSkillsServer {
  val EVENTS: EventHandler = EventHandler()

  val STATE: ServerStateContainer = ServerStateContainer(
    globalState = PlayerSkills.STATE,
    playerRegistry = PlayerRegistry(skillsRegistry = PlayerSkills.STATE.SKILLS),
    reloadListeners = ReloadListeners(),
    eventHandler = EVENTS,
  )

  // no-op to get the server-side functionality created in both physical client and servers
  def create(): Unit = {}

  // Actual hook to trigger physical-server-only code
  def init(): Unit = {}
}
