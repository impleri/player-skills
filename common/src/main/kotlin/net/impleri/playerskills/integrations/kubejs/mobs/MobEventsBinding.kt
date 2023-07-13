package net.impleri.playerskills.integrations.kubejs.mobs

import dev.latvian.mods.kubejs.event.EventGroup
import dev.latvian.mods.kubejs.event.EventHandler

object MobEventsBinding {
  val GROUP: EventGroup = EventGroup.of("MobSkillEvents")
  val RESTRICTIONS: EventHandler = GROUP.server("register") { MobRestrictionsEventJS::class.java }
}
