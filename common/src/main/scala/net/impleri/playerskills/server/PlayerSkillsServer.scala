package net.impleri.playerskills.server

import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.facades.architectury.ReloadListeners
import net.impleri.playerskills.server.skills.PlayerRegistry

object PlayerSkillsServer {
  val STATE: ServerStateContainer = ServerStateContainer(
    globalState = PlayerSkills.STATE,
    playerRegistry = PlayerRegistry(skillsRegistry = PlayerSkills.STATE.SKILLS),
    reloadListeners = ReloadListeners(),
  )

  lazy val EVENTS: EventHandler = EventHandler()

  def init(): Unit = {}
}
