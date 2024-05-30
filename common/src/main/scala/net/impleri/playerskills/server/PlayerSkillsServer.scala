package net.impleri.playerskills.server

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.server.skills.PlayerRegistry
import net.impleri.slab.resources.ReloadListeners

object PlayerSkillsServer {
  val EVENTS: EventHandler = EventHandler()

  val STATE: ServerStateContainer = ServerStateContainer(
    globalState = PlayerSkills.STATE,
    PLAYERS = PlayerRegistry(skillsRegistry = PlayerSkills.STATE.SKILLS),
    eventHandler = EVENTS,
    reloadListeners = ReloadListeners(),
  )

  // no-op to get the server-side functionality created in both physical client and servers
  def create(): Unit = {}

  // Actual hook to trigger physical-server-only code
  def init(): Unit = {}
}
