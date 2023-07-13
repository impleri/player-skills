package net.impleri.playerskills.integrations.kubejs.skills

import dev.latvian.mods.kubejs.server.ServerEventJS
import dev.latvian.mods.kubejs.util.UtilsJS
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.events.SkillChangedEvent
import net.minecraft.world.entity.player.Player

class PlayerSkillChangedEventJS<T>(private val event: SkillChangedEvent<T>) : ServerEventJS(UtilsJS.staticServer) {
  fun getIsImproved(): Boolean {
    val type = event.type ?: return false
    return type.getNextValue(event.previous) == event.skill.value
  }

  fun getIsDegraded(): Boolean {
    val type = event.type ?: return false
    return type.getPrevValue(event.previous) == event.skill.value
  }

  fun getSkill(): Skill<T> {
    return event.skill
  }

  fun getPrevious(): Skill<T> {
    return event.previous
  }

  fun getPlayer(): Player {
    return event.player
  }
}
