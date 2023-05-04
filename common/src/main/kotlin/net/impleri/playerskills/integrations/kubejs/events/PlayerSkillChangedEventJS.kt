package net.impleri.playerskills.integrations.kubejs.events

import dev.latvian.mods.kubejs.server.ServerEventJS
import dev.latvian.mods.kubejs.util.UtilsJS
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.events.SkillChangedEvent
import net.minecraft.world.entity.player.Player

class PlayerSkillChangedEventJS<T>(private val event: SkillChangedEvent<T>) : ServerEventJS(UtilsJS.staticServer) {
  val isImproved: Boolean
    get() {
      val type = event.type ?: return false
      return type.getNextValue(event.previous) === event.skill.value
    }
  val isDegraded: Boolean
    get() {
      val type = event.type ?: return false
      return type.getPrevValue(event.previous) === event.skill.value
    }
  val skill: Skill<T>
    get() = event.skill
  val previous: Skill<T>
    get() = event.previous
  val player: Player
    get() = event.player
}
