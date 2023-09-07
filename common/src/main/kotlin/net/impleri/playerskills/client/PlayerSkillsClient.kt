package net.impleri.playerskills.client

import com.google.common.collect.ImmutableList
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.events.ClientSkillsUpdatedEvent
import net.impleri.playerskills.utils.PlayerSkillsLogger
import org.jetbrains.annotations.ApiStatus

object PlayerSkillsClient {
  fun init() {
    ClientEventHandlers.init()
    PlayerSkillsLogger.SKILLS.info("PlayerSkills Client started")
  }

  /**
   * Request Server to resend skills
   */
  fun resyncSkills() {
    NetHandler.resyncPlayer()
  }

  /**
   * Handle updated skills from server
   */
  @ApiStatus.Internal
  fun syncFromServer(skills: ImmutableList<Skill<*>>, force: Boolean) {
    Registry.syncFromServer(skills, force)
  }

  /**
   * Broadcast client-side event that skills have changed
   */
  @ApiStatus.Internal
  fun emitSkillsUpdated(skills: ImmutableList<Skill<*>>, prev: ImmutableList<Skill<*>>, force: Boolean) {
    ClientSkillsUpdatedEvent.EVENT.invoker().accept(ClientSkillsUpdatedEvent(skills, prev, force))
  }
}
