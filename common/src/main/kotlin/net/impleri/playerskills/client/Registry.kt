package net.impleri.playerskills.client

import com.google.common.collect.ImmutableList
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.utils.PlayerSkillsLogger

/**
 * Internal client-side registry
 */
internal object Registry {
  private val playerSkills: MutableList<Skill<*>> = ArrayList()
  fun syncFromServer(skills: ImmutableList<Skill<*>>, force: Boolean) {
    val prev = get()
    playerSkills.clear()

    val skillList = skills.joinToString(", ") { "${it.name}=${it.value ?: "NULL"}" }
    PlayerSkillsLogger.SKILLS.info("Syncing Client-side skills: $skillList")

    playerSkills.addAll(skills)
    PlayerSkillsClient.emitSkillsUpdated(skills, prev, force)
  }

  fun get(): ImmutableList<Skill<*>> {
    return ImmutableList.copyOf(playerSkills)
  }
}
