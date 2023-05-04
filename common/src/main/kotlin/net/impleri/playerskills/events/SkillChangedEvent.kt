package net.impleri.playerskills.events

import dev.architectury.event.Event
import dev.architectury.event.EventFactory
import net.impleri.playerskills.api.Skill
import net.impleri.playerskills.api.SkillType
import net.minecraft.world.entity.player.Player
import java.util.function.Consumer

class SkillChangedEvent<T>(val player: Player, val skill: Skill<T>, val previous: Skill<T>) {
  val type: SkillType<T>? = SkillType.find(skill)

  companion object {
    val EVENT: Event<Consumer<SkillChangedEvent<*>>> = EventFactory.createConsumerLoop()
  }
}
